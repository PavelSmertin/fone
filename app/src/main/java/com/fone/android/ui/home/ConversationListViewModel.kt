package com.fone.android.ui.home


import androidx.lifecycle.ViewModel
import com.fone.android.api.request.ConversationRequest
import com.fone.android.api.request.ParticipantRequest
import com.fone.android.extension.nowInUtc
import com.fone.android.repository.ConversationRepository
import com.fone.android.vo.Conversation
import com.fone.android.vo.ConversationStatus
import com.fone.android.vo.Participant
import javax.inject.Inject

class ConversationListViewModel @Inject
internal constructor(
    private val messageRepository: ConversationRepository
    //private val jobManager: MixinJobManager
) : ViewModel() {
    var conversations = messageRepository.conversation()

//    private val mobileViewModel: MobileViewModel by lazy {
//        ViewModelProviders.of(this, viewModelFactory).get(MobileViewModel::class.java)
//    }

    fun createGroupConversation(conversationId: String) {
        val c = messageRepository.getConversation(conversationId)
        c?.let {
            val participants = messageRepository.getGroupParticipants(conversationId)
            val mutableList = mutableListOf<Participant>()
            val createAt = nowInUtc()
            participants.mapTo(mutableList) { Participant(conversationId, it.userId, "", createAt) }
            val conversation = Conversation(c.conversationId, c.ownerId, c.category, c.name, c.iconUrl,
                c.announcement, null, c.payType, createAt, null, null,
                null, 0, ConversationStatus.START.ordinal, null)
            messageRepository.insertConversation(conversation, mutableList)

            val participantRequestList = mutableListOf<ParticipantRequest>()
            mutableList.mapTo(participantRequestList) { ParticipantRequest(it.userId, it.role) }
            val request = ConversationRequest(conversationId, it.category!!, it.name, it.iconUrl,
                it.announcement, participantRequestList)
            //jobManager.addJobInBackground(ConversationJob(request, type = TYPE_CREATE))
        }
    }

    fun deleteConversation(conversationId: String) {
        messageRepository.deleteConversationById(conversationId)
    }

    fun updateConversationPinTimeById(conversationId: String, pinTime: String?) {
        messageRepository.updateConversationPinTimeById(conversationId, pinTime)
    }
}