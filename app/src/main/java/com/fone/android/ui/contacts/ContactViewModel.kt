package com.fone.android.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fone.android.api.FoneResponse
import com.fone.android.api.request.AccountUpdateRequest
import com.fone.android.repository.AccountRepository
import com.fone.android.repository.UserRepository
import com.fone.android.vo.Account
import com.fone.android.vo.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactViewModel
@Inject
internal constructor(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    fun getFriends(): LiveData<List<User>> = userRepository.findFriends()

    fun findSelf(): LiveData<User?> = userRepository.findSelf()

    fun update(request: AccountUpdateRequest): Observable<FoneResponse<Account>> =
        accountRepository.update(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun insertUser(user: User) {
        userRepository.upsert(user)
    }

    fun search(query: String): Observable<ArrayList<String>> =
        accountRepository.search(query).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}