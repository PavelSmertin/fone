package com.fone.android.job


import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import java.util.concurrent.ConcurrentHashMap

class FoneJobManager(configuration: Configuration) : JobManager(configuration) {

    private val map: ConcurrentHashMap<String, FoneJob> by lazy {
        ConcurrentHashMap<String, FoneJob>()
    }

    fun saveJob(job: Job) {
        if (job is FoneJob) {
            map[job.jobId] = job
        }
    }

    fun removeJob(id: String) {
        map.remove(id)
    }

    fun cancelJobById(id: String) {
        findJobById(id)?.cancel()
    }

    fun findJobById(id: String): FoneJob? = map[id]

    fun cancelAllJob() {
        for (job in map.values) {
            job.cancel()
        }
        map.clear()
    }
}
