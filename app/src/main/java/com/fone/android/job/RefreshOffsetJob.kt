package com.fone.android.job

import com.birbit.android.jobqueue.Params
import com.fone.android.extension.getEpochNano
import com.fone.android.extension.nowInUtc
import com.fone.android.vo.Offset
import com.fone.android.vo.STATUS_OFFSET
import java.util.*

class RefreshOffsetJob : FoneJob(Params(PRIORITY_UI_HIGH)
    .setSingleId(GROUP).requireNetwork(), UUID.randomUUID().toString()) {

    override fun cancel() {
    }

    companion object {
        private const val serialVersionUID = 1L
        const val GROUP = "RefreshOffsetJob"
    }
    override fun onRun() {
        val statusOffset = offsetDao.getStatusOffset()
        var status = statusOffset?.getEpochNano() ?: nowInUtc().getEpochNano()
        while (true) {
            val response = messageService.messageStatusOffset(status).execute().body()
            if (response != null && response.isSuccess && response.data != null) {
                val blazeMessages = response.data!!
                if (blazeMessages.count() == 0) {
                    break
                }
                for (m in blazeMessages) {
                    makeMessageStatus(m.status, m.messageId)
                    offsetDao.insert(Offset(STATUS_OFFSET, m.updatedAt))
                }
                if (blazeMessages.count() > 0 && blazeMessages.last().updatedAt.getEpochNano() == status) {
                    break
                }
                status = blazeMessages.last().updatedAt.getEpochNano()
            } else {
                break
            }
        }
    }
}