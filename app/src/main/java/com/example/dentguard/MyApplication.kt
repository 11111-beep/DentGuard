package com.example.dentguard

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.example.dentguard.Room.AppDatabase
import com.example.dentguard.Room.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initializeDatabase()
    }

    private fun initializeDatabase() {
        applicationScope.launch {
            val database = AppDatabase.getDatabase(this@MyApplication)
            val userDao = database.userDao()
            
            // 检查是否已有用户数据
            if (userDao.getFirstUser().value == null) {
                // 创建默认用户
                val defaultUser = User(
                    userName = "小何",
                    avatarUri = null,
                    consecutiveDays = 0,
                    morningReminderEnabled = false,
                    nightReminderEnabled = false,
                    checkupReminderEnabled = false
                )
                userDao.insertUser(defaultUser)
            }
        }
    }
}