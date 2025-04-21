package com.example.dentguard.ui.home

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.dentguard.BaseActivity
import com.example.dentguard.R

class correctBrushingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct_brushing)

        // 手动设置 Toolbar 为 ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 显示返回按钮
        supportActionBar?.setDisplayShowTitleEnabled(false) // 隐藏标题

    }

    // 处理选项菜单项点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 如果点击的是返回按钮
        if (item.itemId == android.R.id.home) {
            // 返回上一页
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // 处理返回键按下事件
    override fun onBackPressed() {
        // 直接返回上一页
        super.onBackPressed()
    }
}