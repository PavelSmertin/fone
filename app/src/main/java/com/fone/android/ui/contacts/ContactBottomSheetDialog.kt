package com.fone.android.ui.contacts

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.fone.android.Constants.ARGS_USER
import com.fone.android.R
import com.fone.android.ui.common.FoneBottomSheetDialogFragment
import com.fone.android.vo.User
import com.fone.android.widget.BottomSheet
import kotlinx.android.synthetic.main.fragment_contact_bottom_sheet.view.*


class ContactBottomSheetDialog : FoneBottomSheetDialogFragment() {

    companion object {
        const val TAG = "ContactBottomSheetDialog"

        fun newInstance(user: User) = ContactBottomSheetDialog().apply {
            arguments = Bundle().apply {
                putParcelable(ARGS_USER, user)
            }
        }
    }

    private val user: User by lazy { arguments!!.getParcelable<User>(ARGS_USER) }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.fragment_contact_bottom_sheet, null)
        (dialog as BottomSheet).setCustomView(contentView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contentView.index.text = if (user.fullName != null && user.fullName!!.isNotEmpty()) user.fullName!![0].toString() else ""
        contentView.name.text = user.fullName
        contentView.mobile_tv.text = getString(R.string.contact_mobile, user.phone)
        contentView.invite_tv.setOnClickListener { openSms(user.phone) }
    }

    private fun openSms(mobile: String?) {
        if (mobile == null) {
            return
        }
        val smsUri = Uri.parse("smsto:$mobile")
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
        smsIntent.putExtra("sms_body", getString(R.string.contact_less_invite_content))
        startActivity(smsIntent)
    }
}