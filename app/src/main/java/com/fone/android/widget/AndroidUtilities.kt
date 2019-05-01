package com.fone.android.widget

import android.util.DisplayMetrics
import com.fone.android.FoneApplication
import org.jetbrains.anko.dip

object AndroidUtilities {
    var displayMetrics = DisplayMetrics()
    fun getPixelsInCM(cm: Float, isX: Boolean): Float {
        return cm / 2.54f * if (isX) displayMetrics.xdpi else displayMetrics.ydpi
    }

    fun dp(value: Float): Int {
        return FoneApplication.appContext.dip(value)
    }
}
