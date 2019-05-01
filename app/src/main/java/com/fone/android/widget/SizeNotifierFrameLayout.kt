package com.fone.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.fone.android.extension.appCompatActionBarHeight
import com.fone.android.extension.statusBarHeight

import java.lang.reflect.Field

class SizeNotifierFrameLayout : FrameLayout {

    private val rect = Rect()
    var backgroundImage: Drawable? = null
        set(bitmap) {
            field = bitmap
            invalidate()
        }
    private var keyboardHeight: Int = 0
    private var bottomClip: Int = 0

    constructor(context: Context) : super(context) {
        setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        keyboardHeight = getKeyboardHeight()
        setBottomClip(keyboardHeight)
    }

    private fun getKeyboardHeight(): Int {
        val rootView = rootView
        getWindowVisibleDisplayFrame(rect)
        val usableViewHeight = rootView.height -
            (if (rect.top != 0) context.statusBarHeight() else 0) - getViewInset(rootView)
        return usableViewHeight - (rect.bottom - rect.top)
    }

    private fun setBottomClip(value: Int) {
        bottomClip = value
    }

    override fun onDraw(canvas: Canvas) {
        if (backgroundImage != null) {
            if (backgroundImage is ColorDrawable) {
                if (bottomClip != 0) {
                    canvas.save()
                    canvas.clipRect(0, 0, measuredWidth, measuredHeight - bottomClip)
                }
                backgroundImage!!.setBounds(0, 0, measuredWidth, measuredHeight)
                backgroundImage!!.draw(canvas)
                if (bottomClip != 0) {
                    canvas.restore()
                }
            } else if (backgroundImage is BitmapDrawable) {
                val bitmapDrawable = backgroundImage as BitmapDrawable?
                if (bitmapDrawable!!.tileModeX == Shader.TileMode.REPEAT) {
                    canvas.save()
                    val scale: Float = 2.0f / context.resources.displayMetrics.density
                    canvas.scale(scale, scale)
                    backgroundImage!!.setBounds(0, 0, Math.ceil((measuredWidth / scale).toDouble()).toInt(),
                        Math.ceil((measuredHeight / scale).toDouble()).toInt())
                    backgroundImage!!.draw(canvas)
                    canvas.restore()
                } else {
                    val actionBarHeight = context.appCompatActionBarHeight()
                    val viewHeight = measuredHeight - actionBarHeight
                    val scaleX = measuredWidth.toFloat() / backgroundImage!!.intrinsicWidth.toFloat()
                    val scaleY = (viewHeight + keyboardHeight).toFloat() / backgroundImage!!.intrinsicHeight.toFloat()
                    val scale = if (scaleX < scaleY) scaleY else scaleX
                    val width = Math.ceil((backgroundImage!!.intrinsicWidth * scale).toDouble()).toInt()
                    val height = Math.ceil((backgroundImage!!.intrinsicHeight * scale).toDouble()).toInt()
                    val x = (measuredWidth - width) / 2
                    val y = (viewHeight - height + keyboardHeight) / 2 + actionBarHeight
                    if (bottomClip != 0) {
                        canvas.save()
                        canvas.clipRect(0, actionBarHeight, width, measuredHeight)
                    }
                    backgroundImage!!.setBounds(x, y, x + width, y + height)
                    backgroundImage!!.draw(canvas)
                    if (bottomClip != 0) {
                        canvas.restore()
                    }
                }
            }
        } else {
            super.onDraw(canvas)
        }
    }

    private var mAttachInfoField: Field? = null
    private var mStableInsetsField: Field? = null
    private fun getViewInset(view: View?): Int {
        if (view == null || view.height == displaySize.y || view.height == displaySize.y - context.statusBarHeight()) {
            return 0
        }
        try {
            if (mAttachInfoField == null) {
                mAttachInfoField = View::class.java.getDeclaredField("mAttachInfo")
                mAttachInfoField!!.isAccessible = true
            }
            val mAttachInfo = mAttachInfoField!!.get(view)
            if (mAttachInfo != null) {
                if (mStableInsetsField == null) {
                    mStableInsetsField = mAttachInfo.javaClass.getDeclaredField("mStableInsets")
                    mStableInsetsField!!.isAccessible = true
                }
                val insets = mStableInsetsField!!.get(mAttachInfo) as Rect
                return insets.bottom
            }
        } catch (e: Exception) {
        }

        return 0
    }

    private val displaySize by lazy {
        val displaySize = Point()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        display?.getSize(displaySize)
        displaySize
    }
}
