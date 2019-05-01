package com.fone.android.di


import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.ui.home.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import one.mixin.android.di.module.CommonModule
import one.mixin.android.di.module.ConversationActivityModule
import one.mixin.android.di.module.MainActivityModule


@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [(CommonModule::class), (MainActivityModule::class)])
    internal abstract fun contributeMain(): MainActivity

    @ContributesAndroidInjector(modules = [(CommonModule::class), (ConversationActivityModule::class)])
    internal abstract fun contributeConversation(): ConversationActivity

}