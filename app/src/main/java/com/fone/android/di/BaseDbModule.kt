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
    fun providesResendMessageDao(@DatabaseCategory(DatabaseCategoryEnum.BASE) db: FoneDatabase) = db.resendMessageDao()

}
