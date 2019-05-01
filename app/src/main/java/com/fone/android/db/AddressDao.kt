package com.fone.android.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.fone.android.vo.Address

@Dao
interface AddressDao : BaseDao<Address> {

    @Query("SELECT * FROM addresses WHERE asset_id = :id ORDER BY updated_at DESC")
    fun addresses(id: String): LiveData<List<Address>>

    @Query("DELETE FROM addresses WHERE address_id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM addresses")
    fun deleteAll()

    @Query("SELECT * FROM addresses WHERE address_id = :id")
    fun getById(id: String): LiveData<Address>
}