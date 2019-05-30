package com.fone.android.repository


import androidx.lifecycle.LiveData
import com.fone.android.api.request.RelationshipAction
import com.fone.android.api.request.RelationshipRequest
import com.fone.android.db.AppDao
import com.fone.android.db.UserDao
import com.fone.android.db.insertUpdate
import com.fone.android.util.SINGLE_DB_THREAD
import com.fone.android.util.Session
import com.fone.android.vo.User
import com.fone.android.vo.UserRelationship
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository
@Inject
constructor(private val userDao: UserDao, private val appDao: AppDao) {

    fun findFriends(): LiveData<List<User>> = userDao.findFriends()

    fun getUserById(id: String): User? = userDao.findUser(id)

    fun findContactByConversationId(conversationId: String): User? =
        userDao.findContactByConversationId(conversationId)

    fun upsert(user: User) {
        GlobalScope.launch(SINGLE_DB_THREAD) {
            userDao.insertUpdate(user, appDao)
        }
    }

    fun updateUserRelationship(request: RelationshipRequest) {
        if(RelationshipAction.valueOf(request.action) == RelationshipAction.ADD)
            userDao.updateUserRelationship(request.user_id, UserRelationship.FRIEND.name)
        if(RelationshipAction.valueOf(request.action) == RelationshipAction.REMOVE)
            userDao.updateUserRelationship(request.user_id, UserRelationship.STRANGER.name)
        if(RelationshipAction.valueOf(request.action) == RelationshipAction.BLOCK)
            userDao.updateUserRelationship(request.user_id, UserRelationship.BLOCKING.name)
        if(RelationshipAction.valueOf(request.action) == RelationshipAction.UNBLOCK)
            userDao.updateUserRelationship(request.user_id, UserRelationship.STRANGER.name)

    }

    fun updatePhone(id: String, phone: String) = userDao.updatePhone(id, phone)

    fun findSelf(): LiveData<User?> = userDao.findSelf(Session.getAccountId() ?: "")

    fun findUserById(query: String): LiveData<User> = userDao.findUserById(query)



}