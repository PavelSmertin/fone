package com.fone.android.websocket

import android.app.Application
import android.util.Log
import com.fone.android.FoneApplication
import com.fone.android.db.FloodMessageDao
import com.fone.android.db.JobDao
import com.fone.android.db.MessageDao
import com.fone.android.db.OffsetDao
import com.fone.android.extension.ungzip
import com.fone.android.util.ErrorHandler
import com.fone.android.util.GzipException
import com.fone.android.util.Session
import com.fone.android.vo.*
import com.google.gson.Gson
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent
import okhttp3.WebSocket
import okio.ByteString
import org.jetbrains.anko.getStackTraceString
import java.util.concurrent.ConcurrentHashMap

class SSEHandler(
    val app: Application,
    val messageDao: MessageDao,
    private val offsetDao: OffsetDao,
    private val floodMessageDao: FloodMessageDao,
    private val jobDao: JobDao
): EventSourceHandler {


    private val transactions = ConcurrentHashMap<String, WebSocketTransaction>()
    private val gson = Gson()
    private val accountId = Session.getAccountId()
    private val sessionId = Session.getSessionId()

    override fun onConnect() {
        Log.v("SSE Connected", "True")
    }

    override fun onMessage(event: String, message: MessageEvent) {
        Log.v("SSE Message", event)
        Log.v("SSE Message: ", message.lastEventId)
        Log.v("SSE Message: ", message.data)
    }

    override fun onComment(comment: String) {
        //comments only received if exposeComments turned on
        Log.v("SSE Comment", comment)
    }

    override fun onError(t: Throwable) {
        Log.v("SSE onError", t.getStackTraceString())

        //ignore ssl NPE on eventSource.close()
    }

    override fun onClosed(willReconnect: Boolean) {
        Log.v("SSE Closed", "reconnect? " + willReconnect);
    }



    private fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
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
                if (blazeMessage.action == ERROR_ACTION && blazeMessage.error.code == ErrorHandler.AUTHENTICATION) {
                    (app as FoneApplication).closeAndClear()
                }
            }
        } catch (e: GzipException) {

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
}