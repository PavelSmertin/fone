package com.fone.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.fone.android.R
import kotlinx.android.synthetic.main.view_title.view.*

class TitleView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_title, this, true)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TitleView)
        if (ta != null) {
            if (ta.hasValue(R.styleable.TitleView_titleText)) {
                title_tv.text = ta.getString(R.styleable.TitleView_titleText)
            }
            if (ta.hasValue(R.styleable.TitleView_rightIcon)) {
                right_ib.setImageResource(ta.getResourceId(R.styleable.TitleView_rightIcon, 0))
            }
            if (ta.hasValue(R.styleable.TitleView_rightText)) {
                right_tv.text = ta.getString(R.styleable.TitleView_rightText)
                right_animator.displayedChild = POS_TEXT
            }
            if (ta.hasValue(R.styleable.TitleView_rightTextColor)) {
                right_tv.setTextColor(ta.getColor(R.styleable.TitleView_rightTextColor,
                    ContextCompat.getColor(context, R.color.text_gray)))
                right_animator.displayedChild = POS_TEXT
            }
            if (ta.hasValue(R.styleable.TitleView_leftIcon)) {
                left_ib.setImageResource(ta.getResourceId(R.styleable.TitleView_leftIcon, 0))
            }
            if (ta.hasValue(R.styleable.TitleView_titleColor)) {
                title_tv.setTextColor(ta.getColor(R.styleable.TitleView_titleColor,
                    ContextCompat.getColor(context, R.color.black)))
            }
            if (ta.hasValue(R.styleable.TitleView_android_background)) {
                setBackgroundResource(ta.getResourceId(R.styleable.TitleView_android_background,
                    ContextCompat.getColor(context, R.color.white)))
            } else {
                setBackgroundResource(R.color.white)
            }
            if (ta.hasValue(R.styleable.TitleView_need_divider)) {
                divider.visibility = if (ta.getBoolean(R.styleable.TitleView_need_divider, true)) VISIBLE else GONE
            }
            if (ta.hasValue(R.styleable.TitleView_rightIcon) || ta.hasValue(R.styleable.TitleView_rightText)) {
                right_animator.visibility = View.VISIBLE
            } else {
                right_animator.visibility = View.GONE
            }
            ta.recycle()
        }
    }

    fun setSubTitle(first: String, second: String) {
        title_tv.text = first
        if (second.isBlank()) {
            sub_title_tv.visibility = View.GONE
        } else {
            sub_title_tv.visibility = View.VISIBLE
            sub_title_tv.text = second
        }
    }

    companion object {
        val POS_TEXT = 1
    }
}