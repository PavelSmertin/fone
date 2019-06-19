package com.fone.android


import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.fone.android.db.FoneDatabase
import com.fone.android.di.AppComponent
import com.fone.android.di.AppInjector
import com.fone.android.extension.clear
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.extension.putBoolean
import com.fone.android.job.BlazeMessageService
import com.fone.android.job.FoneJobManager
import com.fone.android.ui.landing.LandingActivity
import com.fone.android.util.Session
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.reactivex.plugins.RxJavaPlugins
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.uiThread
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class FoneApplication : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

//    @Inject
//    lateinit var mixinWorkerFactory: MixinWorkerFactory
//
    @Inject
    lateinit var jobManager: FoneJobManager

    lateinit var appComponent: AppComponent

    companion object {
        lateinit var appContext: Context
        @JvmField
        var conversationId: String? = null

        fun get(): FoneApplication = appContext as FoneApplication
    }

    override fun onCreate() {
        super.onCreate()
        init()
        //FirebaseApp.initializeApp(this)
        FoneApplication.appContext = applicationContext
        AndroidThreeTen.init(this)
        appComponent = AppInjector.init(this)
//        val wmConfig = Configuration.Builder().setWorkerFactory(mixinWorkerFactory).build()
//        WorkManager.initialize(this, wmConfig)
        RxJavaPlugins.setErrorHandler {}
    }

    private fun init() {
//        Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)
//        if (BuildConfig.DEBUG) {
//            Stetho.initializeWithDefaults(this)
//            Timber.plant(Timber.DebugTree())
//        }
    }

    fun inject() {
        appComponent = AppInjector.inject(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? = dispatchingAndroidInjector
    override fun serviceInjector(): DispatchingAndroidInjector<Service>? = dispatchingServiceInjector

    var onlining = AtomicBoolean(false)



    fun closeAndClear(toLanding: Boolean = true) {
        if (onlining.compareAndSet(true, false)) {
            BlazeMessageService.stopService(this)
//            CallService.disconnect(this)
            notificationManager.cancelAll()
            Session.clearAccount()
            defaultSharedPreferences.clear()
            defaultSharedPreferences.putBoolean(Constants.Account.PREF_LOGOUT_COMPLETE, false)
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            if (toLanding) {
                doAsync {
                    clearData()

                    uiThread {
                        inject()
                        LandingActivity.show(this@FoneApplication)
                    }
                }
            } else {
                clearData()
                inject()
            }
        }
    }

    fun clearData() {
        jobManager.cancelAllJob()
        jobManager.clear()
        FoneDatabase.getDatabase(this).clearAllTables()
        defaultSharedPreferences.putBoolean(Constants.Account.PREF_LOGOUT_COMPLETE, true)
    }

    fun gotoTimeWrong(serverTime: Long) {
        if (onlining.compareAndSet(true, false)) {
            BlazeMessageService.stopService(this)
            notificationManager.cancelAll()
            defaultSharedPreferences.putBoolean(Constants.Account.PREF_WRONG_TIME, true)
        }
    }
}