package com.example.dentguard.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_name")
    var userName: String,
    @ColumnInfo(name = "avatar_uri")
    var avatarUri: String?,
    @ColumnInfo(name = "consecutive_days")
    var consecutiveDays: Int = 0,
    @ColumnInfo(name = "points")
    var points: Int = 0,
    @ColumnInfo(name = "achievements")
    var achievements: Int = 1,
    @ColumnInfo(name = "level")
    var level: Int = 1,
    @ColumnInfo(name = "last_sign_in_month")
    var lastSignInMonth: Int = -1,
    @ColumnInfo(name = "last_sign_in_day")
    var lastSignInDay: Int = -1,
    @ColumnInfo(name = "morning_reminder_enabled")
    var morningReminderEnabled: Boolean = false,
    @ColumnInfo(name = "night_reminder_enabled")
    var nightReminderEnabled: Boolean = false,
    @ColumnInfo(name = "checkup_reminder_enabled")
    var checkupReminderEnabled: Boolean = false,
    @ColumnInfo(name = "morning_reminder_time")
    var morningReminderTime: String = "07:00",
    @ColumnInfo(name = "night_reminder_time")
    var nightReminderTime: String = "21:00",
    @ColumnInfo(name = "checkup_reminder_time")
    var checkupReminderTime: Long = 0
)