package com.fone.android.job

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import dagger.android.AndroidInjection
import javax.inject.Inject

class MyJobService : FrameworkJobSchedulerService() {

    @Inject
    lateinit var jobManager: FoneJobManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun getJobManager(): JobManager {
        return jobManager
    }
}