package com.fone.android.di.module

import com.fone.android.ui.landing.MobileFragment
import com.fone.android.ui.landing.VerificationFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class LandingActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMobileFragment(): MobileFragment

    @ContributesAndroidInjector
    internal abstract fun contributeVerificationFragment(): VerificationFragment
}
