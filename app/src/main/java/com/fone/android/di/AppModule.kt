package com.fone.android.di


import android.app.Application
import android.content.ContentResolver
import android.provider.Settings
import com.birbit.android.jobqueue.config.Configuration
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import com.fone.android.BuildConfig
import com.fone.android.Constants.API.URL
import com.fone.android.FoneApplication
import com.fone.android.api.NetworkException
import com.fone.android.api.ServerErrorException
import com.fone.android.api.service.*
import com.fone.android.db.*
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.extension.networkConnected
import com.fone.android.job.BaseJob
import com.fone.android.job.FoneJobManager
import com.fone.android.job.JobLogger
import com.fone.android.job.MyJobService
import com.fone.android.util.LiveDataCallAdapterFactory
import com.fone.android.util.Session
import com.fone.android.vo.LinkState
import com.fone.android.websocket.ChatWebSocket
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.SessionProvider
import okhttp3.internal.http2.Header
import okhttp3.logging.HttpLoggingInterceptor
import one.mixin.android.job.JobNetworkUtil
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.math.abs

@Module(includes = [(ViewModelModule::class), (BaseDbModule::class), (ReadDbModule::class)])
internal class AppModule {

    private val LOCALE = Locale.getDefault().language + "-" + Locale.getDefault().country
    private val API_UA = "Fone/" + BuildConfig.VERSION_NAME +
        " (Android " + android.os.Build.VERSION.RELEASE + "; " + android.os.Build.FINGERPRINT + "; " + LOCALE + ")"

    private fun getDeviceId(resolver: ContentResolver): String {
        var deviceId = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID)
        return UUID.nameUUIDFromBytes(deviceId.toByteArray()).toString()
    }

    @Singleton
    @Provides
    fun provideOkHttp(resolver: ContentResolver): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addNetworkInterceptor(logging)
        }
        builder.connectTimeout(10, TimeUnit.SECONDS)
        builder.writeTimeout(10, TimeUnit.SECONDS)
        builder.readTimeout(10, TimeUnit.SECONDS)
        builder.pingInterval(15, TimeUnit.SECONDS)
        builder.retryOnConnectionFailure(false)
        builder.sessionProvider(object : SessionProvider {
            override fun getSession(request: Request): String {
                return "Authorization: Bearer ${Session.signToken(Session.getAccount(), request)}"
            }

            override fun getSessionHeader(request: Request): Header {
                return Header("Authorization", "Bearer ${Session.signToken(Session.getAccount(), request)}")
            }
        })

        builder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", API_UA)
                .addHeader("Accept-Language", Locale.getDefault().language)
                .addHeader("Mixin-Device-Id", getDeviceId(resolver))
                .build()
            if (FoneApplication.appContext.networkConnected()) {
                val response = try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    if (e.message?.contains("502") == true) {
                        throw ServerErrorException(502)
                    } else throw e
                }

                if (FoneApplication.get().onlining.get()) {
                    response.header("X-Server-Time")?.toLong()?.let { serverTime ->
                        if (abs(serverTime / 1000000 - System.currentTimeMillis()) >= 600000L) {
                            FoneApplication.get().gotoTimeWrong(serverTime)
                        }
                    }
                }

                if (!response.isSuccessful) {
                    val code = response.code()
                    if (code in 500..599) {
                        throw ServerErrorException(code)
                    }
                }
                return@addInterceptor response
            } else {
                throw NetworkException()
            }
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideHttpService(okHttp: OkHttpClient): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp)
        return builder.build()
    }


    @Singleton
    @Provides
    fun provideAccountService(retrofit: Retrofit) = retrofit.create(AccountService::class.java) as AccountService

    @Singleton
    @Provides
    fun provideUserService(retrofit: Retrofit) = retrofit.create(UserService::class.java) as UserService

    @Singleton
    @Provides
    fun provideContactService(retrofit: Retrofit) = retrofit.create(ContactService::class.java) as ContactService


    @Singleton
    @Provides
    fun provideConversationService(retrofit: Retrofit) =
        retrofit.create(ConversationService::class.java) as ConversationService

    @Singleton
    @Provides
    fun provideMessageService(retrofit: Retrofit) = retrofit.create(MessageService::class.java) as MessageService

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit) =
        retrofit.create(AuthorizationService::class.java) as AuthorizationService

    @Singleton
    @Provides
    fun provideContentResolver(app: Application) = app.contentResolver as ContentResolver

    @Provides
    @Singleton
    fun provideJobNetworkUtil(app: Application) =
        JobNetworkUtil(app.applicationContext)

    @Suppress("INACCESSIBLE_TYPE")
    @Provides
    @Singleton
    fun jobManager(app: Application, appComponent: AppComponent, jobNetworkUtil: JobNetworkUtil): FoneJobManager {
        val builder = Configuration.Builder(app)
            .consumerKeepAlive(20)
            .resetDelaysOnRestart()
            .maxConsumerCount(6)
            .minConsumerCount(2)
            .injector { job ->
                if (job is BaseJob) {
                    job.inject(appComponent)
                }
            }
            .customLogger(JobLogger())
            .networkUtil(jobNetworkUtil)
        builder.scheduler(
            FrameworkJobSchedulerService
                .createSchedulerFor(app.applicationContext, MyJobService::class.java))
        return FoneJobManager(builder.build())
    }

    @Provides
    @Singleton
    fun provideLinkState() = LinkState()

    @Provides
    @Singleton
    fun provideChatWebSocket(
        okHttp: OkHttpClient,
        app: Application,
        @DatabaseCategory(DatabaseCategoryEnum.BASE)
        conversationDao: ConversationDao,
        @DatabaseCategory(DatabaseCategoryEnum.BASE)
        messageDao: MessageDao,
        offsetDao: OffsetDao,
        floodMessageDao: FloodMessageDao,
        jobManager: FoneJobManager,
        linkState: LinkState,
        jobDao: JobDao
    ): ChatWebSocket =
        ChatWebSocket(okHttp, app, conversationDao, messageDao, offsetDao, floodMessageDao, jobManager, linkState, jobDao)

}