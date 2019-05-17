package com.fone.android.di

import android.app.Application
import com.fone.android.FoneApplication
import com.fone.android.job.BaseJob
import com.fone.android.job.Injector
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule


import javax.inject.Singleton

@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class),
    (AppModule::class),
    (ActivityModule::class)])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(mixApp: FoneApplication)

    fun inject(baseJob: BaseJob)

    fun inject(injector: Injector)
}
