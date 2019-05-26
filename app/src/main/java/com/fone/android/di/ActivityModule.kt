package com.fone.android.di


import com.fone.android.di.module.ContactActivityModule
import com.fone.android.di.module.InitializeActivityModule
import com.fone.android.di.module.LandingActivityModule
import com.fone.android.ui.contacts.ContactsActivity
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.ui.home.MainActivity
import com.fone.android.ui.landing.InitializeActivity
import com.fone.android.ui.landing.LandingActivity
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

    @ContributesAndroidInjector(modules = [(LandingActivityModule::class)])
    internal abstract fun contributeLanding(): LandingActivity

    @ContributesAndroidInjector(modules = [(InitializeActivityModule::class)])
    internal abstract fun contributeSetupName(): InitializeActivity

    @ContributesAndroidInjector(modules = [(CommonModule::class), (ContactActivityModule::class)])
    internal abstract fun contributeContacts(): ContactsActivity
}