package com.fone.android.event

import com.fone.android.widget.CircleProgress.Companion.STATUS_ERROR


class ProgressEvent(val id: String, var progress: Float, val status: Int = STATUS_ERROR)
