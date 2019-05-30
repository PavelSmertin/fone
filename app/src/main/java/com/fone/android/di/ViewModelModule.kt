package com.fone.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fone.android.ui.common.BottomSheetViewModel
import com.fone.android.ui.contacts.ContactViewModel
import com.fone.android.ui.conversation.ConversationViewModel
import com.fone.android.ui.home.ConversationListViewModel
import com.fone.android.ui.landing.MobileViewModel
import com.fone.android.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
internal abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ContactViewModel::class)
    internal abstract fun bindContactViewModel(contactViewModel: ContactViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationListViewModel::class)
    internal abstract fun bindConversationListViewModel(messageViewModel: ConversationListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel::class)
    internal abstract fun bindConversationViewModel(chatViewModel: ConversationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MobileViewModel::class)
    internal abstract fun bindMobileViewModel(mobileViewModel: MobileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BottomSheetViewModel::class)
    internal abstract fun bindBottomSheetViewModel(bottomSheetViewModel: BottomSheetViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
