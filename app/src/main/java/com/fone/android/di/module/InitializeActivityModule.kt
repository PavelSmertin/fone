package com.fone.android.di.module

import com.fone.android.ui.landing.LoadingFragment
import com.fone.android.ui.landing.SetupNameFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class InitializeActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeNameFragment(): SetupNameFragment

    @ContributesAndroidInjector
    internal abstract fun contributeLoadingFragment(): LoadingFragment

}