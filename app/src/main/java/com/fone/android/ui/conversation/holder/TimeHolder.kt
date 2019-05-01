package com.fone.android.ui.conversation.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fone.android.extension.timeAgoDate
import kotlinx.android.synthetic.main.item_chat_time.view.*

class TimeHolder constructor(containerView: View) : RecyclerView.ViewHolder(containerView) {
    fun bind(time: String) {
        itemView.chat_time.timeAgoDate(time)
    }
}