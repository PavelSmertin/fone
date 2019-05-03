package com.fone.android.job

import com.birbit.android.jobqueue.Params
import com.fone.android.vo.Message


open class SendMessageJob(
    val message: Message,
    messagePriority: Int = PRIORITY_SEND_MESSAGE
) : FoneJob(
    Params(messagePriority).addTags(message.id).groupBy("send_message_group")
    .requireWebSocketConnected().persist(), message.id) {

    companion object {
        private const val serialVersionUID = 1L
    }

    override fun cancel() {
        isCancel = true
        removeJob()
    }

    override fun onAdded() {
        super.onAdded()

        val conversation = conversationDao.findConversationById(message.conversationId)
        if (conversation != null) {
            messageDao.insert(message)
        } else {
            //Bugsnag.notify(Throwable("Insert failed, no conversation $alreadyExistMessage"))
        }

    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        removeJob()
    }

    override fun onRun() {
        jobManager.saveJob(this)
        sendPlainMessage()
        removeJob()
    }

    private fun sendPlainMessage() {
        val conversation = conversationDao.getConversation(message.conversationId) ?: return
        requestCreateConversation(conversation)
//        var content = message.content
//        if (message.category == MessageCategory.PLAIN_TEXT.name) {
//            if (message.content != null) {
//                content = Base64.encodeBytes(message.content!!.toByteArray())
//            }
//        }
//        val blazeParam = BlazeMessageParam(message.conversationId, recipientId,
//            message.id, message.category, content, quote_message_id = message.quoteMessageId)
//        val blazeMessage = createParamBlazeMessage(blazeParam)
//
//        deliver(blazeMessage)
    }

}