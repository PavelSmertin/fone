package com.fone.android.job

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.InvalidationTracker
import com.birbit.android.jobqueue.network.NetworkEventProvider
import com.birbit.android.jobqueue.network.NetworkUtil
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.db.*
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.extension.networkConnected
import com.fone.android.extension.supportsOreo
import com.fone.android.receiver.ExitBroadcastReceiver
import com.fone.android.ui.home.MainActivity
import com.fone.android.util.GsonHelper
import com.fone.android.util.Session
import com.fone.android.websocket.*
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import one.mixin.android.job.JobNetworkUtil
import org.jetbrains.anko.notificationManager
import java.util.concurrent.Executors
import javax.inject.Inject

class BlazeMessageService : Service(), NetworkEventProvider.Listener {

    companion object {
        val TAG = BlazeMessageService::class.java.simpleName
        const val CHANNEL_NODE = "channel_node"
        const val FOREGROUND_ID = 666666
        const val ACTION_TO_BACKGROUND = "action_to_background"
        const val ACTION_ACTIVITY_RESUME = "action_activity_resume"
        const val ACTION_ACTIVITY_PAUSE = "action_activity_pause"

        fun startService(ctx: Context, action: String? = null) {
            val intent = Intent(ctx, BlazeMessageService::class.java).apply {
                this.action = action
            }
            ContextCompat.startForegroundService(ctx, intent)
        }

        fun stopService(ctx: Context) {
            val intent = Intent(ctx, BlazeMessageService::class.java)
            ctx.stopService(intent)
        }
    }

    @Inject
    lateinit var networkUtil: JobNetworkUtil
    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var database: FoneDatabase
    @Inject
    lateinit var webSocket: ChatWebSocketSSE
    @Inject
    lateinit var floodMessageDao: FloodMessageDao
    @Inject
    lateinit var jobDao: JobDao
    @Inject
    lateinit var jobManager: FoneJobManager

    private val accountId = Session.getAccountId()
    private val gson = GsonHelper.customGson

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {

        AndroidInjection.inject(this)
        super.onCreate()
        webSocket.startEventSource()
        startAckJob()
        startFloodJob()
        networkUtil.setListener(this)
        Log.v("SSE BlazeMessageService", "True")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY
        when (ACTION_TO_BACKGROUND) {
            intent.action -> {
                stopForeground(true)
                return START_STICKY
            }
        }
        setForegroundIfNecessary()
        Log.v("SSE onStartCommand", "True")

        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        stopAckJob()
        stopFloodJob()
        webSocket.stopEventSource()
    }

    override fun onNetworkChange(networkStatus: Int) {
        if (networkStatus != NetworkUtil.DISCONNECTED && FoneApplication.get().onlining.get()) {
            webSocket.startEventSource()
        }
    }

