package com.fone.android.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.fone.android.db.*
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.util.SINGLE_DB_THREAD
import com.fone.android.vo.Conversation
import com.fone.android.vo.ConversationItem
import com.fone.android.vo.Message
import com.fone.android.vo.Participant
import io.reactivex.Observable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository
@Inject
internal constructor(
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    private val appDatabase: FoneDatabase,
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    private val readAppDatabase: FoneDatabase,
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    private val messageDao: MessageDao,
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    private val readMessageDao: MessageDao,
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    private val conversationDao: ConversationDao,
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    private val readConversationDao: ConversationDao,
    private val participantDao: ParticipantDao
) {

    @SuppressLint("RestrictedApi")
    fun getMessages(conversationId: String) = MessageProvider.getMessages(conversationId, readAppDatabase)

    fun conversation(): LiveData<List<ConversationItem>> = readConversationDao.conversationList()

    fun insertConversation(conversation: Conversation, participants: List<Participant>) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            appDatabase.runInTransaction {
                conversationDao.insertConversation(conversation)
                participantDao.insertList(participants)
            }
        }
    }

    fun syncInsertConversation(conversation: Conversation, participants: List<Participant>) {
        appDatabase.runInTransaction {
            conversationDao.insertConversation(conversation)
            participantDao.insertList(participants)
        }
    }

    fun getConversationById(conversationId: String): LiveData<Conversation> =
        readConversationDao.getConversationById(conversationId)

    fun findConversationById(conversationId: String): Observable<Conversation> = Observable.just(conversationId).map {
        readConversationDao.findConversationById(conversationId)
    }

    fun searchConversationById(conversationId: String) = readConversationDao.searchConversationById(conversationId)

    fun findMessageById(messageId: String) = messageDao.findMessageById(messageId)

    fun saveDraft(conversationId: String, draft: String) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            conversationDao.saveDraft(conversationId, draft)
        }
    }

    fun getConversation(conversationId: String) = readConversationDao.getConversation(conversationId)

    fun indexUnread(conversationId: String) = readConversationDao.indexUnread(conversationId)

    fun getGroupParticipants(conversationId: String) = readAppDatabase.participantDao().getParticipants(conversationId)

    fun deleteConversationById(conversationId: String) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            conversationDao.deleteConversationById(conversationId)
        }
    }

    fun updateConversationPinTimeById(conversationId: String, pinTime: String?) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            conversationDao.updateConversationPinTimeById(conversationId, pinTime)
        }
    }

    fun findMessageIndex(conversationId: String, messageId: String) = readMessageDao.findMessageIndex(conversationId, messageId)




    fun insertMessage(message: Message) {
        messageDao.insert(message)
    }
}
