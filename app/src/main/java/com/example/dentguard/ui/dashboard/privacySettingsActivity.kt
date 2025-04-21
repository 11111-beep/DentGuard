package com.example.dentguard.ui.dashboard

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.dentguard.ui.dashboard.NotificationReceiver
import com.example.dentguard.R
import com.example.dentguard.databinding.ActivityPrivacySettingsBinding
import java.util.Calendar

class privacySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacySettingsBinding
    private val CHANNEL_ID = "DentGuard_Channel"
    private val MORNING_NOTIFICATION_ID = 1
    private val NIGHT_NOTIFICATION_ID = 2
    private val CHECKUP_NOTIFICATION_ID = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 创建通知渠道
        createNotificationChannel()

        // 加载保存的时间设置
        loadSavedTimes()

        // 设置保存按钮点击事件
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "DentGuard提醒"
            val descriptionText = "口腔护理提醒通知"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadSavedTimes() {
        val sharedPrefs = getSharedPreferences("DentGuardPrefs", Context.MODE_PRIVATE)
        
        // 加载晨起提醒时间
        val morningHour = sharedPrefs.getInt("morning_hour", 7)
        val morningMinute = sharedPrefs.getInt("morning_minute", 0)
        binding.morningTimePicker.hour = morningHour
        binding.morningTimePicker.minute = morningMinute

        // 加载睡前提醒时间
        val nightHour = sharedPrefs.getInt("night_hour", 21)
        val nightMinute = sharedPrefs.getInt("night_minute", 0)
        binding.nightTimePicker.hour = nightHour
        binding.nightTimePicker.minute = nightMinute

        // 加载复查提醒日期
        val checkupYear = sharedPrefs.getInt("checkup_year", Calendar.getInstance().get(Calendar.YEAR))
        val checkupMonth = sharedPrefs.getInt("checkup_month", Calendar.getInstance().get(Calendar.MONTH))
        val checkupDay = sharedPrefs.getInt("checkup_day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        binding.checkupDatePicker.updateDate(checkupYear, checkupMonth, checkupDay)
    }

    private fun saveSettings() {
        val sharedPrefs = getSharedPreferences("DentGuardPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            // 保存晨起提醒时间
            putInt("morning_hour", binding.morningTimePicker.hour)
            putInt("morning_minute", binding.morningTimePicker.minute)

            // 保存睡前提醒时间
            putInt("night_hour", binding.nightTimePicker.hour)
            putInt("night_minute", binding.nightTimePicker.minute)

            // 保存复查提醒日期
            putInt("checkup_year", binding.checkupDatePicker.year)
            putInt("checkup_month", binding.checkupDatePicker.month)
            putInt("checkup_day", binding.checkupDatePicker.dayOfMonth)

            apply()
        }

        // 设置提醒
        scheduleNotifications()

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun scheduleNotifications() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 设置晨起提醒
        scheduleDailyNotification(
            alarmManager,
            binding.morningTimePicker.hour,
            binding.morningTimePicker.minute,
            MORNING_NOTIFICATION_ID,
            "晨起刷牙提醒",
            "该刷牙啦！保持好的口腔卫生习惯~"
        )

        // 设置睡前提醒
        scheduleDailyNotification(
            alarmManager,
            binding.nightTimePicker.hour,
            binding.nightTimePicker.minute,
            NIGHT_NOTIFICATION_ID,
            "睡前刷牙提醒",
            "睡前别忘记刷牙哦！"
        )

        // 设置复查提醒
        scheduleCheckupNotification(alarmManager)
    }

    private fun scheduleDailyNotification(
        alarmManager: AlarmManager,
        hour: Int,
        minute: Int,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("is_repeating", true)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(settingsIntent)
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(settingsIntent)
        }
    }

    private fun scheduleCheckupNotification(alarmManager: AlarmManager) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("notification_id", CHECKUP_NOTIFICATION_ID)
            putExtra("title", "定期复查提醒")
            putExtra("message", "该去复查啦！保持健康的牙齿很重要~")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            CHECKUP_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.checkupDatePicker.year)
            set(Calendar.MONTH, binding.checkupDatePicker.month)
            set(Calendar.DAY_OF_MONTH, binding.checkupDatePicker.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 9) // 设置为上午9点提醒
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}