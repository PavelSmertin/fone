package com.fone.android.di

import com.fone.android.job.MyJobService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceModule {
    @ContributesAndroidInjector internal abstract fun contributeMyJobService(): MyJobService
}