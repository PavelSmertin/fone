package com.fone.android.job

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import com.fone.android.api.*
import com.fone.android.db.ConversationDao
import com.fone.android.db.FoneDatabase
import com.fone.android.db.MessageDao
import com.fone.android.di.AppComponent
import com.fone.android.di.Injectable
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum

import java.net.SocketTimeoutException
import javax.inject.Inject

abstract class BaseJob(params: Params) : Job(params), Injectable {

    @Inject
    @Transient
    lateinit var jobManager: FoneJobManager

    @Transient
    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var appDatabase: FoneDatabase

    @Inject
    @Transient
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var conversationDao: ConversationDao

    @Inject
    @Transient
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var messageDao: MessageDao


    open fun shouldRetry(throwable: Throwable): Boolean {
        if (throwable is SocketTimeoutException) {
            return true
        }
        return (throwable as? ServerErrorException)?.shouldRetry()
            ?: ((throwable as? ClientErrorException)?.shouldRetry()
                ?: ((throwable as? NetworkException)?.shouldRetry()
                    ?: ((throwable as? WebSocketException)?.shouldRetry()
                        ?: ((throwable as? LocalJobException)?.shouldRetry() ?: false))))
    }

    fun inject(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    public override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
//        if (runCount >= 100) {
//            val metaData = MetaData()
//            metaData.addToTab("Job", "shouldReRunOnThrowable", "Retry max count:$runCount")
//            Bugsnag.notify(throwable, metaData)
//        }
        return if (shouldRetry(throwable)) {
            RetryConstraint.RETRY
        } else {
            RetryConstraint.CANCEL
        }
    }

    override fun onAdded() {
    }

//    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
//        if (cancelReason == CancelReason.REACHED_RETRY_LIMIT) {
//            throwable?.let {
//                val metaData = MetaData()
//                metaData.addToTab("Job", "CancelReason", "REACHED_RETRY_LIMIT")
//                Bugsnag.notify(it, metaData)
//            }
//        }
//    }

    override fun getRetryLimit(): Int {
        return Integer.MAX_VALUE
    }

    companion object {
        private const val serialVersionUID = 1L

        const val PRIORITY_UI_HIGH = 20
        const val PRIORITY_SEND_MESSAGE = 18
        const val PRIORITY_SEND_ATTACHMENT_MESSAGE = 17
        const val PRIORITY_SEND_SESSION_MESSAGE = 18
        const val PRIORITY_RECEIVE_MESSAGE = 15
        const val PRIORITY_BACKGROUND = 10
        const val PRIORITY_DELIVERED_ACK_MESSAGE = 7
        const val PRIORITY_ACK_MESSAGE = 5
    }
}