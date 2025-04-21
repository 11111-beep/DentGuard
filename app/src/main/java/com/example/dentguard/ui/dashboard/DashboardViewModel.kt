package com.example.dentguard.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.dentguard.Room.AppDatabase
import com.example.dentguard.Room.User
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random




class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    // 初始化数据库和用户数据访问对象
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    // 获取当前用户信息，返回一个LiveData<User>对象
    val user: LiveData<User?> = userDao.getFirstUser()

    /**
     * 更新用户名。
     * @param newName 新的用户名。
     */
    fun updateUserName(newName: String) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                // 创建新的用户信息对象，复制当前用户的数据，并更新用户名
                val updatedUser = currentUser.copy(userName = newName)
                // 更新用户信息到数据库
                userDao.updateUser(updatedUser)
            }
        }
    }

    /**
     * 更新用户头像。
     * @param avatarUri 新的头像地址。
     */
    fun updateUserAvatar(avatarUri: String?) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                // 创建新的用户信息对象，复制当前用户的数据，并更新头像地址
                val updatedUser = currentUser.copy(avatarUri = avatarUri)
                // 更新用户信息到数据库
                userDao.updateUser(updatedUser)
            }
        }
    }

    /**
     * 更新早晨提醒状态。
     * @param enabled 是否启用提醒。
     */
    fun updateMorningReminder(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                // 创建新的用户信息对象，复制当前用户的数据，并更新早晨提醒状态
                val updatedUser = currentUser.copy(morningReminderEnabled = enabled)
                // 更新用户信息到数据库
                userDao.updateUser(updatedUser)
            }
        }
    }

    /**
     * 更新晚上提醒状态。
     * @param enabled 是否启用提醒。
     */
    fun updateNightReminder(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                // 创建新的用户信息对象，复制当前用户的数据，并更新晚上提醒状态
                val updatedUser = currentUser.copy(nightReminderEnabled = enabled)
                // 更新用户信息到数据库
                userDao.updateUser(updatedUser)
            }
        }
    }

    /**
     * 更新体检提醒状态。
     * @param enabled 是否启用提醒。
     */
    fun updateCheckupReminder(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                // 创建新的用户信息对象，复制当前用户的数据，并更新体检提醒状态
                val updatedUser = currentUser.copy(checkupReminderEnabled = enabled)
                // 更新用户信息到数据库
                userDao.updateUser(updatedUser)
            }
        }
    }

    /**
     * 用户签到。
     * 签到会更新用户的连续签到天数、积分、勋章和等级。
     */
    fun signIn() {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)

                // 检查是否需要重置签到状态
                if (currentMonth != currentUser.lastSignInMonth) {
                    // 重置签到状态
                    val updatedUser = currentUser.copy(
                        consecutiveDays = 1,
                        lastSignInMonth = currentMonth,
                        lastSignInDay = calendar.get(Calendar.DAY_OF_MONTH),
                        points = currentUser.points + Random.nextInt(1, 6)
                    )
                    userDao.updateUser(updatedUser)
                } else {
                    // 增加连续签到天数
                    val newConsecutiveDays = currentUser.consecutiveDays + 1

                    // 随机增加1-5积分
                    val pointsToAdd = Random.nextInt(1, 6)
                    val newPoints = currentUser.points + pointsToAdd

                    // 计算新的勋章数
                    val newAchievements = calculateNewAchievements(
                        currentUser.achievements,
                        newConsecutiveDays,
                        newPoints
                    )

                    // 计算新的等级
                    val newLevel = calculateNewLevel(
                        newConsecutiveDays,
                        newPoints,
                        newAchievements
                    )

                    // 更新用户数据
                    val updatedUser = currentUser.copy(
                        consecutiveDays = newConsecutiveDays,
                        lastSignInDay = calendar.get(Calendar.DAY_OF_MONTH),
                        points = newPoints,
                        achievements = newAchievements,
                        level = newLevel
                    )
                    userDao.updateUser(updatedUser)

                    // 打印签到成功日志
                    android.util.Log.d("DashboardViewModel", "签到成功：积分+$pointsToAdd，当前积分=$newPoints")
                }
            }
        }
    }

    /**
     * 计算新的勋章数。
     * @param currentAchievements 当前勋章数。
     * @param consecutiveDays 连续签到天数。
     * @param points 当前积分。
     * @return 新的勋章数。
     */
    private fun calculateNewAchievements(
        currentAchievements: Int,
        consecutiveDays: Int,
        points: Int
    ): Int {
        // 初始勋章数为1
        var newAchievements = 1

        // 每连续5天护牙增加1个勋章
        newAchievements += consecutiveDays / 5

        // 每10积分增加1个勋章
        newAchievements += points / 10

        // 确保勋章数不会减少
        return maxOf(currentAchievements, newAchievements)
    }

    /**
     * 计算新的等级。
     * @param consecutiveDays 连续签到天数。
     * @param points 当前积分。
     * @param achievements 当前勋章数。
     * @return 新的等级。
     */
    private fun calculateNewLevel(
        consecutiveDays: Int,
        points: Int,
        achievements: Int
    ): Int {
        // 初始等级为1
        var newLevel = 1

        // 基础等级 = 护牙天数/10 + 积分/20 + 勋章数
        val baseLevel = (consecutiveDays / 10) + (points / 20) + achievements

        // 确保等级至少为1
        newLevel = maxOf(1, baseLevel)

        return newLevel
    }
}

