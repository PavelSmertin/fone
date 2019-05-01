package com.fone.android.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fone.android.R
import com.fone.android.db.ConversationDao
import com.fone.android.db.UserDao
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.ui.common.BlazeBaseActivity
import com.fone.android.ui.common.NavigationController
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.util.ErrorHandler
import com.fone.android.vo.isGroup
import com.uber.autodispose.autoDisposable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import javax.inject.Inject

//import androidx.core.content.getSystemService

class MainActivity : BlazeBaseActivity() {

    @Inject
    lateinit var navigationController: NavigationController

    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]

    lateinit var conversationDao: ConversationDao

    @Inject
    lateinit var userDao: UserDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigationController.navigateToMessage()
        }

        handlerCode(intent)
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlerCode(intent)
    }

    private var alertDialog: AlertDialog? = null

    private fun handlerCode(intent: Intent) {
        if (intent.extras != null && intent.extras!!.getString("conversation_id", null) != null) {
            alertDialog?.dismiss()
            alertDialog = alert(getString(R.string.group_wait)) {}.show()
            val conversationId = intent.extras!!.getString("conversation_id")!!
            Maybe.just(conversationId).map {
                val innerIntent: Intent?
                var conversation = conversationDao.findConversationById(conversationId)

                if (conversation?.isGroup() == true) {
                    innerIntent = ConversationActivity.putIntent(this, conversationId)
                } else {
                    var user = userDao.findPlainUserByConversationId(conversationId)
                    innerIntent = ConversationActivity.putIntent(this, conversationId, user?.userId)
                }
                runOnUiThread { alertDialog?.dismiss() }
                innerIntent
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scopeProvider).subscribe({
                    it?.let { intent ->
                        this.startActivity(intent)
                    }
                }, {
                    alertDialog?.dismiss()
                    ErrorHandler.handleError(it)
                })
        }
    }

    companion object {
        private const val URL = "url"
        private const val SCAN = "scan"
        private const val TRANSFER = "transfer"

        fun showUrl(context: Context, url: String) {
            Intent(context, MainActivity::class.java).apply {
                putExtra(URL, url)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }.run {
                context.startActivity(this)
            }
        }

        fun showTransfer(context: Context, userId: String) {
            Intent(context, MainActivity::class.java).apply {
                putExtra(TRANSFER, userId)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }.run {
                context.startActivity(this)
            }
        }

        fun showScan(context: Context, text: String) {
            Intent(context, MainActivity::class.java).apply {
                putExtra(SCAN, text)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }.run { context.startActivity(this) }
        }

        fun show(context: Context) {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }.run {
                context.startActivity(this)
            }
        }

        fun getSingleIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
        }
    }
}

