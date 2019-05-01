package com.fone.android.ui.common

import androidx.fragment.app.FragmentManager
import com.fone.android.R
import com.fone.android.ui.home.ConversationListFragment
import com.fone.android.ui.home.MainActivity
import javax.inject.Inject

class NavigationController
@Inject
constructor(mainActivity: MainActivity) {
    private val containerId: Int = R.id.container
    private val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
    private val context = mainActivity

    fun pushContacts() {
        //ContactsActivity.show(context)
    }

    fun navigateToMessage() {
        val conversationListFragment = ConversationListFragment.newInstance()
        fragmentManager.beginTransaction()
            .replace(containerId, conversationListFragment)
            .commitAllowingStateLoss()
    }

}
