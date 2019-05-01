package com.fone.android.ui.conversation

import android.app.Activity
import android.app.NotificationManager
import androidx.annotation.WorkerThread
import androidx.collection.ArraySet
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.fone.android.Constants.PAGE_SIZE
import com.fone.android.FoneApplication
import com.fone.android.extension.nowInUtc
import com.fone.android.repository.ConversationRepository
import com.fone.android.repository.UserRepository
import com.fone.android.vo.*
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class ConversationViewModel
@Inject
internal constructor(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getMessages(id: String, initialLoadKey: Int = 0): LiveData<PagedList<MessageItem>> {
        return LivePagedListBuilder(conversationRepository.getMessages(id), PagedList.Config.Builder()
            .setPrefetchDistance(PAGE_SIZE * 2)
            .setPageSize(PAGE_SIZE)
            .setEnablePlaceholders(true)
            .build())
            .setInitialLoadKey(initialLoadKey)
            .build()
    }

    fun indexUnread(conversationId: String) =
        conversationRepository.indexUnread(conversationId)

    fun searchConversationById(id: String) =
        conversationRepository.searchConversationById(id)
            .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())!!



    fun saveDraft(conversationId: String, text: String) =
        conversationRepository.saveDraft(conversationId, text)



    fun sendTextMessage(conversationId: String, sender: User, content: String, isPlain: Boolean) {
        val category = if (isPlain) MessageCategory.PLAIN_TEXT.name else MessageCategory.SIGNAL_TEXT.name
        val message = createMessage(UUID.randomUUID().toString(), conversationId,
            sender.userId, category, content.trim(), nowInUtc(), MessageStatus.SENDING)
    }

    fun sendReplyMessage(conversationId: String, sender: User, content: String, replyMessage: MessageItem, isPlain: Boolean) {
        val category = if (isPlain) MessageCategory.PLAIN_TEXT.name else MessageCategory.SIGNAL_TEXT.name
        val message = createReplyMessage(UUID.randomUUID().toString(), conversationId,
            sender.userId, category, content.trim(), nowInUtc(), MessageStatus.SENDING, replyMessage.messageId, Gson().toJson(
                QuoteMessageItem(replyMessage)
            ))
    }

    fun sendFordMessage(conversationId: String, sender: User, id: String, isPlain: Boolean) =
        Flowable.just(id).observeOn(Schedulers.io()).map {
            conversationRepository.findMessageById(id)?.let { message ->
                when {
                }
                return@let 1
            }
        }.observeOn(AndroidSchedulers.mainThread())!!


    @WorkerThread
    fun initConversation(conversationId: String, recipient: User, sender: User) {
        val createdAt = nowInUtc()
        val conversation = createConversation(conversationId, ConversationCategory.CONTACT.name,
            recipient.userId, ConversationStatus.START.ordinal)
        val participants = arrayListOf(
            Participant(conversationId, sender.userId, "", createdAt),
            Participant(conversationId, recipient.userId, "", createdAt)
        )
        conversationRepository.syncInsertConversation(conversation, participants)
    }

    fun getUserById(userId: String) =
        Observable.just(userId).subscribeOn(Schedulers.io())
            .map { userRepository.getUserById(it) }.observeOn(AndroidSchedulers.mainThread())!!

    fun cancel(id: String) {

    }


    fun markMessageRead(conversationId: String, accountId: String) {
    }


    private val notificationManager: NotificationManager by lazy {
        FoneApplication.appContext.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun deleteMessages(set: ArraySet<MessageItem>) {
    }

    fun findMessageIndexSync(conversationId: String, messageId: String) =
        conversationRepository.findMessageIndex(conversationId, messageId)

    fun findMessageIndex(conversationId: String, messageId: String) =
        Observable.just(1).map {
            conversationRepository.findMessageIndex(conversationId, messageId)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!


}