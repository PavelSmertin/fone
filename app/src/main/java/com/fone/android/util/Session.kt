package com.fone.android.util

import com.fone.android.Constants
import com.fone.android.FoneApplication
import com.fone.android.extension.*
import com.fone.android.vo.Account
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Session {
    companion object {

        private var self: Account? = null

        fun getAccount(): Account? = if (self != null) {
            self
        } else {
            val preference = FoneApplication.appContext.sharedPreferences(Constants.Account.PREF_SESSION)
            val json = preference.getString(Constants.Account.PREF_NAME_ACCOUNT, "")
            if (!json.isNullOrBlank()) {
                Gson().fromJson<Account>(json, object : TypeToken<Account>() {}.type)
            } else {
                null
            }
        }


        @JvmStatic
        fun getAccountId(): String? {
            val account = Session.getAccount()
            return account?.userId
        }

        fun signToken(acct: Account?, request: Request): String {
            val token = getToken()
            if (acct == null || token == null || token.isBlank()) {
                return ""
            }
            val key = getRSAPrivateKeyFromString(token)
            val expire = System.currentTimeMillis() / 1000 + 1800
            val iat = System.currentTimeMillis() / 1000

            var content = "${request.method()}${request.url().cutOut()}"
            if (request.body() != null && request.body()!!.contentLength() > 0) {
                content += request.body()!!.bodyToString()
            }
            return Jwts.builder()
                .setClaims(ConcurrentHashMap<String, Any>().apply {
                    put(Claims.ID, UUID.randomUUID().toString())
                    put(Claims.EXPIRATION, expire)
                    put(Claims.ISSUED_AT, iat)
                    put("uid", acct.userId)
                    put("sid", acct.session_id)
                    put("sig", content.sha256().toHex())
                    put("scp", "FULL")
                })
                .signWith(SignatureAlgorithm.RS512, key)
                .compact()
        }

        fun getToken(): String? {
            val preference = FoneApplication.appContext.sharedPreferences(Constants.Account.PREF_SESSION)
            return preference.getString(Constants.Account.PREF_NAME_TOKEN, null)
        }
    }
}

