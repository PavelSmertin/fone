package com.fone.android.job

import com.birbit.android.jobqueue.Params
import com.fone.android.vo.Conversation
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