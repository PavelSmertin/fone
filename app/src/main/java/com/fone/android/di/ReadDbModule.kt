package com.fone.android.di

import android.app.Application
import com.fone.android.db.FoneDatabase
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class ReadDbModule {

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    fun provideReadDb(app: Application) = FoneDatabase.getReadDatabase(app)

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    fun provideConversationDao(@DatabaseCategory(DatabaseCategoryEnum.READ) db: FoneDatabase) = db.conversationDao()

    @Singleton
    @Provides
    @DatabaseCategory(DatabaseCategoryEnum.READ)
    fun provideMessageDao(@DatabaseCategory(DatabaseCategoryEnum.READ) db: FoneDatabase) = db.messageDao()
}
