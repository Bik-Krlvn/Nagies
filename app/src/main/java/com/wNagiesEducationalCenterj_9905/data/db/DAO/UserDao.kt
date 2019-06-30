package com.wNagiesEducationalCenterj_9905.data.db.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wNagiesEducationalCenterj_9905.data.db.Entities.UserEntity
import io.reactivex.Flowable

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserToken(userEntity: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    fun getAuthenticatedUser(username:String,password:String):LiveData<UserEntity>

    @Query("SELECT * FROM users WHERE token = :token")
    fun getAuthenticatedUserWithToken(token:String):Flowable<List<UserEntity>>
}