package com.example.dentguard.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SignInRecordDao {
    @Insert
    suspend fun insertSignInRecord(record: SignInRecord)

    @Query("SELECT * FROM sign_in_records WHERE user_id = :userId ORDER BY sign_in_date DESC")
    suspend fun getUserSignInRecords(userId: Long): List<SignInRecord>

    @Query("SELECT * FROM sign_in_records WHERE user_id = :userId AND sign_in_date = :date")
    suspend fun getSignInRecordByDate(userId: Long, date: Long): SignInRecord?
}