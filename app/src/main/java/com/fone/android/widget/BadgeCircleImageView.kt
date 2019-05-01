package com.fone.android.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.fone.android.R
import com.fone.android.extension.dpToPx
import kotlinx.android.synthetic.main.view_badge_circle_image.view.*


open class BadgeCircleImageView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_badge_circle_image, this, true)
    }

    var pos: Int = START_BOTTOM

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChild(badge, MeasureSpec.makeMeasureSpec(measuredWidth / 4, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight / 4, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val i = measuredWidth / 8
        if (pos == START_BOTTOM) {
            badge.layout(0, 5 * i, 2 * i, 7 * i)
        } else if (pos == END_BOTTOM) {
            badge.layout(5 * i, 5 * i, 7 * i, 7 * i)
        }
    }

    fun setBorder(width: Float = 2f, color: Int = Color.WHITE) {
        bg.borderWidth = context.dpToPx(width)
        bg.borderColor = color
    }

    companion object {
        const val START_BOTTOM = 0
        const val END_BOTTOM = 1
    }
}