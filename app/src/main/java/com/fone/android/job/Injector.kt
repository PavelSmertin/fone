package com.fone.android.job

import com.fone.android.FoneApplication
import com.fone.android.api.service.ConversationService
import com.fone.android.api.service.UserService
import com.fone.android.db.*
import com.fone.android.di.Injectable
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.websocket.ChatWebSocketSSE
import javax.inject.Inject

open class Injector : Injectable {

    @Inject
    lateinit var jobManager: FoneJobManager
    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var messageDao: MessageDao
    @Inject
    lateinit var messageHistoryDao: MessageHistoryDao
    @Inject
    lateinit var userDao: UserDao
    @Inject
    lateinit var jobDao: JobDao
    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var conversationDao: ConversationDao
    @Inject
    lateinit var participantDao: ParticipantDao
    @Inject
    lateinit var snapshotDao: SnapshotDao
    @Inject
    lateinit var assetDao: AssetDao
    @Inject
    lateinit var chatWebSocket: ChatWebSocketSSE
    @Inject
    lateinit var stickerDao: StickerDao
    @Inject
    lateinit var resendMessageDao: ResendMessageDao
    @Inject
    lateinit var userApi: UserService
    @Inject
    lateinit var conversationService: ConversationService

    init {
        FoneApplication.get().appComponent.inject(this)
    }

}