    @SuppressLint("NewApi")
    private fun setForegroundIfNecessary() {
        val exitIntent = Intent(this, ExitBroadcastReceiver::class.java).apply {
            action = ACTION_TO_BACKGROUND
        }
        val exitPendingIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_NODE)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.background_connection_enabled))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setWhen(0)
            .setDefaults(0)
            .setSound(null)
            .setDefaults(0)
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(this, R.color.gray_light))
            .setSmallIcon(R.drawable.ic_send_white_24dp)
            .addAction(R.drawable.ic_close_black_24dp, getString(R.string.exit), exitPendingIntent)

        val pendingIntent = PendingIntent.getActivity(this, 0, MainActivity.getSingleIntent(this), 0)
        builder.setContentIntent(pendingIntent)

        supportsOreo {
            val channel = NotificationChannel(CHANNEL_NODE,
                FoneApplication.get().getString(R.string.notification_node), NotificationManager.IMPORTANCE_LOW)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setSound(null, null)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(FOREGROUND_ID, builder.build())
    }

    private val ackThread by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    private fun startAckJob() {
        database.invalidationTracker.addObserver(ackObserver)
    }

    private fun stopAckJob() {
        database.invalidationTracker.removeObserver(ackObserver)
        ackJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
    }

    private val ackObserver =
        object : InvalidationTracker.Observer("jobs") {
            override fun onInvalidated(tables: MutableSet<String>) {
                runAckJob()
            }
        }

    @Synchronized
    private fun runAckJob() {
        if (ackJob?.isActive == true || !networkConnected()) {
            return
        }
        ackJob = GlobalScope.launch(ackThread) {
            ackJobBlock()
            Session.getExtensionSessionId()?.let {
                ackSessionJobBlock()
                syncMessageStatusToExtension(it)
            }
        }
    }

    private var ackJob: Job? = null

    private suspend fun ackJobBlock() {
        jobDao.findAckJobsDeferred().await()?.let { list ->
            if (list.isNotEmpty()) {
                list.map { gson.fromJson(it.blazeMessage, BlazeAckMessage::class.java) }.let {
                    try {
                        deliver(createAckListParamBlazeMessage(it)).let {
                            jobDao.deleteList(list)
                        }
                    } catch (e: Exception) {
                        runAckJob()
                    } finally {
                        ackJob = null
                    }
                }
            }
        }
    }

    private suspend fun ackSessionJobBlock() {
        jobDao.findSessionAckJobsDeferred().await()?.let { list ->
            if (list.isNotEmpty()) {
                list.map { gson.fromJson(it.blazeMessage, BlazeAckMessage::class.java) }.let {
                    try {
                        deliver(createAckSessionListParamBlazeMessage(it)).let {
                            jobDao.deleteList(list)
                        }
                    } catch (e: Exception) {
                        runAckJob()
                    } finally {
                        ackJob = null
                    }
                }
            }
        }
    }

    private suspend fun syncMessageStatusToExtension(sessionId: String) {
//        jobDao.findCreatePlainSessionJobsDeferred().await()?.let { list ->
//            if (list.isNotEmpty()) {
//                list.map { gson.fromJson(it.blazeMessage, BlazeAckMessage::class.java) }.let {
//                    try {
//                        val plainText = gson.toJson(TransferPlainAckData(
//                            action = PlainDataAction.ACKNOWLEDGE_MESSAGE_RECEIPTS.name,
//                            messages = it
//                        ))
//                        val encoded = Base64.encodeBytes(plainText.toByteArray())
//                        val bm = createParamSessionMessage(createPlainJsonParam(accountId!!, accountId, encoded, sessionId))
//                        jobManager.addJobInBackground(SendSessionStatusMessageJob(bm))
//                        jobDao.deleteList(list)
//                    } catch (e: Exception) {
//                        runAckJob()
//                    } finally {
//                        ackJob = null
//                    }
//                }
//            }
//        }
    }

    private val floodThread by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

//    private val messageDecrypt by lazy { DecryptMessage() }
//    private val callMessageDecrypt by lazy { DecryptCallMessage(callState) }
//    private val sessionMessageDecrypt by lazy { DecryptSessionMessage() }

    private fun startFloodJob() {
        database.invalidationTracker.addObserver(floodObserver)
    }

    private fun stopFloodJob() {
        database.invalidationTracker.removeObserver(floodObserver)
        floodJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
    }

    private val floodObserver =
        object : InvalidationTracker.Observer("flood_messages") {
            override fun onInvalidated(tables: MutableSet<String>) {
                runFloodJob()
            }
        }

    @Synchronized
    private fun runFloodJob() {
        if (floodJob?.isActive == true) {
            return
        }
        floodJob = GlobalScope.launch(floodThread) {
            floodJobBlock()
        }
    }

    private var floodJob: Job? = null

    private suspend fun floodJobBlock() {
//        floodMessageDao.findFloodMessageDeferred().await()?.let { list ->
//            try {
//                list.forEach { message ->
//                    val data = gson.fromJson(message.data, BlazeMessageData::class.java)
//                    if (data.category.startsWith("WEBRTC_")) {
//                        callMessageDecrypt.onRun(data)
//                    } else if (data.userId == accountId && !data.sessionId.isNullOrEmpty()) {
//                        sessionMessageDecrypt.onRun(data)
//                    } else {
//                        messageDecrypt.onRun(data)
//                    }
//                    floodMessageDao.delete(message)
//                }
//            } catch (e: Exception) {
//                runFloodJob()
//            } finally {
//                floodJob = null
//            }
//        }
    }

    private fun deliver(blazeMessage: BlazeMessage): Boolean {
        //val bm = webSocket.sendMessage(blazeMessage)
//        if (bm == null) {
//            Thread.sleep(Constants.SLEEP_MILLIS)
//            throw WebSocketException()
//        } else if (bm.error != null) {
//            if (bm.error.code == ErrorHandler.FORBIDDEN) {
//                return true
//            } else {
//                Thread.sleep(Constants.SLEEP_MILLIS)
//                throw NetworkException()
//            }
//        }
        return true
    }
}