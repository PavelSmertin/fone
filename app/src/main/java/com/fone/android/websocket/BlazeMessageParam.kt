package com.fone.android.websocket

import java.io.Serializable

data class BlazeMessageParam(
    val conversation_id: String?,
    val recipient_id: String?,
    val message_id: String?,
    val category: String?,
    val data: String?,
    val status: String? = null,
    val recipients: ArrayList<BlazeMessageParamSession>? = null,
    val messages: List<Any>? = null,
    val quote_message_id: String? = null,
    val session_id: String? = null,
    var primitive_id: String? = null,
    val primitive_message_id: String? = null,
    var representative_id: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 6L
    }
}

data class BlazeMessageParamSession(val user_id: String, val session_id: String? = null)

fun createAckParam(message_id: String, status: String) =
    BlazeMessageParam(null, null, message_id, null, null, status)

fun createAckListParam(messages: List<BlazeAckMessage>) =
    BlazeMessageParam(null, null, null, null, null, null, null, messages)

