package one.mixin.android.di.module

import com.fone.android.ui.conversation.ConversationFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ConversationActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeConversationFragment(): ConversationFragment
}