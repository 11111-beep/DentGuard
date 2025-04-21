package com.example.dentguard.Room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users LIMIT 1")
    fun getFirstUser(): LiveData<User?>

    @Query("UPDATE users SET consecutive_days = :days")
    suspend fun updateConsecutiveDays(days: Int)

    @Query("UPDATE users SET points = :points")
    suspend fun updatePoints(points: Int)

    @Query("UPDATE users SET achievements = :achievements")
    suspend fun updateAchievements(achievements: Int)

    @Query("UPDATE users SET level = :level")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE users SET last_sign_in_month = :month, last_sign_in_day = :day")
    suspend fun updateLastSignInDate(month: Int, day: Int)

    @Query("UPDATE users SET morning_reminder_enabled = :enabled")
    suspend fun updateMorningReminderEnabled(enabled: Boolean)

    @Query("UPDATE users SET night_reminder_enabled = :enabled")
    suspend fun updateNightReminderEnabled(enabled: Boolean)

    @Query("UPDATE users SET checkup_reminder_enabled = :enabled")
    suspend fun updateCheckupReminderEnabled(enabled: Boolean)

    @Query("UPDATE users SET morning_reminder_time = :time")
    suspend fun updateMorningReminderTime(time: String)

    @Query("UPDATE users SET night_reminder_time = :time")
    suspend fun updateNightReminderTime(time: String)

    @Query("UPDATE users SET checkup_reminder_time = :timestamp")
    suspend fun updateCheckupReminderTime(timestamp: Long)
}