package com.fone.android.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FoneDialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.fone.android.R
import com.fone.android.di.Injectable
import com.fone.android.widget.BottomSheet
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import javax.inject.Inject

abstract class FoneBottomSheetDialogFragment : FoneDialogFragment(), Injectable {

    protected lateinit var contentView: View
    protected val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val bottomViewModel: BottomSheetViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BottomSheetViewModel::class.java)
    }

    override fun getTheme() = R.style.AppTheme_Dialog

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheet {
        return BottomSheet.Builder(requireActivity(), true).create()
    }

    override fun dismiss() {
        if (isAdded) {
            try {
                dialog.dismiss()
                onDismissListener?.onDismiss()
            } catch (e: IllegalStateException) {
                super.dismissAllowingStateLoss()
            }
        }
    }

    var onDismissListener: OnDismissListener? = null

    interface OnDismissListener {
        fun onDismiss()
    }
}