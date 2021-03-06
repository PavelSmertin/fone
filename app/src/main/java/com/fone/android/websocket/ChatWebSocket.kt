package com.fone.android.websocket

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.fone.android.Constants.API.WS_URL
import com.fone.android.FoneApplication
import com.fone.android.api.ClientErrorException
import com.fone.android.db.*
import com.fone.android.extension.gzip
import com.fone.android.extension.networkConnected
import com.fone.android.extension.ungzip
import com.fone.android.job.FoneJobManager
import com.fone.android.job.RefreshOffsetJob
import com.fone.android.util.ErrorHandler.Companion.AUTHENTICATION
import com.fone.android.util.GzipException
import com.fone.android.util.Session
import com.fone.android.vo.*
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.*
import okio.ByteString
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ChatWebSocket(
    private val okHttpClient: OkHttpClient,
    val app: Application,
    val conversationDao: ConversationDao,
    val messageDao: MessageDao,
    private val offsetDao: OffsetDao,
    private val floodMessageDao: FloodMessageDao,
    val jobManager: FoneJobManager,
    private val linkState: LinkState,
    private val jobDao: JobDao
) : WebSocketListener() {

    private val failCode = 1000
    private val quitCode = 1001
    var connected: Boolean = false
    private var client: WebSocket? = null
    private val transactions = ConcurrentHashMap<String, WebSocketTransaction>()
    private val gson = Gson()
    private val accountId = Session.getAccountId()
    private val sessionId = Session.getSessionId()

    companion object {
        val TAG = ChatWebSocket::class.java.simpleName
    }

    init {
        connected = false
    }

    @Synchronized
    fun connect() {
        if (client == null) {
            connected = false
            client = okHttpClient.newWebSocket(Request.Builder().url(WS_URL).build(), this)
        }
    }

    @Synchronized
    fun disconnect() {
        if (client != null) {
            closeInternal(quitCode)
            transactions.clear()
            connectTimer?.dispose()
            client = null
            connected = false
        }
    }

    @Synchronized
    fun sendMessage(blazeMessage: BlazeMessage): BlazeMessage? {
        var bm: BlazeMessage? = null
        val latch = CountDownLatch(1)
        val transaction = WebSocketTransaction(blazeMessage.id,
            object : TransactionCallbackSuccess {
                override fun success(data: BlazeMessage) {
                    bm = data
                    latch.countDown()
                }
            },
            object : TransactionCallbackError {
                override fun error(data: BlazeMessage?) {
                    bm = data
                    latch.countDown()
                }
            })
        if (client != null && connected) {
            transactions[blazeMessage.id] = transaction
            val result = client!!.send(gson.toJson(blazeMessage).gzip())
            if (result) {
                latch.await(5, TimeUnit.SECONDS)
            }
        } else {
            Log.e(TAG, "WebSocket not connect")
        }
        return bm
    }

    private fun sendPendingMessage() {
        val blazeMessage = createListPendingMessage()
        val transaction = WebSocketTransaction(blazeMessage.id,
            object : TransactionCallbackSuccess {
                override fun success(data: BlazeMessage) {
                }
            },
            object : TransactionCallbackError {
                override fun error(data: BlazeMessage?) {
                    sendPendingMessage()
                }
            })
        transactions[blazeMessage.id] = transaction
        client?.send(gson.toJson(blazeMessage).gzip())
    }

    @Synchronized
    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        if (client != null) {
            connected = true
            client = webSocket
            webSocketObserver?.onSocketOpen()
            FoneApplication.appContext.runOnUiThread {
                linkState.state = LinkState.ONLINE
            }
            connectTimer?.dispose()
            jobManager.start()
            jobManager.addJobInBackground(RefreshOffsetJob())
            sendPendingMessage()
        }
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        try {
            val json = bytes?.ungzip()
            val blazeMessage = gson.fromJson(json, BlazeMessage::class.java)
            if (blazeMessage.error == null) {
                if (transactions[blazeMessage.id] != null) {
                    transactions[blazeMessage.id]!!.success.success(blazeMessage)
                    transactions.remove(blazeMessage.id)
                }
                if (blazeMessage.data != null && blazeMessage.isReceiveMessageAction()) {
                    handleReceiveMessage(blazeMessage)
                }
            } else {
                if (transactions[blazeMessage.id] != null) {
                    transactions[blazeMessage.id]!!.error.error(blazeMessage)
                    transactions.remove(blazeMessage.id)
                }
                if (blazeMessage.action == ERROR_ACTION && blazeMessage.error.code == AUTHENTICATION) {
                    val errorDescription = "Force logout webSocket.\nblazeMessage: $blazeMessage"
                    val ise = IllegalStateException(errorDescription)
//                    Bugsnag.notify(ise)
//                    Crashlytics.log(Log.ERROR, "401", errorDescription)
//                    Crashlytics.logException(ise)
                    connected = false
                    closeInternal(quitCode)
                    (app as FoneApplication).closeAndClear()
                }
            }
        } catch (e: GzipException) {
//            Bugsnag.notify(e)
        }
    }

    @SuppressLint("CheckResult")
    @Synchronized
    override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
        connected = false
        if (code == failCode) {
            closeInternal(code)
            jobManager.stop()
            if (connectTimer == null || connectTimer?.isDisposed == true) {
                connectTimer = Observable.interval(2000, TimeUnit.MILLISECONDS).subscribe({
                    if (FoneApplication.appContext.networkConnected() && Session.checkToken()) {
                        connect()
                    }
                }, {
                })
            }
        } else {
            webSocket?.cancel()
        }
    }

    private var connectTimer: Disposable? = null

    @Synchronized
    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
        t?.let {
            Log.e(TAG, "WebSocket onFailure ", it)
        }
        if (client != null) {
            if (t != null && (t is ClientErrorException && t.code == AUTHENTICATION)) {
                closeInternal(quitCode)
            } else {
                onClosed(webSocket, failCode, "OK")
            }
        }
    }

    private fun closeInternal(code: Int) {
        try {
            connected = false
            if (client != null) {
                client!!.close(code, "OK")
            }
        } catch (e: Exception) {
//            Bugsnag.notify(e)
        } finally {
            client = null
            webSocketObserver?.onSocketClose()
            FoneApplication.appContext.runOnUiThread {
                linkState.state = LinkState.OFFLINE
            }
        }
    }

    private fun handleReceiveMessage(blazeMessage: BlazeMessage) {
        val data = gson.fromJson(blazeMessage.data, BlazeMessageData::class.java)
        if (blazeMessage.action == ACKNOWLEDGE_MESSAGE_RECEIPT) {
            makeMessageStatus(data.status, data.messageId)
            offsetDao.insert(Offset(STATUS_OFFSET, data.updatedAt))
        } else if (blazeMessage.action == CREATE_MESSAGE || blazeMessage.action == CREATE_CALL) {
            if (data.userId == accountId && data.category.isEmpty()) {
                makeMessageStatus(data.status, data.messageId)
            } else {
                floodMessageDao.insert(FloodMessage(data.messageId, gson.toJson(data), data.createdAt))
            }
        } else if (blazeMessage.action == CREATE_SESSION_MESSAGE) {
            if (data.userId == accountId && data.sessionId == sessionId && data.category.isEmpty()) {
            } else {
                floodMessageDao.insert(FloodMessage(data.messageId, gson.toJson(data), data.createdAt))
            }
        } else {
            jobDao.insert(createAckJob(ACKNOWLEDGE_MESSAGE_RECEIPTS, BlazeAckMessage(data.messageId, MessageStatus.READ.name)))
        }
    }

    private fun makeMessageStatus(status: String, messageId: String) {
        val curStatus = messageDao.findMessageStatusById(messageId)
        if (curStatus != null && curStatus != MessageStatus.READ.name) {
            messageDao.updateMessageStatus(status, messageId)
        }
        sendSessionAck(status, messageId)
    }

    private fun sendSessionAck(status: String, messageId: String) {
        val extensionSessionId = Session.getExtensionSessionId()
        extensionSessionId?.let {
            jobDao.insert(createAckJob(CREATE_SESSION_MESSAGE, BlazeAckMessage(messageId, status)))
        }
    }

    fun setWebSocketObserver(webSocketObserver: WebSocketObserver) {
        this.webSocketObserver = webSocketObserver
    }

    private var webSocketObserver: WebSocketObserver? = null

    interface WebSocketObserver {
        fun onSocketClose()
        fun onSocketOpen()
    }
}
