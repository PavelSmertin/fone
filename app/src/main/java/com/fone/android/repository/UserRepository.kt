package com.fone.android.repository


import com.fone.android.db.UserDao
import com.fone.android.vo.User


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository
@Inject
constructor(private val userDao: UserDao) {

    fun getUserById(id: String): User? = userDao.findUser(id)

    fun findContactByConversationId(conversationId: String): User? =
        userDao.findContactByConversationId(conversationId)

}