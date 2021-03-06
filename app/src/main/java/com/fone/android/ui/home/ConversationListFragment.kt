package com.fone.android.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fone.android.R
import com.fone.android.extension.*
import com.fone.android.ui.common.LinkFragment
import com.fone.android.ui.common.NavigationController
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.util.Session
import com.fone.android.vo.*
import com.fone.android.websocket.SystemConversationAction
import com.fone.android.widget.BottomSheet
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_conversation_list.*

import kotlinx.android.synthetic.main.item_list_conversation.view.*
import kotlinx.android.synthetic.main.view_conversation_bottom.view.*
import kotlinx.android.synthetic.main.view_empty.*


import org.jetbrains.anko.doAsync
import javax.inject.Inject

class ConversationListFragment : LinkFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var navigationController: NavigationController
//    @Inject
//    lateinit var jobManager: FoneJobManager

    private val messagesViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConversationListViewModel::class.java)
    }

    private val messageAdapter by lazy { MessageAdapter(message_rv) }

    private var distance = 0
    private var shadowVisible = true
    private val touchSlop: Int by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }

    companion object {
        fun newInstance() = ConversationListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_conversation_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        message_rv.adapter = messageAdapter
        message_rv.itemAnimator = null
        message_rv.setHasFixedSize(true)
        message_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (distance < -touchSlop && !shadowVisible) {
                    distance = 0
                    shadowVisible = true
                } else if (distance > touchSlop && shadowVisible) {
                    distance = 0
                    shadowVisible = false
                }
                if ((dy > 0 && shadowVisible) || (dy < 0 && !shadowVisible)) {
                    distance += dy
                }
            }
        })

        messageAdapter.onItemClickListener = object : OnItemClickListener {
            override fun longClick(conversation: ConversationItem): Boolean {
                showBottomSheet(conversation.conversationId, conversation.pinTime != null)
                return true
            }

            override fun click(position: Int, conversation: ConversationItem) {
                if (conversation.isGroup() && (conversation.status == ConversationStatus.START.ordinal ||
                        conversation.status == ConversationStatus.FAILURE.ordinal)) {
                    if (!context!!.networkConnected()) {
                        context?.toast(R.string.error_network)
                        return
                    }
                    doAsync { messagesViewModel.createGroupConversation(conversation.conversationId) }
                } else {
                    ConversationActivity.show(context!!, conversationId = conversation.conversationId)
                }
            }
        }
        messagesViewModel.conversations.observe(this, Observer { r ->
            if (r == null || r.isEmpty()) {
                empty_view.visibility = VISIBLE
            } else {
                empty_view.visibility = GONE
                messageAdapter.setConversationList(r)
//                r.filter { it.isGroup() && (it.iconUrl() == null || !File(it.iconUrl()).exists()) }
//                    .forEach {
//                        jobManager.addJobInBackground(GenerateAvatarJob(it.conversationId))
//                    }
            }
        })

        start_bn.setOnClickListener {
            navigationController.pushContacts()
        }
    }

    @SuppressLint("InflateParams")
    fun showBottomSheet(conversationId: String, hasPin: Boolean) {
        val builder = BottomSheet.Builder(requireActivity())
        val view = View.inflate(ContextThemeWrapper(requireActivity(), R.style.Custom), R.layout.view_conversation_bottom, null)
        builder.setCustomView(view)
        val bottomSheet = builder.create()
        view.delete_tv.setOnClickListener {
            messagesViewModel.deleteConversation(conversationId)
            bottomSheet.dismiss()
        }
        view.cancel_tv.setOnClickListener { bottomSheet.dismiss() }
        if (hasPin) {
            view.pin_tv.setText(R.string.conversation_pin_clear)
            view.pin_tv.setOnClickListener {
                messagesViewModel.updateConversationPinTimeById(conversationId, null)
                bottomSheet.dismiss()
            }
        } else {
            view.pin_tv.setText(R.string.conversation_pin)
            view.pin_tv.setOnClickListener {
                messagesViewModel.updateConversationPinTimeById(conversationId, nowInUtc())
                bottomSheet.dismiss()
            }
        }

        bottomSheet.show()
    }

    class MessageAdapter(private val rv: RecyclerView) : RecyclerView.Adapter<MessageHolder>() {

        var conversations: List<ConversationItem>? = null

        var onItemClickListener: OnItemClickListener? = null

        fun setConversationList(newConversations: List<ConversationItem>) {
            if (conversations == null) {
                conversations = newConversations
                notifyItemRangeInserted(0, newConversations.size)
            } else {
                val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return conversations!!.size
                    }

                    override fun getNewListSize(): Int {
                        return newConversations.size
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val old = conversations!![oldItemPosition]
                        val newItem = newConversations[newItemPosition]
                        return old.conversationId == newItem.conversationId
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val old = conversations!![oldItemPosition]
                        val newItem = newConversations[newItemPosition]
                        return old == newItem
                    }
                })
                conversations = newConversations
                val recyclerViewState = rv.layoutManager?.onSaveInstanceState()
                diffResult.dispatchUpdatesTo(this)
                rv.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        }

        override fun onBindViewHolder(holder: MessageHolder, position: Int) {
            holder.bind(onItemClickListener, position, conversations!![position])
        }

        override fun getItemCount() = notNullElse(conversations, { list -> list.size }, 0)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder =
            MessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_conversation, parent, false))
    }

    class MessageHolder constructor(containerView: View) : RecyclerView.ViewHolder(containerView) {
        var context: Context = itemView.context
        private fun getText(id: Int) = context.getText(id).toString()

        fun bind(onItemClickListener: OnItemClickListener?, position: Int, conversationItem: ConversationItem) {
            val id = Session.getAccountId()
            conversationItem.getConversationName().let {
                itemView.name_tv.text = it
            }
            itemView.group_name_tv.visibility = GONE
            when {
                conversationItem.messageStatus == MessageStatus.FAILED.name -> {
                    conversationItem.content?.let {
                        setConversationName(conversationItem)
                        itemView.msg_tv.setText(R.string.conversation_waiting)
                    }
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_fail)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_TEXT.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_TEXT.name -> {
                    conversationItem.content?.let {
                        setConversationName(conversationItem)
                        itemView.msg_tv.text = it
                    }
                    null
                }
                conversationItem.contentType == MessageCategory.SYSTEM_ACCOUNT_SNAPSHOT.name -> {
                    itemView.msg_tv.setText(R.string.conversation_status_transfer)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_transfer)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_STICKER.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_STICKER.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_sticker)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_stiker)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_IMAGE.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_IMAGE.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_pic)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_pic)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_VIDEO.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_VIDEO.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_video)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_video)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_DATA.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_DATA.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_file)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_file)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_AUDIO.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_AUDIO.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_audio)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_audio)
                }
                conversationItem.contentType == MessageCategory.APP_BUTTON_GROUP.name -> {
                    itemView.group_name_tv.visibility = GONE
                    val buttons = Gson().fromJson(conversationItem.content, Array<AppButtonData>::class.java)
                    var content = ""
                    buttons.map { content += "[" + it.label + "]" }
                    itemView.msg_tv.text = content
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_touch_app)
                }
                conversationItem.contentType == MessageCategory.APP_CARD.name -> {
                    itemView.group_name_tv.visibility = GONE
                    val cardData = Gson().fromJson(conversationItem.content, AppCardData::class.java)
                    itemView.msg_tv.text = cardData.title
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_touch_app)
                }
                conversationItem.contentType == MessageCategory.SIGNAL_CONTACT.name ||
                    conversationItem.contentType == MessageCategory.PLAIN_CONTACT.name -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.contact_less_title)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_contact)
                }
                conversationItem.isCallMessage() -> {
                    setConversationName(conversationItem)
                    itemView.msg_tv.setText(R.string.conversation_status_voice)
                    AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_voice)
                }
                conversationItem.contentType == MessageCategory.SYSTEM_CONVERSATION.name -> {
                    when (conversationItem.actionName) {
                        SystemConversationAction.CREATE.name -> {
                            itemView.msg_tv.text =
                                String.format(getText(R.string.chat_group_create),
                                    if (id == conversationItem.senderId) {
                                        getText(R.string.chat_you_start)
                                    } else {
                                        conversationItem.name
                                    }, conversationItem.groupName)
                        }
                        SystemConversationAction.ADD.name -> {
                            itemView.msg_tv.text =
                                String.format(getText(R.string.chat_group_add),
                                    if (id == conversationItem.senderId) {
                                        getText(R.string.chat_you_start)
                                    } else {
                                        conversationItem.senderFullName
                                    },
                                    if (id == conversationItem.participantUserId) {
                                        getText(R.string.chat_you)
                                    } else {
                                        conversationItem.participantFullName
                                    })
                        }
                        SystemConversationAction.REMOVE.name -> {
                            itemView.msg_tv.text =
                                String.format(getText(R.string.chat_group_remove),
                                    if (id == conversationItem.senderId) {
                                        getText(R.string.chat_you_start)
                                    } else {
                                        conversationItem.senderFullName
                                    },
                                    if (id == conversationItem.participantUserId) {
                                        getText(R.string.chat_you)
                                    } else {
                                        conversationItem.participantFullName
                                    })
                        }
                        SystemConversationAction.JOIN.name -> {
                            itemView.msg_tv.text =
                                String.format(getText(R.string.chat_group_join),
                                    if (id == conversationItem.participantUserId) {
                                        getText(R.string.chat_you_start)
                                    } else {
                                        conversationItem.participantFullName
                                    })
                        }
                        SystemConversationAction.EXIT.name -> {
                            itemView.msg_tv.text =
                                String.format(getText(R.string.chat_group_exit),
                                    if (id == conversationItem.participantUserId) {
                                        getText(R.string.chat_you_start)
                                    } else {
                                        conversationItem.participantFullName
                                    })
                        }
                        SystemConversationAction.ROLE.name -> {
                            itemView.msg_tv.text = getText(R.string.group_role)
                        }
                        else -> {
                            itemView.msg_tv.text = ""
                        }
                    }
                    null
                }
                else -> {
                    itemView.msg_tv.text = ""
                    null
                }
            }.also {
                it?.setBounds(0, 0, itemView.context.dpToPx(12f), itemView.context.dpToPx(12f))
                TextViewCompat.setCompoundDrawablesRelative(itemView.msg_tv, it, null, null, null)
            }

            if (conversationItem.senderId == Session.getAccountId() &&
                conversationItem.contentType != MessageCategory.SYSTEM_CONVERSATION.name &&
                conversationItem.contentType != MessageCategory.SYSTEM_ACCOUNT_SNAPSHOT.name &&
                !conversationItem.isCallMessage()) {
                when (conversationItem.messageStatus) {
                    MessageStatus.SENDING.name -> AppCompatResources.getDrawable(itemView.context,
                        R.drawable.ic_status_sending)
                    MessageStatus.SENT.name -> AppCompatResources.getDrawable(itemView.context,
                        R.drawable.ic_status_sent)
                    MessageStatus.DELIVERED.name -> AppCompatResources.getDrawable(itemView.context,
                        R.drawable.ic_status_delivered)
                    MessageStatus.READ.name -> AppCompatResources.getDrawable(itemView.context,
                        R.drawable.ic_status_read)
                    else -> {
                        AppCompatResources.getDrawable(itemView.context, R.drawable.ic_status_sending)
                    }
                }.also {
                    it?.setBounds(0, 0, itemView.context.dpToPx(12f), itemView.context.dpToPx(12f))
                    itemView.msg_flag.setImageDrawable(it)
                    itemView.msg_flag.visibility = VISIBLE
                }
            } else {
                itemView.msg_flag.visibility = GONE
            }
            conversationItem.createdAt?.let {
                itemView.time_tv.timeAgo(it)
            }
            if (conversationItem.pinTime == null) {
                itemView.msg_pin.visibility = GONE
                if (conversationItem.isGroup() && conversationItem.status == ConversationStatus.START.ordinal) {
                    itemView.pb.visibility = VISIBLE
                    itemView.unread_tv.visibility = GONE
                } else {
                    itemView.pb.visibility = GONE
                    notEmptyOrElse(conversationItem.unseenMessageCount,
                        { itemView.unread_tv.text = "$it"; itemView.unread_tv.visibility = VISIBLE },
                        { itemView.unread_tv.visibility = GONE })
                }
            } else {
                itemView.msg_pin.visibility = VISIBLE
                if (conversationItem.isGroup() && conversationItem.status == ConversationStatus.START.ordinal) {
                    itemView.pb.visibility = VISIBLE
                    itemView.unread_tv.visibility = GONE
                } else {
                    itemView.pb.visibility = GONE
                    notEmptyOrElse(conversationItem.unseenMessageCount,
                        { itemView.unread_tv.text = "$it"; itemView.unread_tv.visibility = VISIBLE; },
                        { itemView.unread_tv.visibility = GONE }
                    )
                }
            }

            //itemView.bot_iv.visibility = if (conversationItem.isBot()) VISIBLE else GONE
            itemView.mute_iv.visibility = if (conversationItem.isMute()) VISIBLE else GONE
            //itemView.verified_iv.visibility = if (conversationItem.ownerVerified == true) VISIBLE else GONE

            if (conversationItem.isGroup()) {
                itemView.avatar_iv.setGroup(conversationItem.iconUrl())
            } else {
                itemView.avatar_iv.setInfo(conversationItem.getConversationName(),
                    conversationItem.iconUrl(), conversationItem.ownerId)
            }
            itemView.setOnClickListener { onItemClickListener?.click(position, conversationItem) }
            itemView.setOnLongClickListener {
                notNullElse(onItemClickListener, { it.longClick(conversationItem) }, false)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setConversationName(conversationItem: ConversationItem) {
            if (conversationItem.isGroup() && conversationItem.senderId != Session.getAccountId()) {
                itemView.group_name_tv.text = "${conversationItem.senderFullName}: "
                itemView.group_name_tv.visibility = VISIBLE
            } else {
                itemView.group_name_tv.visibility = GONE
            }
        }
    }

    interface OnItemClickListener {
        fun click(position: Int, conversation: ConversationItem)
        fun longClick(conversation: ConversationItem): Boolean
    }
}