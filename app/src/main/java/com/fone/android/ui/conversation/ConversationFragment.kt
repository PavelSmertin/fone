package com.fone.android.ui.conversation

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fone.android.Constants.PAGE_SIZE
import com.fone.android.FoneApplication
import com.fone.android.R
import com.fone.android.RxBus
import com.fone.android.event.BlinkEvent
import com.fone.android.event.DragReleaseEvent
import com.fone.android.extension.*
import com.fone.android.ui.common.LinkFragment
import com.fone.android.ui.conversation.adapter.ConversationAdapter
import com.fone.android.ui.conversation.adapter.Menu
import com.fone.android.ui.conversation.adapter.MenuType
import com.fone.android.ui.conversation.holder.BaseViewHolder
import com.fone.android.ui.conversation.preview.PreviewDialogFragment
import com.fone.android.util.Session
import com.fone.android.vo.*
import com.fone.android.widget.ChatControlView
import com.fone.android.widget.DraggableRecyclerView
import com.fone.android.widget.DraggableRecyclerView.Companion.FLING_DOWN
import com.fone.android.widget.DraggableRecyclerView.Companion.FLING_UP
import com.fone.android.widget.FoneHeadersDecoration
import com.fone.android.widget.keyboard.KeyboardAwareLinearLayout
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.autoDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_conversation.*
import kotlinx.android.synthetic.main.view_chat_control.view.*
import kotlinx.android.synthetic.main.view_reply.view.*
import kotlinx.android.synthetic.main.view_title.view.*
import kotlinx.android.synthetic.main.view_tool.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class ConversationFragment : LinkFragment(), KeyboardAwareLinearLayout.OnKeyboardShownListener,
    KeyboardAwareLinearLayout.OnKeyboardHiddenListener {

    companion object {
        const val TAG = "ConversationFragment"

        const val CONVERSATION_ID = "conversation_id"
        const val RECIPIENT_ID = "recipient_id"
        const val RECIPIENT = "recipient"
        private const val MESSAGE_ID = "message_id"
        private const val KEY_WORD = "key_word"
        private const val MESSAGES = "messages"

        fun putBundle(
            conversationId: String?,
            recipientId: String?,
            messageId: String?,
            keyword: String?,
            messages: ArrayList<ForwardMessage>?
        ): Bundle =
            Bundle().apply {
                if (conversationId == null && recipientId == null) {
                    throw IllegalArgumentException("lose data")
                }
                messageId?.let {
                    putString(MESSAGE_ID, messageId)
                }
                keyword?.let {
                    putString(KEY_WORD, keyword)
                }
                putString(CONVERSATION_ID, conversationId)
                putString(RECIPIENT_ID, recipientId)
                putParcelableArrayList(MESSAGES, messages)
            }

        fun newInstance(bundle: Bundle) = ConversationFragment().apply { arguments = bundle }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var conversationContext: CoroutineContext

    private val chatViewModel: ConversationViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConversationViewModel::class.java)
    }

    private var unreadTipCount: Int = 0
    private val chatAdapter: ConversationAdapter by lazy {
        ConversationAdapter(keyword, onItemListener, isGroup, !isPlainMessage()).apply {
            registerAdapterDataObserver(chatAdapterDataObserver)
        }
    }

    private val chatAdapterDataObserver =
        object : RecyclerView.AdapterDataObserver() {
            var oldSize = 0

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                chatAdapter.currentList?.let {
                    oldSize = it.size
                }
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                when {
                    isFirstLoad -> {
                        isFirstLoad = false
//                        if (context?.sharedPreferences(RefreshConversationJob.PREFERENCES_CONVERSATION)
//                                ?.getBoolean(conversationId, false) == true) {
//                            showGroupNotification = true
//                            showAlert(0)
//                        }
                        val position = if (messageId != null) {
                            unreadCount + 1
                        } else {
                            unreadCount
                        }
                        (chat_rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, chat_rv.measuredHeight * 3 / 4)
                        chat_rv.visibility = VISIBLE
                    }
                    isBottom -> {
                        if (chatAdapter.currentList != null && chatAdapter.currentList!!.size > oldSize) {
                            chat_rv.layoutManager?.scrollToPosition(0)
                        }
                    }
                    else -> {
                        if (unreadTipCount > 0) {
                            down_unread.visibility = VISIBLE
                            down_unread.text = "$unreadTipCount"
                        } else {
                            down_unread.visibility = GONE
                        }
                    }
                }
                chatAdapter.currentList?.let {
                    oldSize = it.size
                }
            }
        }


    private val onItemListener: ConversationAdapter.OnItemListener by lazy {
        object : ConversationAdapter.OnItemListener() {
            override fun onSelect(isSelect: Boolean, messageItem: MessageItem, position: Int) {
                if (isSelect) {
                    chatAdapter.addSelect(messageItem)
                } else {
                    chatAdapter.removeSelect(messageItem)
                }
                when {
                    chatAdapter.selectSet.isEmpty() -> tool_view.fadeOut()
                    chatAdapter.selectSet.size == 1 -> {
                        try {
                            if (chatAdapter.selectSet.valueAt(0)?.type == MessageCategory.SIGNAL_TEXT.name ||
                                chatAdapter.selectSet.valueAt(0)?.type == MessageCategory.PLAIN_TEXT.name) {
                                tool_view.copy_iv.visibility = View.VISIBLE
                            } else {
                                tool_view.copy_iv.visibility = View.GONE
                            }
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            tool_view.copy_iv.visibility = View.GONE
                        }
                        if (chatAdapter.selectSet.valueAt(0)?.supportSticker() == true) {
                            tool_view.add_sticker_iv.visibility = VISIBLE
                        } else {
                            tool_view.add_sticker_iv.visibility = GONE
                        }
                        if (chatAdapter.selectSet.valueAt(0)?.canNotReply() == true) {
                            tool_view.reply_iv.visibility = View.GONE
                        } else {
                            tool_view.reply_iv.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        tool_view.forward_iv.visibility = View.VISIBLE
                        tool_view.reply_iv.visibility = View.GONE
                        tool_view.copy_iv.visibility = View.GONE
                        tool_view.add_sticker_iv.visibility = GONE
                    }
                }
                if (chatAdapter.selectSet.find { it.canNotForward() } != null) {
                    tool_view.forward_iv.visibility = View.GONE
                } else {
                    tool_view.forward_iv.visibility = View.VISIBLE
                }
                chatAdapter.notifyDataSetChanged()
            }

            override fun onLongClick(messageItem: MessageItem, position: Int): Boolean {
                val b = chatAdapter.addSelect(messageItem)
                if (b) {
                    if (messageItem.type == MessageCategory.SIGNAL_TEXT.name ||
                        messageItem.type == MessageCategory.PLAIN_TEXT.name) {
                        tool_view.copy_iv.visibility = View.VISIBLE
                    } else {
                        tool_view.copy_iv.visibility = View.GONE
                    }

                    if (messageItem.supportSticker()) {
                        tool_view.add_sticker_iv.visibility = VISIBLE
                    } else {
                        tool_view.add_sticker_iv.visibility = GONE
                    }

                    if (chatAdapter.selectSet.find { it.canNotForward() } != null) {
                        tool_view.forward_iv.visibility = View.GONE
                    } else {
                        tool_view.forward_iv.visibility = View.VISIBLE
                    }
                    if (chatAdapter.selectSet.find { it.canNotReply() } != null) {
                        tool_view.reply_iv.visibility = View.GONE
                    } else {
                        tool_view.reply_iv.visibility = View.VISIBLE
                    }
                    chatAdapter.notifyDataSetChanged()
                    tool_view.fadeIn()
                }
                return b
            }

            override fun onCancel(id: String) {
                chatViewModel.cancel(id)
            }

            override fun onImageClick(messageItem: MessageItem, view: View) {
            }

            @TargetApi(Build.VERSION_CODES.O)
            override fun onFileClick(messageItem: MessageItem) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O &&
                    messageItem.mediaMimeType.equals("application/vnd.android.package-archive", true)) {
                    if (requireContext().packageManager.canRequestPackageInstalls()) {
                        openMedia(messageItem)
                    } else {
                        startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
                    }
                } else {
                    openMedia(messageItem)
                }
            }

            @SuppressLint("CheckResult")
            override fun onUserClick(userId: String) {
            }

            override fun onUrlClick(url: String) {
            }

            override fun onMentionClick(name: String) {
            }

            override fun onAddClick() {
            }

            override fun onBlockClick() {
            }

            override fun onActionClick(action: String) {
            }

            override fun onContactCardClick(userId: String) {
            }

            override fun onMessageClick(messageId: String?) {
                messageId?.let {
                    chatViewModel.findMessageIndex(conversationId, it).autoDisposable(scopeProvider).subscribe({
                        if (it == 0) {
                            toast(R.string.error_not_found)
                        } else {
                            if (it == chatAdapter.itemCount - 1) {
                                scrollTo(it, 0, action = {
                                    requireContext().mainThreadDelayed({
                                        RxBus.publish(BlinkEvent(messageId))
                                    }, 60)
                                })
                            } else {
                                scrollTo(it + 1, chat_rv.measuredHeight * 3 / 4, action = {
                                    requireContext().mainThreadDelayed({
                                        RxBus.publish(BlinkEvent(messageId))
                                    }, 60)
                                })
                            }
                        }
                    }, {
                    })
                }
            }
        }
    }

    private val decoration by lazy {
        FoneHeadersDecoration(chatAdapter)
    }

    private var imageUri: Uri? = null

    private val conversationId: String by lazy {
        var cid = arguments!!.getString(CONVERSATION_ID)
        if (cid.isNullOrBlank()) {
            isFirstMessage = true
            cid = generateConversationId(sender.userId, recipient!!.userId)
        }
        cid
    }

    private var recipient: User? = null

    private val isGroup: Boolean by lazy {
        recipient == null
    }

    private val isBot: Boolean by lazy {
        recipient?.isBot() == true
    }

    private val messageId: String? by lazy {
        arguments!!.getString(MESSAGE_ID, null)
    }

    private val keyword: String? by lazy {
        arguments!!.getString(KEY_WORD, null)
    }

    private val sender: User by lazy { Session.getAccount()!!.toUser() }
    private var app: App? = null

    private var isFirstMessage = false
    private var isFirstLoad = true
    private var isBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipient = arguments!!.getParcelable<User?>(RECIPIENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_conversation, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        conversationContext = Job()
        val messages = arguments!!.getParcelableArrayList<ForwardMessage>(MESSAGES)
        if (messages != null) {
            sendForwardMessages(messages)
        } else {
            initView()
        }
    }

    private var showGroupNotification = false
    private var disposable: Disposable? = null
    private var paused = false
    private var starTransition = false

    override fun onResume() {
        super.onResume()
        input_layout.addOnKeyboardShownListener(this)
        input_layout.addOnKeyboardHiddenListener(this)
        FoneApplication.conversationId = conversationId
        if (paused) {
            paused = false
            chat_rv.adapter?.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
        input_layout.removeOnKeyboardShownListener(this)
        input_layout.removeOnKeyboardHiddenListener(this)
        markRead()
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        if (chat_control.isRecording) {
            chat_control.cancelExternal()
        }
        FoneApplication.conversationId = null
    }

    override fun onBackPressed(): Boolean {
        return when {
            tool_view.visibility == VISIBLE -> {
                closeTool()
                true
            }
            chat_control.getVisibleContainer()?.isVisible == true -> {
                chat_control.reset()
                true
            }
            chat_control.isRecording -> {
                chat_control.cancelExternal()
                true
            }
            reply_view.visibility == VISIBLE -> {
                reply_view.fadeOut()
                chat_control.showOtherInput()
                true
            }
            else -> false
        }
    }

    private fun hideIfShowBottomSheet() {
        if (sticker_container.isVisible &&
            menu_container.isVisible &&
            gallery_container.isVisible) {
            chat_control.reset()
        }
        if (chat_control.isRecording) {
            chat_control.cancelExternal()
        }
        if (reply_view.visibility == VISIBLE) {
            reply_view.fadeOut()
            chat_control.showOtherInput()
        }
    }

    private fun closeTool() {
        chatAdapter.selectSet.clear()
        chatAdapter.notifyDataSetChanged()
        tool_view.fadeOut()
    }

    private fun markRead() {
        chatAdapter.markRead()
    }

    override fun onStop() {
        val draftText = chat_control.chat_et.text
        if (draftText != null) {
            chatViewModel.saveDraft(conversationId, draftText.toString())
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chat_rv?.let { rv ->
            rv.children.forEach {
                val vh = rv.getChildViewHolder(it)
                if (vh != null && vh is BaseViewHolder) {
                    vh.stopListen()
                }
            }
        }
        chatAdapter.unregisterAdapterDataObserver(chatAdapterDataObserver)
    }

    override fun onDetach() {
        super.onDetach()
        conversationContext.cancelChildren()
    }

    private var firstPosition = 0

    @SuppressLint("CheckResult")
    private fun initView() {
        chat_rv.visibility = INVISIBLE
        if (chat_rv.adapter == null) {
            chat_rv.adapter = chatAdapter
        }
        chat_control.callback = chatControlCallback
        chat_control.activity = requireActivity()
        chat_control.inputLayout = input_layout
        chat_control.stickerContainer = sticker_container
        chat_control.menuContainer = menu_container
        chat_control.galleryContainer = gallery_container
        chat_control.recordTipView = record_tip_tv
        chat_control.setCircle(record_circle)

        chat_rv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true)
        chat_rv.addItemDecoration(decoration)
        chat_rv.itemAnimator = null

        chat_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                firstPosition = (chat_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (firstPosition > 0) {
                    if (isBottom) {
                        isBottom = false
                        showAlert()
                    }
                } else {
                    if (!isBottom) {
                        isBottom = true
                        hideAlert()
                    }
                    unreadTipCount = 0
                    down_unread.visibility = GONE
                }
            }
        })
        chat_rv.callback = object : DraggableRecyclerView.Callback {
            override fun onScroll(dis: Float) {
                val currentContainer = chat_control.getDraggableContainer()
                if (currentContainer != null) {
                    dragChatControl(dis)
                }
            }

            override fun onRelease(fling: Int) {
                releaseChatControl(fling)
            }
        }
        action_bar.left_ib.setOnClickListener {
            activity?.onBackPressed()
        }

        bg_quick_flag.setOnClickListener {
            if (chat_rv.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                chat_rv.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
            }
            scrollTo(0)
            unreadTipCount = 0
            down_unread.visibility = GONE
        }
        chatViewModel.searchConversationById(conversationId)
            .autoDisposable(scopeProvider).subscribe({
                it?.draft?.let { str ->
                    if (isAdded) {
                        chat_control.chat_et.setText(str)
                    }
                }
            }, {
                Timber.e(it)
            })
        tool_view.close_iv.setOnClickListener { activity?.onBackPressed() }
        tool_view.delete_iv.setOnClickListener {
            chatViewModel.deleteMessages(chatAdapter.selectSet)
            closeTool()
        }
        reply_view.reply_close_iv.setOnClickListener {
            reply_view.fadeOut()
            chat_control.showOtherInput()
        }
        tool_view.copy_iv.setOnClickListener {
            try {
                context?.getClipboardManager()?.primaryClip =
                    ClipData.newPlainText(null, chatAdapter.selectSet.valueAt(0)?.content)
                context?.toast(R.string.copy_success)
            } catch (e: ArrayIndexOutOfBoundsException) {
            }
            closeTool()
        }

        tool_view.reply_iv.setOnClickListener {
            chatAdapter.selectSet.valueAt(0)?.let {
                reply_view.bind(it)
            }
            if (!reply_view.isVisible) {
                reply_view.fadeIn()
                chat_control.hideOtherInput()
                chat_control.reset()
                if (chat_control.isRecording) {
                    chat_control.cancelExternal()
                }
                chat_control.chat_et.showKeyboard()
            }
            closeTool()
        }

        bindData()
    }

    private fun liveDataMessage(unreadCount: Int) {
        chatViewModel.getMessages(conversationId, unreadCount).observe(this@ConversationFragment, Observer {
            it?.let {
                if (it.size > 0) {
                    isFirstMessage = false
                }
                if (!isFirstLoad && !isBottom && it.size > chatAdapter.getRealItemCount()) {
                    unreadTipCount += (it.size - chatAdapter.getRealItemCount())
                }
                if (isFirstLoad && messageId == null && unreadCount > 0) {
                    chatAdapter.unreadIndex = unreadCount
                } else if (it.size != chatAdapter.getRealItemCount()) {
                    chatAdapter.unreadIndex = null
                }
                if (it.size > 0) {
                    chatViewModel.markMessageRead(conversationId, sender.userId)
                }
            }
            chatAdapter.submitList(it)
        })
    }

    private var unreadCount = 0
    private fun bindData() {
        GlobalScope.launch(conversationContext) {
            unreadCount = if (!messageId.isNullOrEmpty()) {
                chatViewModel.findMessageIndexSync(conversationId, messageId!!)
            } else {
                chatViewModel.indexUnread(conversationId)
            }
            withContext(Dispatchers.Main) {
                if (!isAdded) {
                    return@withContext
                }
                liveDataMessage(unreadCount)
            }
        }

        if (isBot) {
            chat_control.showBot()
        } else {
            chat_control.hideBot()
        }
    }

    private var appList: List<App>? = null

    private fun sendForwardMessages(messages: List<ForwardMessage>) {
        createConversation {
            initView()
            messages.let {
                for (item in it) {
                    if (item.id != null) {
                    } else {
                        when (item.type) {
                            ForwardCategory.TEXT.name -> {
                                item.content?.let { sendMessage(it) }
                            }
                        }
                    }
                }
                scrollToDown()
            }
        }
    }

    private inline fun createConversation(crossinline action: () -> Unit) {
        if (isFirstMessage) {
            doAsync {
                chatViewModel.initConversation(conversationId, recipient!!, sender)
                isFirstMessage = false

                uiThread {
                    if (isAdded) {
                        action()
                    }
                }
            }
        } else {
            action()
        }
    }

    private fun isPlainMessage(): Boolean {
        return if (isGroup) {
            false
        } else {
            this.isBot
        }
    }

    private fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            chat_control.chat_et.setText("")
            createConversation {
                chatViewModel.sendTextMessage(conversationId, sender, message, isPlainMessage())
                scrollToDown()
                markRead()
            }
        }
    }

    private fun sendReplyMessage(message: String) {
        if (message.isNotBlank() && reply_view.messageItem != null) {
            chat_control.chat_et.setText("")
            createConversation {
                chatViewModel.sendReplyMessage(conversationId, sender, message, reply_view.messageItem!!, isPlainMessage())
                reply_view.fadeOut()
                chat_control.showOtherInput()
                reply_view.messageItem = null
                scrollToDown()
                markRead()
            }
        }
    }

    override fun onKeyboardHidden() {
        chat_control.toggleKeyboard(false)
    }

    override fun onKeyboardShown() {
        chat_control.toggleKeyboard(true)
    }

    private fun clickSticker() {
    }

    private fun clickMenu() {
        val menuFragment = requireFragmentManager().findFragmentByTag(MenuFragment.TAG)
        if (menuFragment == null) {
            initMenuLayout()
        }
    }



    private fun initMenuLayout(isSelfCreatedBot: Boolean = false) {
        val menuFragment = MenuFragment.newInstance(isGroup, isBot, isSelfCreatedBot)
        activity?.replaceFragment(menuFragment, R.id.menu_container, MenuFragment.TAG)
        appList?.let {
            menuFragment.setAppList(it)
        }
        menuFragment.callback = object : MenuFragment.Callback {
            override fun onMenuClick(menu: Menu) {
                chat_control.reset()
                when (menu.type) {
                    MenuType.Camera -> {
                    }
                    MenuType.File -> {
                        RxPermissions(requireActivity())
                            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .subscribe({ granted ->
                                if (granted) {
                                    selectDocument()
                                } else {
                                    context?.openPermissionSetting()
                                }
                            }, {
                            })
                    }
                    MenuType.Contact -> {}
                    MenuType.Voice -> {}
                    MenuType.App -> {}
                }
            }
        }
    }

    private fun scrollToDown() {
        chat_rv.layoutManager?.scrollToPosition(0)
        if (firstPosition > PAGE_SIZE * 6) {
            chatAdapter.notifyDataSetChanged()
        }
    }

    private fun scrollTo(position: Int, offset: Int = -1, delay: Long = 30, action: (() -> Unit)? = null) {
        chat_rv.postDelayed({
            if (isAdded) {
                if (position == 0 && offset == 0) {
                    chat_rv.layoutManager?.scrollToPosition(0)
                } else if (offset == -1) {
                    (chat_rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                } else {
                    (chat_rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
                }
                action?.let { it() }
                if (abs(firstPosition - position) > PAGE_SIZE * 6) {
                    chatAdapter.notifyDataSetChanged()
                }
            }
        }, delay)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
        } else if (requestCode == REQUEST_FILE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openMedia(messageItem: MessageItem) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            messageItem.mediaUrl?.let {
                val file = File(it)
                val uri = if (!file.exists()) {
                    Uri.parse(it)
                } else {
                    requireContext().getUriForFile(file)
                }
                intent.setDataAndType(uri, messageItem.mediaMimeType)
                requireContext().startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            context?.toast(R.string.error_unable_to_open_media)
        }
    }


    private fun showAlert(duration: Long = 100) {
        if (isGroup) {
            if (showGroupNotification) {
                group_flag.visibility = VISIBLE
            } else {
                group_flag.visibility = GONE
            }
            if (!isBottom) {
                down_flag.visibility = VISIBLE
            } else {
                down_flag.visibility = GONE
            }
            if (bg_quick_flag.translationY != 0f) {
                bg_quick_flag.translationY(0f, duration)
            }
        } else {
            if (bg_quick_flag.translationY != 0f) {
                bg_quick_flag.translationY(0f, duration)
            }
        }
    }

    private fun hideAlert() {
        if (isGroup) {
            if (showGroupNotification) {
                group_flag.visibility = VISIBLE
            } else {
                group_flag.visibility = GONE
            }
            if (isBottom) {
                if (showGroupNotification) {
                    bg_quick_flag.translationY(requireContext().dpToPx(60f).toFloat(), 100)
                } else if (isBottom) {
                    bg_quick_flag.translationY(requireContext().dpToPx(130f).toFloat(), 100)
                }
            }
        } else {
            bg_quick_flag.translationY(requireContext().dpToPx(130f).toFloat(), 100)
        }
    }

    private var previewDialogFragment: PreviewDialogFragment? = null

    private fun showPreview(uri: Uri, action: (Uri) -> Unit) {
        if (previewDialogFragment == null) {
            previewDialogFragment = PreviewDialogFragment.newInstance()
        }
        previewDialogFragment?.show(requireFragmentManager(), uri, action)
    }

    private val voiceAlert by lazy {
        AlertDialog.Builder(requireContext(), R.style.MixinAlertDialogTheme)
            .setMessage(getString(R.string.chat_call_warning_voice))
            .setNegativeButton(getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }

    private fun showVoiceWarning() {
        if (!voiceAlert.isShowing) {
            voiceAlert.show()
        }
    }

    private fun dragChatControl(dis: Float) {
        val currentContainer = chat_control.getDraggableContainer() ?: return
        val params = currentContainer.layoutParams
        val targetH = params.height - dis.toInt()
        val total = (requireContext().screenHeight() * 2) / 3
        if (targetH <= 0 || targetH >= total) return

        params.height = targetH
        currentContainer.layoutParams = params
    }

    private fun releaseChatControl(fling: Int) {
        val currentContainer = chat_control.getDraggableContainer() ?: return
        val curH = currentContainer.height
        val max = (requireContext().screenHeight() * 2) / 3
        val maxMid = input_layout.keyboardHeight + (max - input_layout.keyboardHeight) / 2
        val minMid = input_layout.keyboardHeight / 2
        val targetH = if (curH > input_layout.keyboardHeight) {
            if (fling == FLING_UP) {
                max
            } else if (fling == FLING_DOWN) {
                input_layout.keyboardHeight
            } else {
                if (curH <= maxMid) {
                    input_layout.keyboardHeight
                } else {
                    max
                }
            }
        } else if (curH < input_layout.keyboardHeight) {
            if (fling == FLING_UP) {
                input_layout.keyboardHeight
            } else if (fling == FLING_DOWN) {
                0
            } else {
                if (curH > minMid) {
                    input_layout.keyboardHeight
                } else {
                    0
                }
            }
        } else {
            if (fling == FLING_UP) {
                max
            } else if (fling == FLING_DOWN) {
                0
            } else {
                input_layout.keyboardHeight
            }
        }
        if (targetH == 0) {
            chat_control.reset()
        }
        currentContainer.animateHeight(curH, targetH)
        RxBus.publish(DragReleaseEvent(targetH == max))
    }

    private val chatControlCallback = object : ChatControlView.Callback {
        override fun onRecordStart(audio: Boolean) {
        }

        override fun isReady(): Boolean {
            return true
        }

        override fun onRecordEnd() {
        }

        override fun onRecordCancel() {
        }

        override fun onCalling() {
        }

        override fun onGalleryClick() {
        }

        override fun onStickerClick() {
            clickSticker()
        }

        override fun onSendClick(text: String) {
            if (reply_view.isVisible && reply_view.messageItem != null) {
                sendReplyMessage(text)
            } else {
                sendMessage(text)
            }
        }

        override fun onMenuClick() {
            clickMenu()
        }

        override fun onBotClick() {
            hideIfShowBottomSheet()
        }

        override fun onDragChatControl(dis: Float) {
            dragChatControl(dis)
        }

        override fun onReleaseChatControl(fling: Int) {
            releaseChatControl(fling)
        }
    }
}