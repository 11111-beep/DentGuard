package com.example.dentguard.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sign_in_records")
data class SignInRecord(
   @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "sign_in_date")
    val signInDate: Long,

    @ColumnInfo(name = "continues_days")
    val continuesDays: Int
)