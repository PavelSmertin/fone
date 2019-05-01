package com.fone.android.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArraySet
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fone.android.Constants.PAGE_SIZE
import com.fone.android.R
import com.fone.android.RxBus
import com.fone.android.event.BlinkEvent
import com.fone.android.extension.hashForDate
import com.fone.android.extension.isSameDay
import com.fone.android.extension.notNullElse
import com.fone.android.ui.conversation.holder.BaseViewHolder
import com.fone.android.ui.conversation.holder.MessageHolder
import com.fone.android.ui.conversation.holder.TimeHolder
import com.fone.android.ui.conversation.holder.TransparentHolder
import com.fone.android.ui.home.ConversationListFragment
import com.fone.android.vo.*
import com.fone.android.widget.FoneStickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.item_chat_unread.view.*

import kotlin.math.abs

class ConversationAdapter(
    private val keyword: String?,
    private val onItemListener: OnItemListener,
    private val isGroup: Boolean,
    private val isSecret: Boolean = true
) : PagedListAdapter<MessageItem, RecyclerView.ViewHolder>(diffCallback), FoneStickyRecyclerHeadersAdapter<TimeHolder> {
    var selectSet: ArraySet<MessageItem> = ArraySet()
    var unreadIndex: Int? = null
    var recipient: User? = null

    var hasBottomView = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getAttachIndex(): Int? = if (unreadIndex != null) {
        if (hasBottomView) {
            unreadIndex
        } else {
            unreadIndex!! - 1
        }
    } else {
        null
    }

    override fun onCreateAttach(parent: ViewGroup): View =
        LayoutInflater.from(parent.context).inflate(R.layout.item_chat_unread, parent, false)

    override fun onBindAttachView(view: View) {
        view.unread_tv.text = view.context.getString(R.string.unread, unreadIndex!!)
    }

    fun markRead() {
        unreadIndex?.let {
            unreadIndex = null
        }
    }

    override fun getHeaderId(position: Int): Long = notNullElse(getItem(position), {
        Math.abs(it.createdAt.hashForDate())
    }, 0)

    override fun onCreateHeaderViewHolder(parent: ViewGroup): TimeHolder =
        TimeHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_time, parent, false))

    override fun onBindHeaderViewHolder(holder: TimeHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it.createdAt)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (getItemViewType(position)) {
                MESSAGE_TYPE -> {
                    (holder as MessageHolder).bind(it, keyword, isLast(position),
                        isFirst(position), selectSet.size > 0, isSelect(position), onItemListener)
                }
                else -> {
                }
            }
        }
    }

    private fun isSelect(position: Int): Boolean {
        return if (selectSet.isEmpty()) {
            false
        } else {
            selectSet.find { it.messageId == getItem(position)?.messageId } != null
        }
    }

    override fun isListLast(position: Int): Boolean {
        return position == 0
    }

    override fun isLast(position: Int): Boolean {
        val currentItem = getItem(position)
        val previousItem = previous(position)
        return when {
            currentItem == null ->
                false
            previousItem == null ->
                true
            currentItem.type == MessageCategory.SYSTEM_CONVERSATION.name ->
                true
            previousItem.type == MessageCategory.SYSTEM_CONVERSATION.name ->
                true
            previousItem.userId != currentItem.userId ->
                true
            !isSameDay(previousItem.createdAt, currentItem.createdAt) ->
                true
            else -> false
        }
    }

    private fun isFirst(position: Int): Boolean {
        return if (isGroup || recipient != null) {
            val currentItem = getItem(position)
            if (!isGroup && (recipient?.isBot() == false || recipient?.userId == currentItem?.userId)) {
                return false
            }
            val nextItem = next(position)
            when {
                currentItem == null ->
                    false
                nextItem == null ->
                    true
                nextItem.type == MessageCategory.SYSTEM_CONVERSATION.name ->
                    true
                nextItem.userId != currentItem.userId ->
                    true
                !isSameDay(nextItem.createdAt, currentItem.createdAt) ->
                    true
                else -> false
            }
        } else {
            false
        }
    }

    private fun previous(position: Int): MessageItem? {
        return if (position > 0) {
            getItem(position - 1)
        } else {
            null
        }
    }

    private fun next(position: Int): MessageItem? {
        return if (position < itemCount - 1) {
            getItem(position + 1)
        } else {
            null
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<MessageItem>?, currentList: PagedList<MessageItem>?) {
        super.onCurrentListChanged(previousList, currentList)
        if (currentList != null && previousList != null && previousList.size != 0) {
            val changeCount = currentList.size - previousList.size
            when {
                abs(changeCount) >= PAGE_SIZE -> notifyDataSetChanged()
                changeCount > 0 -> for (i in 1 until changeCount + 1)
                    getItem(i)?.let {
                        RxBus.publish(BlinkEvent(it.messageId, isLast(i)))
                    }
                changeCount < 0 -> notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasBottomView && isSecret) {
            2
        } else if (hasBottomView || isSecret) {
            1
        } else {
            0
        }
    }

    fun getRealItemCount(): Int {
        return super.getItemCount()
    }

    public override fun getItem(position: Int): MessageItem? {
        return if (position > itemCount - 1) {
            null
        } else if (isSecret && hasBottomView) {
            when (position) {
                0 -> create(
                    MessageCategory.STRANGER.name, if (super.getItemCount() > 0) {
                    super.getItem(0)?.createdAt
                } else {
                    null
                })
                itemCount - 1 -> create(MessageCategory.SECRET.name, if (super.getItemCount() > 0) {
                    super.getItem(super.getItemCount() - 1)?.createdAt
                } else {
                    null
                })
                else -> super.getItem(position - 1)
            }
        } else if (isSecret) {
            if (position == itemCount - 1) {
                create(MessageCategory.SECRET.name, if (super.getItemCount() > 0) {
                    super.getItem(super.getItemCount() - 1)?.createdAt
                } else {
                    null
                })
            } else {
                super.getItem(position)
            }
        } else if (hasBottomView) {
            if (position == 0) {
                create(MessageCategory.STRANGER.name, if (super.getItemCount() > 0) {
                    super.getItem(0)?.createdAt
                } else {
                    null
                })
            } else {
                super.getItem(position - 1)
            }
        } else {
            super.getItem(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            MESSAGE_TYPE -> {
                val item = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
                ConversationListFragment.MessageHolder(item)
            }
            else -> {
                val item = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_transparent, parent, false)
                TransparentHolder(item)
            }
        }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        getItem(holder.layoutPosition)?.let {
            (holder as BaseViewHolder).listen(it.messageId)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as BaseViewHolder).stopListen()
    }

    private fun getItemType(messageItem: MessageItem?): Int =
        notNullElse(messageItem, { item ->
            when {
                item.type == MessageCategory.STRANGER.name -> STRANGER_TYPE
                item.type == MessageCategory.SECRET.name -> SECRET_TYPE
                item.status == MessageStatus.FAILED.name -> WAITING_TYPE
                item.type == MessageCategory.SIGNAL_TEXT.name || item.type == MessageCategory.PLAIN_TEXT.name -> {
                    if (!item.quoteId.isNullOrEmpty() && !item.quoteContent.isNullOrEmpty()) {
                        REPLY_TYPE
                    } else if (!item.siteName.isNullOrBlank() || !item.siteDescription.isNullOrBlank()) {
                        LINK_TYPE
                    } else {
                        MESSAGE_TYPE
                    }
                }
                item.type == MessageCategory.SIGNAL_IMAGE.name ||
                    item.type == MessageCategory.PLAIN_IMAGE.name -> IMAGE_TYPE
                item.type == MessageCategory.SYSTEM_CONVERSATION.name -> INFO_TYPE
                item.type == MessageCategory.SYSTEM_ACCOUNT_SNAPSHOT.name -> BILL_TYPE
                item.type == MessageCategory.SIGNAL_DATA.name ||
                    item.type == MessageCategory.PLAIN_DATA.name -> FILE_TYPE
                item.type == MessageCategory.SIGNAL_STICKER.name ||
                    item.type == MessageCategory.PLAIN_STICKER.name -> STICKER_TYPE
                item.type == MessageCategory.APP_BUTTON_GROUP.name -> ACTION_TYPE
                item.type == MessageCategory.APP_CARD.name -> ACTION_CARD_TYPE
                item.type == MessageCategory.SIGNAL_CONTACT.name ||
                    item.type == MessageCategory.PLAIN_CONTACT.name -> CONTACT_CARD_TYPE
                item.type == MessageCategory.SIGNAL_VIDEO.name ||
                    item.type == MessageCategory.PLAIN_VIDEO.name -> VIDEO_TYPE
                item.type == MessageCategory.SIGNAL_AUDIO.name ||
                    item.type == MessageCategory.PLAIN_AUDIO.name -> AUDIO_TYPE
                item.isCallMessage() -> CALL_TYPE
                else -> UNKNOWN_TYPE
            }
        }, NULL_TYPE)

    override fun getItemViewType(position: Int): Int = getItemType(getItem(position))

    companion object {
        const val NULL_TYPE = -2
        const val UNKNOWN_TYPE = -1
        const val MESSAGE_TYPE = 0
        const val IMAGE_TYPE = 1
        const val INFO_TYPE = 2
        const val CARD_TYPE = 3
        const val BILL_TYPE = 4
        const val FILE_TYPE = 6
        const val STICKER_TYPE = 7
        const val ACTION_TYPE = 8
        const val ACTION_CARD_TYPE = 9
        const val REPLY_TYPE = 10
        const val WAITING_TYPE = 11
        const val LINK_TYPE = 12
        const val STRANGER_TYPE = 13
        const val SECRET_TYPE = 14
        const val CONTACT_CARD_TYPE = 15
        const val VIDEO_TYPE = 16
        const val AUDIO_TYPE = 17
        const val CALL_TYPE = 18

        private val diffCallback = object : DiffUtil.ItemCallback<MessageItem>() {
            override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
                return oldItem.mediaStatus == newItem.mediaStatus &&
                    oldItem.status == newItem.status &&
                    oldItem.userFullName == newItem.userFullName &&
                    oldItem.participantFullName == newItem.participantFullName &&
                    oldItem.sharedUserFullName == newItem.sharedUserFullName &&
                    oldItem.mediaSize == newItem.mediaSize
            }
        }
    }

    open class OnItemListener {

        open fun onSelect(isSelect: Boolean, messageItem: MessageItem, position: Int) {}

        open fun onLongClick(messageItem: MessageItem, position: Int): Boolean = true

        open fun onImageClick(messageItem: MessageItem, view: View) {}

        open fun onFileClick(messageItem: MessageItem) {}

        open fun onCancel(id: String) {}

        open fun onRetryUpload(messageId: String) {}

        open fun onRetryDownload(messageId: String) {}

        open fun onUserClick(userId: String) {}

        open fun onMentionClick(name: String) {}

        open fun onUrlClick(url: String) {}

        open fun onAddClick() {}

        open fun onBlockClick() {}

        open fun onActionClick(action: String) {}

        open fun onBillClick(messageItem: MessageItem) {}

        open fun onContactCardClick(userId: String) {}

        open fun onTransferClick(userId: String) {}

        open fun onMessageClick(messageId: String?) {}

        open fun onCallClick(messageItem: MessageItem) {}
    }

    fun addSelect(messageItem: MessageItem): Boolean {
        return selectSet.add(messageItem)
    }

    fun removeSelect(messageItem: MessageItem): Boolean {
        return selectSet.remove(selectSet.find { it.messageId == messageItem.messageId })
    }
}