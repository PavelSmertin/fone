package com.fone.android.ui.common

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.fone.android.Constants.ARGS_USER
import com.fone.android.R
import com.fone.android.api.request.RelationshipAction
import com.fone.android.extension.addFragment
import com.fone.android.extension.notNullElse
import com.fone.android.ui.contacts.ProfileFragment
import com.fone.android.ui.conversation.ConversationActivity
import com.fone.android.ui.conversation.holder.BaseViewHolder
import com.fone.android.util.Session
import com.fone.android.vo.User
import com.fone.android.vo.UserRelationship
import com.fone.android.widget.BottomSheet
import com.fone.android.widget.linktext.AutoLinkMode
import kotlinx.android.synthetic.main.fragment_user_bottom_sheet.view.*
import org.threeten.bp.Instant

class UserBottomSheetDialogFragment : FoneBottomSheetDialogFragment() {

    companion object {
        const val ARGS_CONVERSATION_ID = "args_conversation_id"

        const val TAG = "UserBottomSheetDialog"

        const val MUTE_8_HOURS = 8 * 60 * 60
        const val MUTE_1_WEEK = 7 * 24 * 60 * 60
        const val MUTE_1_YEAR = 365 * 24 * 60 * 60

        fun newInstance(user: User, conversationId: String? = null) = UserBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGS_USER, user)
                putString(ARGS_CONVERSATION_ID, conversationId)
            }
        }
    }

    private lateinit var user: User
    // bot need conversation id
    private var conversationId: String? = null
    private lateinit var menu: AlertDialog

    private var keepDialog = false

    var showUserTransactionAction: (() -> Unit)? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.fragment_user_bottom_sheet, null)
        (dialog as BottomSheet).setCustomView(contentView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user = arguments!!.getParcelable(ARGS_USER)!!
        conversationId = arguments!!.getString(ARGS_CONVERSATION_ID)
        contentView.left_iv.setOnClickListener { dismiss() }
        contentView.avatar.setOnClickListener {
            user.avatarUrl.let { url ->
                if (!url.isNullOrBlank()) {
                    //AvatarActivity.show(requireActivity(), url, contentView.avatar)
                    dismiss()
                }
            }
        }

        bottomViewModel.findUserById(user.userId).observe(this, Observer { u ->
            if (u == null) return@Observer
            // prevent add self
            if (u.userId == Session.getAccountId()) {
                activity?.addFragment(this@UserBottomSheetDialogFragment, ProfileFragment.newInstance(), ProfileFragment.TAG)
                dismiss()
                return@Observer
            }
            user = u
            updateUserInfo(u)
            //initMenu()
        })
        contentView.add_fl.setOnClickListener {
            updateRelationship(UserRelationship.FRIEND.name)
        }
        contentView.send_fl.setOnClickListener {
            context?.let { ctx -> ConversationActivity.show(ctx, null, user.userId) }
            dismiss()
        }

        contentView.detail_tv.movementMethod = LinkMovementMethod()
        contentView.detail_tv.addAutoLinkMode(AutoLinkMode.MODE_URL)
        contentView.detail_tv.setUrlModeColor(BaseViewHolder.LINK_COLOR)
//        contentView.detail_tv.setAutoLinkOnClickListener { _, url ->
//            openUrlWithExtraWeb(url, conversationId, requireFragmentManager())
//            dismiss()
//        }

//        bottomViewModel.refreshUser(user.userId, true)
    }

    private fun initMenu() {
        val choices = mutableListOf<String>()
        choices.add(getString(R.string.contact_other_share))
        choices.add(getString(R.string.contact_other_transactions))
        when (user.relationship) {
            UserRelationship.BLOCKING.name -> {
            }
            UserRelationship.FRIEND.name -> {
                choices.add(getString(R.string.edit_name))
                setMute(choices)
                choices.add(getString(R.string.contact_other_remove))
            }
            UserRelationship.STRANGER.name -> {
                setMute(choices)
                choices.add(getString(R.string.contact_other_block))
            }
        }
        menu = AlertDialog.Builder(context!!)
            .setItems(choices.toTypedArray()) { _, which ->
                when (choices[which]) {
                    getString(R.string.contact_other_share) -> {
//                        ForwardActivity.show(context!!, arrayListOf(ForwardMessage(ForwardCategory.CONTACT.name, sharedUserId = user.userId)), true)
//                        dismiss()
                    }
                    getString(R.string.edit_name) -> {
//                        keepDialog = true
//                        showDialog(user.fullName)
                    }
                    getString(R.string.un_mute) -> {
//                        unMute()
                    }
                    getString(R.string.mute) -> {
//                        keepDialog = true
//                        mute()
                    }
                    getString(R.string.contact_other_block) -> {
//                        bottomViewModel.updateRelationship(RelationshipRequest(user.userId, RelationshipAction.BLOCK.name))
                    }
                    getString(R.string.contact_other_remove) -> {
//                        updateRelationship(UserRelationship.STRANGER.name)
                    }
                }
            }.create()
        menu.setOnDismissListener {
            if (!keepDialog) {
                dismiss()
            }
        }

        contentView.right_iv.setOnClickListener {
            (dialog as BottomSheet).fakeDismiss()
            menu.show()
        }
    }

    private fun setMute(choices: MutableList<String>) {
        if (notNullElse(user.muteUntil, {
                Instant.now().isBefore(Instant.parse(it))
            }, false)) {
            choices.add(getString(R.string.un_mute))
        } else {
            choices.add(getString(R.string.mute))
        }
    }

    private fun updateUserInfo(user: User) {
        contentView.avatar.setInfo(user.fullName, user.avatarUrl, user.userId)
        contentView.name.text = user.fullName
        contentView.id_tv.text = getString(R.string.contact_mixin_id, user.identityNumber)
        contentView.verified_iv.visibility = if (user.isVerified != null && user.isVerified) View.VISIBLE else View.GONE

        contentView.creator_tv.visibility = View.GONE
        contentView.bot_iv.visibility = View.GONE
        contentView.detail_tv.visibility = View.GONE
        contentView.open_fl.visibility = View.GONE

        updateUserStatus(user.relationship)

    }

    private fun updateRelationship(relationship: String) {
        updateUserStatus(relationship)
        bottomViewModel.updateRelationship(user, RelationshipAction.ADD.name)
    }

    private fun updateUserStatus(relationship: String) {
        when (relationship) {
            UserRelationship.BLOCKING.name -> {
                contentView.send_fl.visibility = View.GONE
                contentView.add_fl.visibility = View.GONE
                contentView.unblock_fl.visibility = View.VISIBLE
                contentView.unblock_fl.setOnClickListener {
                    bottomViewModel.updateRelationship(user, RelationshipAction.UNBLOCK.name)
                    dismiss()
                }
            }
            UserRelationship.FRIEND.name -> {
                contentView.add_fl.visibility = View.GONE
                contentView.send_fl.visibility = View.VISIBLE
                contentView.send_fl.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = resources.getDimensionPixelOffset(R.dimen.activity_vertical_margin)
                }
                contentView.unblock_fl.visibility = View.GONE
            }
            UserRelationship.STRANGER.name -> {
                contentView.add_fl.visibility = View.VISIBLE
                contentView.send_fl.visibility = View.VISIBLE
                contentView.add_fl.updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = resources.getDimensionPixelOffset(R.dimen.activity_vertical_margin)
                }
                contentView.unblock_fl.visibility = View.GONE
            }
        }
    }

}