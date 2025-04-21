package com.example.dentguard

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.dentguard.databinding.ActivityMainBinding
import com.example.dentguard.ui.dashboard.DashboardFragment
import com.example.dentguard.ui.home.HomeFragment
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置底部导航
        setupNavigation()
        
        // 设置下拉刷新
        setupSwipeRefresh()
        
        // 请求权限
        requestPermissions()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            )
        )

        navView.setupWithNavController(navController)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.mint_200)
        binding.swipeRefreshLayout.setOnRefreshListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)

            if (currentFragment is HomeFragment) {
                currentFragment.refreshData()
            } else if (currentFragment is DashboardFragment) {
                currentFragment.refreshData()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1200)
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS ,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        )

        // 对于 Android 13 (API 33) 及以上版本，需要请求具体的图片和视频权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "为了应用能够正常工作，请授予以下权限",
                    "确定",
                    "取消"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您需要在设置中手动授予以下权限",
                    "前往设置",
                    "取消"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "以下权限被拒绝：$deniedList", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
