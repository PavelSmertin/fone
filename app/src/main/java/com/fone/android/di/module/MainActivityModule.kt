package one.mixin.android.di.module

import com.fone.android.ui.home.ConversationListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class MainActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeConversationListFragment(): ConversationListFragment

}
