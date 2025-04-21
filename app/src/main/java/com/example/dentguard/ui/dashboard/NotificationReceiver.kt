package com.example.dentguard.ui.dashboard

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dentguard.R
import com.example.dentguard.Room.AppDatabase
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "DentGuard_Channel"
        private const val CHANNEL_NAME = "刷牙提醒"
        private const val CHANNEL_DESCRIPTION = "提醒您按时刷牙"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // 检查开关状态
        val notificationId = intent.getIntExtra("notification_id", 0)
        val user = runBlocking {
            AppDatabase.getDatabase(context).userDao().getUserById(1)
        }
        if (user == null) return

        // 根据通知ID检查对应的开关状态
        val isEnabled = when (notificationId) {
            1 -> user.morningReminderEnabled
            2 -> user.nightReminderEnabled
            3 -> user.checkupReminderEnabled
            else -> false
        }

        // 如果开关关闭，不显示通知
        if (!isEnabled) return

        // 创建通知渠道
        createNotificationChannel(context)

        val title = intent.getStringExtra("title") ?: "提醒"
        val message = intent.getStringExtra("message") ?: "该刷牙啦！"
        val isRepeating = intent.getBooleanExtra("is_repeating", false)

        // 显示通知
        showNotification(context, notificationId, title, message)

        // 如果是重复通知，设置下一次通知
        if (isRepeating) {
            scheduleNextNotification(context, intent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        // 获取默认通知声音
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(null, true) // 在锁屏时显示通知

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 检查通知权限
        if (!notificationManager.areNotificationsEnabled()) {
            // 通知权限被禁用，可以在这里添加跳转到设置页面的逻辑
            return
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun scheduleNextNotification(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        
        if (hour == -1 || minute == -1) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // 设置为明天同一时间
        }

        val nextIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", intent.getIntExtra("notification_id", 0))
            putExtra("title", intent.getStringExtra("title"))
            putExtra("message", intent.getStringExtra("message"))
            putExtra("is_repeating", true)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intent.getIntExtra("notification_id", 0),
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // 如果没有精确闹钟权限，跳转到设置页面
                    val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // 处理权限被拒绝的情况
            val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
        }
    }
}