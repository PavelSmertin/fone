package com.fone.android.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.fone.android.Constants
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.db.ConversationDao
import com.fone.android.db.UserDao
import com.fone.android.di.type.DatabaseCategory
import com.fone.android.di.type.DatabaseCategoryEnum
import com.fone.android.extension.defaultSharedPreferences
import com.fone.android.ui.common.BlazeBaseActivity
import com.fone.android.ui.common.NavigationController
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.ui.landing.InitializeActivity
import com.fone.android.ui.landing.LandingActivity
import com.fone.android.ui.landing.LoadingFragment
import com.fone.android.util.ErrorHandler
import com.fone.android.util.Session
import com.fone.android.vo.isGroup
import com.fone.android.widget.MaterialSearchView
import com.uber.autodispose.autoDisposable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
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

        if (!Session.checkToken()) run {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
            return
        }

        if (defaultSharedPreferences.getBoolean(Constants.Account.PREF_SET_NAME, false)) {
            InitializeActivity.showSetupName(this)
        }

        FoneApplication.get().onlining.set(true)
        if (!defaultSharedPreferences.getBoolean(LoadingFragment.IS_LOADED, false)) {
            InitializeActivity.showLoading(this)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigationController.navigateToMessage()
        }

        initView()
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

//                if (conversation == null) {
//                    val response = conversationService.getConversation(conversationId).execute().body()
//                    if (response != null && response.isSuccess) {
//                        response.data?.let { data ->
//                            var ownerId: String = data.creatorId
//                            if (data.category == ConversationCategory.CONTACT.name) {
//                                ownerId = data.participants.find { p -> p.userId != Session.getAccountId() }!!.userId
//                            } else if (data.category == ConversationCategory.GROUP.name) {
//                                ownerId = data.creatorId
//                            }
//                            var c = conversationDao.findConversationById(data.conversationId)
//                            if (c == null) {
//                                c = Conversation(
//                                    data.conversationId,
//                                    ownerId,
//                                    data.category,
//                                    data.name,
//                                    data.iconUrl,
//                                    data.announcement,
//                                    data.codeUrl,
//                                    "",
//                                    data.createdAt,
//                                    null,
//                                    null,
//                                    null,
//                                    0,
//                                    ConversationStatus.SUCCESS.ordinal,
//                                    null)
//                                conversation = c
//                                conversationDao.insertConversation(c)
//                            } else {
//                                conversationDao.updateConversation(data.conversationId, ownerId, data.category,
//                                    data.name, data.announcement, data.muteUntil, data.createdAt, ConversationStatus.SUCCESS.ordinal)
//                            }
//
//                            val participants = mutableListOf<Participant>()
//                            val userIdList = mutableListOf<String>()
//                            for (p in data.participants) {
//                                val item = Participant(conversationId, p.userId, p.role, p.createdAt!!)
//                                if (p.role == ParticipantRole.OWNER.name) {
//                                    participants.add(0, item)
//                                } else {
//                                    participants.add(item)
//                                }
//
//                                val u = userDao.findUser(p.userId)
//                                if (u == null) {
//                                    userIdList.add(p.userId)
//                                }
//                            }
//                            if (userIdList.isNotEmpty()) {
//                                jobManager.addJobInBackground(RefreshUserJob(userIdList))
//                            }
//                            participantDao.insertList(participants)
//                        }
//                    }
//                }
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


    private fun initView() {

        search_bar.setOnRightClickListener(View.OnClickListener {
            navigationController.pushContacts()
        })

        search_bar.setOnBackClickListener(View.OnClickListener {
            navigationController.hideSearch()
            search_bar.closeSearch()
        })

        search_bar.mOnQueryTextListener = object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                //SearchFragment.getInstance().setQueryText(newText)
                return true
            }
        }

        search_bar.setSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                navigationController.hideSearch()
            }

            override fun onSearchViewOpened() {
                navigationController.showSearch()
            }
        })
        root_view.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && search_bar.isOpen) {
                search_bar.closeSearch()
                true
            } else {
                false
            }
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

