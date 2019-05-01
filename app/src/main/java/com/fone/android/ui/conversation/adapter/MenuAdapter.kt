package com.fone.android.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fone.android.R
import com.fone.android.extension.loadCircleImage
import com.fone.android.vo.App
import kotlinx.android.synthetic.main.item_chat_menu.view.*


class MenuAdapter(
    private val isGroup: Boolean,
    private val isBot: Boolean,
    private val isSelfCreatedBot: Boolean
) : RecyclerView.Adapter<MenuAdapter.MenuHolder>() {

    private val buildInMenus = arrayListOf<Menu>().apply {

    }

    var onMenuListener: OnMenuListener? = null

    var appList = listOf<App>()
        set(value) {
            if (field == value) return
            field = value

            menus = mutableListOf<Menu>().apply {
                addAll(buildInMenus)
            }
            for (app in appList) {
                menus.add(Menu(MenuType.App, null, null, app.icon_url, app.homeUri, app.name))
            }

            notifyDataSetChanged()
        }

    private var menus = mutableListOf<Menu>().apply {
        addAll(buildInMenus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MenuHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_menu, parent, false))

    override fun getItemCount() = menus.size

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val view = holder.itemView
        val ctx = view.context
        val menu = menus[position]
        if (menu.icon != null) {
            view.menu_icon.visibility = VISIBLE
            view.menu_icon.setImageResource(menu.icon)
            view.app_icon.visibility = GONE
            menu.nameRes?.let {
                view.menu_title.text = ctx.getString(it)
            }
        } else {
            view.app_icon.visibility = VISIBLE
            view.app_icon.loadCircleImage(menu.iconUrl)
            view.menu_icon.visibility = GONE
            view.menu_title.text = menu.name
        }
        view.setOnClickListener {
            onMenuListener?.onMenuClick(menu)
        }
    }

    class MenuHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnMenuListener {
        fun onMenuClick(menu: Menu)
    }
}