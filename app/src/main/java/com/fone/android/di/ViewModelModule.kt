package com.fone.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fone.android.ui.conversation.ConversationViewModel
import com.fone.android.ui.home.ConversationListViewModel
import com.fone.android.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
internal abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ConversationListViewModel::class)
    internal abstract fun bindConversationListViewModel(messageViewModel: ConversationListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel::class)
    internal abstract fun bindConversationViewModel(chatViewModel: ConversationViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
