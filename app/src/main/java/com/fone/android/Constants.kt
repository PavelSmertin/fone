package com.fone.android

object Constants {

    object API {
        const val DOMAIN = "https://fone.one"
        const val URL = "https://fone-238313.appspot.com/api/"
        const val WS_URL = "wss://blaze.fone.one"

        const val GIPHY_URL = "http://api.giphy.com/v1/"
    }

    object Account {
        const val PREF_SESSION = "pref_session"
        const val PREF_PIN_TOKEN = "pref_pin_token"
        const val PREF_NAME_ACCOUNT = "pref_name_account"
        const val PREF_NAME_TOKEN = "pref_name_token"
        const val PREF_SET_NAME = "pref_set_name"
        const val PREF_PIN_CHECK = "pref_pin_check"
        const val PREF_PIN_INTERVAL = "pref_pin_interval"
        const val PREF_PIN_ITERATOR = "pref_pin_iterator"
        const val PREF_CAMERA_TIP = "pref_camera_tip"
        const val PREF_LOGOUT_COMPLETE = "pref_logout_complete"
        const val PREF_BIOMETRICS = "pref_biometrics"
        const val PREF_WRONG_TIME = "pref_wrong_time"
        const val PREF_RESTORE = "pref_restore"
        const val PREF_SHOW_DEPOSIT_TIP = "pref_show_deposit_tip"
        const val PREF_FIRST_SHOW_DEPOSIT = "pref_first_show_deposit"
        const val PREF_EXTENSION_SESSION_ID = "pref_extension_session_id"
    }



    object DataBase {
        const val DB_NAME = "fone.db"
        const val MINI_VERSION = 15
        const val CURRENT_VERSION = 20
    }

    object BackUp {
        const val BACKUP_PERIOD = "backup_period"
        const val BACKUP_LAST_TIME = "backup_last_time"
    }

    const val SLEEP_MILLIS: Long = 1000
    const val INTERVAL_24_HOURS: Long = 1000 * 60 * 60 * 24
    const val INTERVAL_48_HOURS: Long = 1000 * 60 * 60 * 48
    const val INTERVAL_10_MINS: Long = 1000 * 60 * 10

    const val ARGS_USER = "args_user"
    const val ARGS_USER_ID = "args_user_id"


    const val Fone_Conversation_ID_HEADER = "Fone-Conversation-ID"
    val KEYS = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "<<")

    const val BATCH_SIZE = 700

    const val PAGE_SIZE = 20


}
