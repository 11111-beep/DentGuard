package com.example.dentguard.ui.dashboard

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dentguard.Refreshable
import com.example.dentguard.R
import com.example.dentguard.databinding.FragmentDashboardBinding
import java.util.Calendar

class DashboardFragment : Fragment(), Refreshable {

    // 视图绑定对象
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // 当前用户头像的URI
    private var currentAvatarUri: Uri? = null

    // DashboardViewModel 实例，用于处理用户数据

    private lateinit var dashboardViewModel: DashboardViewModel

    // 伴生对象，定义通知ID常量
    companion object {
        private const val MORNING_NOTIFICATION_ID = 1 // 早晨提醒通知ID
        private const val NIGHT_NOTIFICATION_ID = 2   // 晚上提醒通知ID
        private const val CHECKUP_NOTIFICATION_ID = 3 // 体检提醒通知ID
    }

    /**
     * 处理编辑资料活动的返回结果。
     * 使用ActivityResultContracts处理活动结果。
     */
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 处理Activity结果
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                // 更新用户名
                data.getStringExtra("new_name")?.let { newName ->
                    dashboardViewModel.updateUserName(newName)
                }
                // 更新头像
                data.getStringExtra("new_avatar")?.let { avatarUri ->
                    dashboardViewModel.updateUserAvatar(avatarUri)
                }
            }
        }
    }

    /**
     * 创建Fragment的视图。
     * 初始化ViewModel，绑定视图，设置点击监听器。
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化ViewModel
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 观察用户数据变化
        dashboardViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                // 更新用户名
                binding.userName.text = it.userName
                // 更新用户头像
                it.avatarUri?.let { uri ->
                    try {
                        binding.profileImage.setImageURI(Uri.parse(uri))
                    } catch (e: Exception) {
                        // 如果设置头像失败，使用默认头像
                        binding.profileImage.setImageResource(R.drawable.default_avatar)
                    }
                }

                // 更新用户统计数据
                binding.brushCount.text = it.consecutiveDays.toString()
                binding.points.text = it.points.toString()
                binding.achievements.text = it.achievements.toString()
                binding.userLevel.text = "Lv.${it.level} 护牙达人"

                // 更新提醒开关状态
                binding.switchMorning.isChecked = it.morningReminderEnabled
                binding.switchNight.isChecked = it.nightReminderEnabled
                binding.switchCheckup.isChecked = it.checkupReminderEnabled
            }
        }

        // 设置点击监听器
        setupClickListeners()
        return root
    }

    /**
     * 设置视图的点击监听器。
     * 包括编辑资料、隐私设置、关于我们、捐赠、帮助与反馈按钮的点击事件，
     * 以及提醒开关的变化监听。
     */
    private fun setupClickListeners() {
        // 编辑资料按钮点击事件
        binding.editProfile.setOnClickListener {
            // 启动编辑资料活动
            val intent = Intent(requireContext(), editProfileActivity::class.java).apply {
                putExtra("current_name", binding.userName.text.toString())
                putExtra("current_avatar", currentAvatarUri?.toString())
            }
            editProfileLauncher.launch(intent)
        }

        // 隐私设置按钮点击事件
        binding.privacySettings.setOnClickListener {
            val intent = Intent(requireContext(), privacySettingsActivity::class.java)
            startActivity(intent)
        }

        // 关于我们按钮点击事件
        binding.aboutUs.setOnClickListener {
            showToast("关于我们")
        }

        // 捐赠按钮点击事件
        binding.donate.setOnClickListener {
            showToast("爱心捐赠")
        }

        // 帮助与反馈按钮点击事件
        binding.helpFeedback.setOnClickListener {
            showToast("帮助与反馈")
        }

        // 早晨提醒开关变化监听
        binding.switchMorning.setOnCheckedChangeListener { _, isChecked ->
            dashboardViewModel.updateMorningReminder(isChecked)
            if (isChecked) {
                // 设置新的通知
                setupMorningNotification()
            } else {
                // 取消通知
                cancelNotification(MORNING_NOTIFICATION_ID)
            }
            // showToast(if (isChecked) "已开启晨起刷牙提醒" else "已关闭晨起刷牙提醒")
        }

        // 晚上提醒开关变化监听
        binding.switchNight.setOnCheckedChangeListener { _, isChecked ->
            dashboardViewModel.updateNightReminder(isChecked)
            if (isChecked) {
                // 设置新的通知
                setupNightNotification()
            } else {
                // 取消通知
                cancelNotification(NIGHT_NOTIFICATION_ID)
            }
            // showToast(if (isChecked) "已开启睡前刷牙提醒" else "已关闭睡前刷牙提醒")
        }

        // 体检提醒开关变化监听
        binding.switchCheckup.setOnCheckedChangeListener { _, isChecked ->
            dashboardViewModel.updateCheckupReminder(isChecked)
            if (isChecked) {
                // 设置新的通知
                setupCheckupNotification()
            } else {
                // 取消通知
                cancelNotification(CHECKUP_NOTIFICATION_ID)
            }
            // showToast(if (isChecked) "已开启定期复查提醒" else "已关闭定期复查提醒")
        }
    }

    /**
     * 设置早晨刷牙提醒通知。
     * 配置在早上8点显示提醒，重复每天。
     */
    private fun setupMorningNotification() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8) // 默认早上8点
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果当前时间已经过了设定的时间，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("notification_id", MORNING_NOTIFICATION_ID)
            putExtra("title", "早上刷牙提醒")
            putExtra("message", "早上好！该刷牙啦！")
            putExtra("is_repeating", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            MORNING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    showToast("请授予精确闹钟权限以使用提醒功能")
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            showToast("无法设置精确闹钟，请检查权限设置")
            e.printStackTrace()
        }
    }

    /**
     * 设置晚上刷牙提醒通知。
     * 配置在晚上10点显示提醒，重复每天。
     */
    private fun setupNightNotification() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22) // 默认晚上10点
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果当前时间已经过了设定的时间，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("notification_id", NIGHT_NOTIFICATION_ID)
            putExtra("title", "晚上刷牙提醒")
            putExtra("message", "晚上好！该刷牙啦！")
            putExtra("is_repeating", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            NIGHT_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    showToast("请授予精确闹钟权限以使用提醒功能")
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            showToast("无法设置精确闹钟，请检查权限设置")
            e.printStackTrace()
        }
    }

    /**
     * 设置定期检查提醒通知。
     * 配置在上午10点显示提醒，重复每天。
     */
    private fun setupCheckupNotification() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10) // 默认上午10点
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果当前时间已经过了设定的时间，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("notification_id", CHECKUP_NOTIFICATION_ID)
            putExtra("title", "定期检查提醒")
            putExtra("message", "该去检查牙齿啦！")
            putExtra("is_repeating", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            CHECKUP_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    showToast("请授予精确闹钟权限以使用提醒功能")
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            showToast("无法设置精确闹钟，请检查权限设置")
            e.printStackTrace()
        }
    }

    /**
     * 取消指定ID的通知。
     * @param notificationId 通知ID
     */
    private fun cancelNotification(notificationId: Int) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 显示短时提示信息。
     * @param message 提示内容
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Fragment视图销毁时清理资源。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 刷新用户数据。
     * 实现Refreshable接口的方法，用于刷新数据。
     */
    override fun refreshData() {
        // 观察数据变化
        dashboardViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.userName.text = it.userName
                it.avatarUri?.let { uri ->
                    try {
                        binding.profileImage.setImageURI(Uri.parse(uri))
                    } catch (e: Exception) {
                        binding.profileImage.setImageResource(R.drawable.default_avatar)
                    }
                }

                // 更新用户数据
                binding.brushCount.text = it.consecutiveDays.toString()
                binding.points.text = it.points.toString()
                binding.achievements.text = it.achievements.toString()
                binding.userLevel.text = "Lv.${it.level} 护牙达人"

                // 更新提醒开关状态
                binding.switchMorning.isChecked = it.morningReminderEnabled
                binding.switchNight.isChecked = it.nightReminderEnabled
                binding.switchCheckup.isChecked = it.checkupReminderEnabled
            }
        }
    }
}
