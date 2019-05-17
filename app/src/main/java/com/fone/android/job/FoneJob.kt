package com.fone.android.job

import android.util.Log
import com.birbit.android.jobqueue.Params
import com.fone.android.Constants
import com.fone.android.api.NetworkException
import com.fone.android.api.WebSocketException
import com.fone.android.util.ErrorHandler
import com.fone.android.util.Session
import com.fone.android.vo.Conversation
import com.fone.android.vo.MessageStatus
import com.fone.android.vo.createAckJob
import com.fone.android.websocket.BlazeAckMessage
import com.fone.android.websocket.BlazeMessage
import com.fone.android.websocket.CREATE_SESSION_MESSAGE
import timber.log.Timber

abstract class FoneJob(params: Params, val jobId: String) : BaseJob(params) {

    protected var isCancel = false

    companion object {
        private const val serialVersionUID = 1L
        val TAG = FoneJob::class.java.simpleName
    }

    override fun onAdded() {
    }

    protected fun removeJob() {
        jobManager.removeJob(jobId)
    }

    override fun shouldRetry(throwable: Throwable): Boolean {
        return if (isCancel) {
            Timber.d("cancel")
            false
        } else {
            Timber.d("no cancel")
            super.shouldRetry(throwable)
        }
    }

    protected fun makeMessageStatus(status: String, messageId: String) {
        val curStatus = messageDao.findMessageStatusById(messageId)
        if (curStatus != null && curStatus != MessageStatus.READ.name) {
            messageDao.updateMessageStatus(status, messageId)
        }
        sendSessionAck(status, messageId)
    }


    protected fun deliver(blazeMessage: BlazeMessage): Boolean {
        val bm = chatWebSocket.sendMessage(blazeMessage)
        if (bm == null) {
            Thread.sleep(Constants.SLEEP_MILLIS)
            throw WebSocketException()
        } else if (bm.error != null) {
            if (bm.error.code == ErrorHandler.FORBIDDEN) {
                return true
            } else {
                Log.e(TAG, bm.toString())
                Thread.sleep(Constants.SLEEP_MILLIS)
                throw NetworkException()
            }
        }
        return true
    }


    private fun sendSessionAck(status: String, messageId: String) {
        val extensionSessionId = Session.getExtensionSessionId()
        extensionSessionId?.let {
            jobDao.insert(createAckJob(CREATE_SESSION_MESSAGE, BlazeAckMessage(messageId, status)))
        }
    }

    protected fun requestCreateConversation(conversation: Conversation) {
//        if (conversation.status != ConversationStatus.SUCCESS.ordinal) {
//            val participantRequest = arrayListOf(ParticipantRequest(conversation.ownerId!!, ""))
//            val request = ConversationRequest(conversationId = conversation.conversationId,
//                category = conversation.category, participants = participantRequest)
//            val response = conversationApi.create(request).execute().body()
//            if (response != null && response.isSuccess && response.data != null && !isCancel) {
//                conversationDao
//                    .updateConversationStatusById(conversation.conversationId, ConversationStatus.SUCCESS.ordinal)
//            } else {
//                throw Exception("Create Conversation Exception")
//            }
//        }
    }

    internal abstract fun cancel()
}