package com.fone.android.widget

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter

interface FoneStickyRecyclerHeadersAdapter<VH : RecyclerView.ViewHolder> : StickyRecyclerHeadersAdapter<VH> {
    fun onCreateAttach(parent: ViewGroup): View
    fun getAttachIndex(): Int?
    fun onBindAttachView(view: View)
    fun isLast(position: Int): Boolean
    fun isListLast(position: Int): Boolean
}
