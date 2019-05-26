package com.fone.android.di.module

import com.fone.android.ui.contacts.AddPeopleFragment
import com.fone.android.ui.contacts.ContactBottomSheetDialog
import com.fone.android.ui.contacts.ContactsFragment
import com.fone.android.ui.contacts.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContactActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    internal abstract fun contributeContactBottomSheetDialogFragment(): ContactBottomSheetDialog

    @ContributesAndroidInjector
    internal abstract fun contributeAddPeopleFragment(): AddPeopleFragment

}