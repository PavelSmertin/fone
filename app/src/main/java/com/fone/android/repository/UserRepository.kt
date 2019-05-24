package com.fone.android.repository


import com.fone.android.db.AppDao
import com.fone.android.db.UserDao
import com.fone.android.db.insertUpdate
import com.fone.android.util.SINGLE_DB_THREAD
import com.fone.android.vo.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository
@Inject
constructor(private val userDao: UserDao, private val appDao: AppDao) {

    fun getUserById(id: String): User? = userDao.findUser(id)

    fun findContactByConversationId(conversationId: String): User? =
        userDao.findContactByConversationId(conversationId)

    fun upsert(user: User) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            userDao.insertUpdate(user, appDao)
        }
    }

    fun updatePhone(id: String, phone: String) = userDao.updatePhone(id, phone)

}