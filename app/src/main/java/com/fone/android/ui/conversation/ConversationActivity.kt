package com.fone.android.ui.conversation


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import com.fone.android.R
import com.fone.android.extension.replaceFragment
import com.fone.android.repository.UserRepository
import com.fone.android.ui.common.BlazeBaseActivity
import com.fone.android.ui.conversation.ConversationFragment.Companion.CONVERSATION_ID
import com.fone.android.ui.conversation.ConversationFragment.Companion.RECIPIENT
import com.fone.android.ui.conversation.ConversationFragment.Companion.RECIPIENT_ID
import com.fone.android.util.Session
import com.fone.android.vo.ForwardMessage
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ConversationActivity : BlazeBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        showConversation(intent)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showConversation(intent)
    }

    @Inject
    lateinit var userRepository: UserRepository

    private fun showConversation(intent: Intent) {
        val bundle = intent.extras ?: return
        if (bundle.getString(CONVERSATION_ID) == null) {
            val userId = bundle.getString(RECIPIENT_ID)!!
            Observable.just(userId).map {
                userRepository.getUserById(userId)!!
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).autoDisposable(scopeProvider)
                .subscribe({
                    if (it.userId == Session.getAccountId()) {
                        throw IllegalArgumentException("error data")
                    }
                    bundle.putParcelable(RECIPIENT, it)
                    replaceFragment(ConversationFragment.newInstance(bundle), R.id.container, ConversationFragment.TAG)
                }, {
                    replaceFragment(
                        ConversationFragment.newInstance(intent.extras!!), R.id.container,
                        ConversationFragment.TAG
                    )
                })
        } else {
            Observable.just(bundle.getString(CONVERSATION_ID)).map {
                userRepository.findContactByConversationId(it)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).autoDisposable(scopeProvider)
                .subscribe({
                    if (it?.userId == Session.getAccountId()) {
                        throw IllegalArgumentException("error data ${bundle.getString(CONVERSATION_ID)}")
                    }
                    bundle.putParcelable(RECIPIENT, it)
                    replaceFragment(ConversationFragment.newInstance(bundle), R.id.container, ConversationFragment.TAG)
                }, {
                    replaceFragment(
                        ConversationFragment.newInstance(intent.extras!!), R.id.container,
                        ConversationFragment.TAG
                    )
                })
        }
    }

    companion object {

        fun show(
            context: Context,
            conversationId: String? = null,
            recipientId: String? = null,
            messageId: String? = null,
            keyword: String? = null,
            messages: ArrayList<ForwardMessage>? = null
        ) {
            if (conversationId == null && recipientId == null) {
                throw IllegalArgumentException("lose data")
            }
            if (recipientId == Session.getAccountId()) {
                throw IllegalArgumentException("error data $conversationId")
            }
            Intent(context, ConversationActivity::class.java).apply {
                putExtras(ConversationFragment.putBundle(conversationId, recipientId, messageId, keyword, messages))
            }.run {
                context.startActivity(this)
            }
        }

        fun putIntent(
            context: Context,
            conversationId: String? = null,
            recipientId: String? = null,
            messageId: String? = null,
            keyword: String? = null,
            messages: ArrayList<ForwardMessage>? = null
        ): Intent {
            if (conversationId == null && recipientId == null) {
                throw IllegalArgumentException("lose data")
            }
            if (recipientId == Session.getAccountId()) {
                throw IllegalArgumentException("error data $conversationId")
            }
            return Intent(context, ConversationActivity::class.java).apply {
                putExtras(ConversationFragment.putBundle(conversationId, recipientId, messageId, keyword, messages))
            }
        }
    }
}