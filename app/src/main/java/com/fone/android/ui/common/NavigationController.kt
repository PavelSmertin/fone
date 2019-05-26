package com.fone.android.ui.common

import androidx.fragment.app.FragmentManager
import com.fone.android.R
import com.fone.android.ui.contacts.ContactsActivity
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
        ContactsActivity.show(context)
    }

    fun navigateToMessage() {
        val conversationListFragment = ConversationListFragment.newInstance()
        fragmentManager.beginTransaction()
            .replace(containerId, conversationListFragment)
            .commitAllowingStateLoss()
    }

    fun showSearch() {
//        val searchFragment = SearchFragment.getInstance()
//        fragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
//            .add(containerId, searchFragment)
//            .commitAllowingStateLoss()
    }

    fun hideSearch() {
//        fragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
//            .remove(SearchFragment.getInstance())
//            .commitAllowingStateLoss()
    }

}
