package com.fone.android.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fone.android.api.FoneResponse
import com.fone.android.api.request.RelationshipRequest
import com.fone.android.api.response.ConversationResponse
import com.fone.android.repository.AccountRepository
import com.fone.android.repository.UserRepository
import com.fone.android.vo.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BottomSheetViewModel @Inject internal constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository

) : ViewModel() {
    fun join(code: String): Observable<FoneResponse<ConversationResponse>> =
        accountRepository.join(code).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun findUserById(id: String): LiveData<User> = userRepository.findUserById(id)

    fun updateRelationship(u: User, request: String) {
        userRepository.updateUserRelationship(RelationshipRequest(u.userId, request))
        userRepository.upsert(u)
    }

}