package com.fone.android.websocket

import android.app.Application
import android.util.Log
import com.fone.android.Constants
import com.fone.android.db.*
import com.fone.android.job.FoneJobManager
import com.fone.android.util.Session
import com.tylerjroach.eventsource.EventSource
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch


class ChatWebSocketSSE(
    val app: Application,
    val conversationDao: ConversationDao,
    val messageDao: MessageDao,
    val offsetDao: OffsetDao,
    val floodMessageDao: FloodMessageDao,
    val jobManager: FoneJobManager,
    val jobDao: JobDao
) {

    var connected: Boolean = false
    private val transactions = ConcurrentHashMap<String, WebSocketTransaction>()
    private var connectTimer: Disposable? = null

    var sseHandler: SSEHandler? = SSEHandler(
                                                app,
                                                messageDao,
                                                offsetDao,
                                                floodMessageDao,
                                                jobDao
    )
    var eventSource: EventSource? = null
    var extraHeaderParameters: Map<String, String>? = mapOf( "Authorization" to "Bearer ${Session.getToken()}")

    companion object {
        val TAG = ChatWebSocketSSE::class.java.simpleName
    }

    init {
        connected = false
    }

    @Synchronized
    fun startEventSource() {
        Log.v("SSE startEventSource", extraHeaderParameters.toString())

        eventSource = EventSource.Builder(Constants.API.SSE_URL)
            .eventHandler(sseHandler)
            .headers(extraHeaderParameters)
            .build()
        eventSource!!.connect()

        connected = true
        connectTimer?.dispose()
    }

    @Synchronized
    fun stopEventSource() {
        if (eventSource == null)
            eventSource!!.close()
        sseHandler = null


        transactions.clear()
        connectTimer?.dispose()
        connected = false

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
//        if (client != null && connected) {
//            transactions[blazeMessage.id] = transaction
//            val result = client!!.send(gson.toJson(blazeMessage).gzip())
//            if (result) {
//                latch.await(5, TimeUnit.SECONDS)
//            }
//        } else {
//            Log.e(TAG, "WebSocket not connect")
//        }
        return bm
    }


}
