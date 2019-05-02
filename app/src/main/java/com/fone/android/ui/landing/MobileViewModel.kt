package com.fone.android.ui.landing

import androidx.lifecycle.ViewModel
import com.fone.android.extension.nowInUtc
import com.fone.android.repository.ConversationRepository
import com.fone.android.repository.UserRepository
import com.fone.android.vo.*
import javax.inject.Inject

class MobileViewModel @Inject internal
constructor(
    private val userRepository: UserRepository,
    private val messageRepository: ConversationRepository
) : ViewModel() {

    fun insertUser(user: User) {
        userRepository.upsert(user)
    }

    fun initialConversation() {
        var conversation = Conversation(
            "1",
            "1",
            ConversationCategory.CONTACT.name,
            "Давай поговорим",
            "https://placeimg.com/140/140/any",
            null,
            null,
            "",
            nowInUtc(),
            null,
            null,
            null,
            0,
            ConversationStatus.SUCCESS.ordinal,
            null)
        messageRepository.insertConversation(conversation, mutableListOf<Participant>())
    }

}