package com.fone.android.di

import android.app.Application
import com.fone.android.db.FoneDatabase
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class BaseDbModule {

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    fun provideDb(app: Application) = FoneDatabase.getDatabase(app)

    @Singleton
    @Provides
    fun provideUserDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.userDao()

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    fun provideConversationDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.conversationDao()

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.BASE)
    fun provideMessageDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.messageDao()

    @Singleton
    @Provides
    fun provideParticipantDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.participantDao()

    @Singleton
    @Provides
    fun provideOffsetDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.offsetDao()

    @Singleton
    @Provides
    fun provideAssetDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.assetDao()

    @Singleton
    @Provides
    fun provideSnapshotDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.snapshotDao()

    @Singleton
    @Provides
    fun provideMessageHistoryDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.messageHistoryDao()

    @Singleton
    @Provides
    fun provideSentSenderKeyDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.sentSenderKeyDao()

    @Singleton
    @Provides
    fun provideStickerAlbumDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.stickerAlbumDao()

    @Singleton
    @Provides
    fun provideStickerDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.stickerDao()

    @Singleton
    @Provides
    fun provideHyperlinkDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.hyperlinkDao()

    @Singleton
    @Provides
    fun providesAppDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.appDao()

    @Singleton
    @Provides
    fun providesFloodMessageDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.floodMessageDao()

    @Singleton
    @Provides
    fun providesJobDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.jobDao()

    @Singleton
    @Provides
    fun providesAddressDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.addressDao()

    @Singleton
    @Provides
    fun providesResendMessageDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.resendMessageDao()

    @Singleton
    @Provides
    fun providesStickerRelationshipDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.stickerRelationshipDao()

    @Singleton
    @Provides
    fun providesHotAssetDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.topAssetDao()
}
