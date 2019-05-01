package com.fone.android.ui.conversation.holder

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.fone.android.R
import com.fone.android.extension.notNullElse
import com.fone.android.extension.timeAgoClock
import com.fone.android.ui.conversation.adapter.ConversationAdapter
import com.fone.android.vo.MessageItem
import com.fone.android.widget.linktext.AutoLinkMode
import kotlinx.android.synthetic.main.date_wrapper.view.*
import kotlinx.android.synthetic.main.item_chat_action.view.chat_name
import kotlinx.android.synthetic.main.item_chat_message.view.*
import org.jetbrains.anko.dip

class MessageHolder constructor(containerView: View) : BaseViewHolder(containerView) {

    init {
        itemView.chat_tv.addAutoLinkMode(AutoLinkMode.MODE_URL)
        itemView.chat_tv.setUrlModeColor(LINK_COLOR)

        itemView.chat_tv.setAutoLinkOnClickListener { autoLinkMode, matchedText ->
            when (autoLinkMode) {
                AutoLinkMode.MODE_URL -> {
                    onItemListener?.onUrlClick(matchedText)
                }
                AutoLinkMode.MODE_MENTION -> {
                    onItemListener?.onMentionClick(matchedText)
                }
                else -> {
                }
            }
        }
    }

    override fun chatLayout(isMe: Boolean, isLast: Boolean, isBlink: Boolean) {
        super.chatLayout(isMe, isLast, isBlink)
        val lp = (itemView.chat_layout.layoutParams as ConstraintLayout.LayoutParams)
        if (isMe) {
            lp.horizontalBias = 1f
            if (isLast) {
                itemView.chat_layout.setBackgroundResource(R.drawable.chat_bubble_me_last)
            } else {
                itemView.chat_layout.setBackgroundResource(R.drawable.chat_bubble_me)
            }
        } else {
            lp.horizontalBias = 0f
            if (isLast) {
                itemView.chat_layout.setBackgroundResource(R.drawable.chat_bubble_other_last)
            } else {
                itemView.chat_layout.setBackgroundResource(R.drawable.chat_bubble_other)
            }
        }
    }

    private var onItemListener: ConversationAdapter.OnItemListener? = null

    fun bind(
        messageItem: MessageItem,
        keyword: String?,
        isLast: Boolean,
        isFirst: Boolean = false,
        hasSelect: Boolean,
        isSelect: Boolean,
        onItemListener: ConversationAdapter.OnItemListener
    ) {
        this.onItemListener = onItemListener
        if (hasSelect && isSelect) {
            itemView.setBackgroundColor(SELECT_COLOR)
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        itemView.chat_tv.setOnLongClickListener {
            if (!hasSelect) {
                onItemListener.onLongClick(messageItem, adapterPosition)
            } else {
                onItemListener.onSelect(!isSelect, messageItem, adapterPosition)
                true
            }
        }

        itemView.chat_tv.setOnClickListener {
            if (hasSelect) {
                onItemListener.onSelect(!isSelect, messageItem, adapterPosition)
            }
        }

        itemView.setOnLongClickListener {
            if (!hasSelect) {
                onItemListener.onLongClick(messageItem, adapterPosition)
            } else {
                onItemListener.onSelect(!isSelect, messageItem, adapterPosition)
                true
            }
        }

        itemView.setOnClickListener {
            if (hasSelect) {
                onItemListener.onSelect(!isSelect, messageItem, adapterPosition)
            }
        }

        notNullElse(keyword, { k ->
            messageItem.content?.let { str ->
                val start = str.indexOf(k, 0, true)
                if (start >= 0) {
                    val sp = SpannableString(str)
                    sp.setSpan(BackgroundColorSpan(HIGHLIGHTED), start,
                        start + k.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    itemView.chat_tv.text = sp
                } else {
                    itemView.chat_tv.text = messageItem.content
                }
            }
        }, {
            itemView.chat_tv.text = messageItem.content
        })
        val isMe = meId == messageItem.userId
        if (isFirst && !isMe) {
            itemView.chat_name.visibility = View.VISIBLE
            itemView.chat_name.text = messageItem.userFullName
            if (messageItem.appId != null) {
                itemView.chat_name.setCompoundDrawables(null, null, botIcon, null)
                itemView.chat_name.compoundDrawablePadding = itemView.dip(3)
            } else {
                itemView.chat_name.setCompoundDrawables(null, null, null, null)
            }
            itemView.chat_name.setTextColor(getColorById(messageItem.userId))
            itemView.chat_name.setOnClickListener { onItemListener.onUserClick(messageItem.userId) }
        } else {
            itemView.chat_name.visibility = View.GONE
        }

        if (messageItem.appId != null) {
            itemView.chat_name.setCompoundDrawables(null, null, botIcon, null)
            itemView.chat_name.compoundDrawablePadding = itemView.dip(3)
        } else {
            itemView.chat_name.setCompoundDrawables(null, null, null, null)
        }
        itemView.chat_time.timeAgoClock(messageItem.createdAt)
        setStatusIcon(isMe, messageItem.status, {
            itemView.chat_flag.setImageDrawable(it)
            itemView.chat_flag.visibility = View.VISIBLE
        }, {
            itemView.chat_flag.visibility = View.GONE
        })

        chatLayout(isMe, isLast)
    }
}