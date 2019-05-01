package com.fone.android.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.fone.android.R
import com.fone.android.extension.dpToPx
import com.fone.android.extension.round
import com.fone.android.ui.conversation.holder.BaseViewHolder
import com.fone.android.vo.MessageItem
import kotlinx.android.synthetic.main.view_reply.view.*
import org.jetbrains.anko.dip

class ReplyView constructor(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_reply, this, true)
        setBackgroundColor(Color.WHITE)
        reply_view_iv.round(dip(3))
    }

    private val dp72 by lazy {
        context.dpToPx(72f)
    }
    private val dp12 by lazy {
        context.dpToPx(12f)
    }

    private fun setIcon(@DrawableRes icon: Int) = AppCompatResources.getDrawable(context, icon)?.also {
        it.setBounds(0, 0, dp12, dp12)
    }.let {
        TextViewCompat.setCompoundDrawablesRelative(reply_view_tv, it, null, null, null)
    }

    var messageItem: MessageItem? = null
    fun bind(messageItem: MessageItem) {
        this.messageItem = messageItem
        reply_start_view.setBackgroundColor(BaseViewHolder.getColorById(messageItem.userId))
        reply_name_tv.setTextColor(BaseViewHolder.getColorById(messageItem.userId))
        when {
            messageItem.type.endsWith("_TEXT") -> {
                reply_view_tv.text = messageItem.content
                TextViewCompat.setCompoundDrawablesRelative(reply_view_tv, null, null, null, null)
                (reply_view_tv.layoutParams as ConstraintLayout.LayoutParams).endToStart = R.id.reply_close_iv
                reply_view_iv.visibility = View.GONE
                reply_avatar.visibility = View.GONE
            }
        }
        reply_name_tv.text = messageItem.userFullName
    }
}
