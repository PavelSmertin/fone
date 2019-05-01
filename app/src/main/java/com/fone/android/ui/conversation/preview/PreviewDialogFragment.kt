package com.fone.android.ui.conversation.preview

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.FoneDialogFragment
import androidx.fragment.app.FragmentManager
import com.fone.android.R
import com.fone.android.extension.loadImage
import kotlinx.android.synthetic.main.fragment_preview.view.*


class PreviewDialogFragment : FoneDialogFragment() {

    companion object {
        const val IS_VIDEO: String = "IS_VIDEO"
        fun newInstance(isVideo: Boolean = false): PreviewDialogFragment {
            val previewDialogFragment = PreviewDialogFragment()
            previewDialogFragment.arguments = bundleOf(
                IS_VIDEO to isVideo
            )
            return previewDialogFragment
        }
    }


    private var mediaDialogView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mediaDialogView = inflater.inflate(R.layout.fragment_preview, null, false)

        mediaDialogView!!.dialog_close_iv.setOnClickListener {
            dismiss()
        }
        return mediaDialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setWindowAnimations(R.style.BottomSheet_Animation)
        dialog.setOnShowListener {
            mediaDialogView!!.dialog_send_ib.setOnClickListener { action!!(uri!!); dismiss() }
            mediaDialogView!!.dialog_iv.loadImage(uri)
        }
    }

    private var uri: Uri? = null
    private var action: ((Uri) -> Unit)? = null
    fun show(fragmentManager: FragmentManager, uri: Uri, action: (Uri) -> Unit) {
        super.showNow(fragmentManager, "PreviewDialogFragment")
        this.uri = uri
        this.action = action
    }

}
