package com.fone.android.ui.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.fone.android.R
import com.fone.android.extension.addFragment
import com.fone.android.extension.openPermissionSetting
import com.fone.android.job.FoneJobManager
import com.fone.android.ui.common.BaseFragment
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.vo.User
import com.fone.android.vo.UserRelationship
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ir.mirrajabi.rxcontacts.Contact
import ir.mirrajabi.rxcontacts.RxContacts
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.view_title.view.*
import java.util.*
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var jobManager: FoneJobManager

    private val contactsViewModel: ContactViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ContactViewModel::class.java)
    }

    private val contactAdapter: ContactsAdapter by lazy {
        ContactsAdapter(context!!, Collections.emptyList(), 0)
    }

    companion object {
        const val TAG = "ContactsFragment"

        fun newInstance() = ContactsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_contacts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contact_recycler_view.adapter = contactAdapter
        contact_recycler_view.setHasFixedSize(true)
        contact_recycler_view.layoutManager = LinearLayoutManager(context)
        contact_recycler_view.addItemDecoration(StickyRecyclerHeadersDecoration(contactAdapter))
        val header = LayoutInflater.from(context).inflate(R.layout.view_contact_header, contact_recycler_view, false)
        contactAdapter.setHeader(header)
        if (!hasContactPermission()) {
            val footer = LayoutInflater.from(context)
                .inflate(R.layout.view_contact_list_empty, contact_recycler_view, false)
            contactAdapter.setFooter(footer)
        }
        contactAdapter.setContactListener(mContactListener)
        title_view.left_ib.setOnClickListener { activity?.onBackPressed() }
        //title_view.right_animator.setOnClickListener { SettingActivity.show(context!!) }

        if (hasContactPermission()) {
            fetchContacts()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contactsViewModel.getFriends().observe(this, Observer { users ->
            if (users != null && users.isNotEmpty()) {
                if (!hasContactPermission()) {
                    contactAdapter.friendSize = users.size
                    contactAdapter.users = users
                } else {
                    val newList = arrayListOf<User>().apply {
                        addAll(users)
                        addAll(contactAdapter.users.filter { it.relationship != UserRelationship.FRIEND.name })
                    }
                    contactAdapter.friendSize = users.size
                    contactAdapter.users = newList
                }
            } else {
                if (!hasContactPermission()) {
                    contactAdapter.users = Collections.emptyList()
                }
            }
            contactAdapter.notifyDataSetChanged()
        })
        contactsViewModel.findSelf().observe(this, Observer { self ->
            if (self != null) {
                contactAdapter.me = self
                contactAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun hasContactPermission() =
        context!!.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun fetchContacts() {
        RxContacts.fetch(context!!)
            .toSortedList(Contact::compareTo)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .autoDisposable(scopeProvider)
            .subscribe({ contacts ->
                val mutableList = mutableListOf<User>()
                for (item in contacts) {
                    item.phoneNumbers.mapTo(mutableList) {
                        User("",
                            "", "contact", item.displayName,
                            "", it, false, "", null)
                    }
                }
                mutableList.addAll(0, contactAdapter.users)
                contactAdapter.users = mutableList
                contactAdapter.notifyDataSetChanged()
            }, { _ -> })
//        jobManager.addJobInBackground(UploadContactsJob())
    }

    private val mContactListener: ContactsAdapter.ContactListener = object : ContactsAdapter.ContactListener {

        override fun onHeaderRl() {
            activity?.addFragment(this@ContactsFragment, ProfileFragment.newInstance(), ProfileFragment.TAG)
        }

        override fun onNewGroup() {
//            GroupActivity.show(context!!)
        }

        override fun onAddContact() {
            activity?.addFragment(this@ContactsFragment, AddPeopleFragment.newInstance(), AddPeopleFragment.TAG)
        }

        @SuppressLint("CheckResult")
        override fun onEmptyRl() {
            RxPermissions(activity!!)
                .request(Manifest.permission.READ_CONTACTS)
                .subscribe { granted ->
                    if (granted) {
                        contactAdapter.removeFooter()
//                        jobManager.addJobInBackground(UploadContactsJob())
//                        fetchContacts()
//                        WorkManager.getInstance().enqueueOneTimeNetworkWorkRequest<RefreshContactWorker>()
                    } else {
                        context?.openPermissionSetting()
                    }
                }
        }

        override fun onFriendItem(user: User) {
            context?.let { ctx -> ConversationActivity.show(ctx, null, user.userId) }
        }

        override fun onContactItem(user: User) {
            ContactBottomSheetDialog.newInstance(user).showNow(requireFragmentManager(), ContactBottomSheetDialog.TAG)
        }

    }
}