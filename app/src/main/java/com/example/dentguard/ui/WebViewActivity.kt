package com.example.dentguard.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.example.dentguard.BaseActivity
import com.example.dentguard.databinding.ActivityWebviewBinding

class WebViewActivity : BaseActivity() {
    // 视图绑定对象
    private lateinit var binding: ActivityWebviewBinding

    // 创建 Activity 时调用
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 绑定视图
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置 Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 显示返回按钮
        supportActionBar?.setDisplayShowTitleEnabled(false) // 隐藏标题

        // 获取从 Intent 传递过来的 URL，如果没有 URL 则返回
        val url = intent.getStringExtra("url") ?: return

        // 配置 WebView
        binding.webView.apply {
            // 配置 WebView 设置
            settings.apply {
                // 启用 JavaScript 支持
                javaScriptEnabled = true
                // 启用 DOM 存储
                domStorageEnabled = true
                // 启用 数据库支持
                databaseEnabled = true

                // 缓存策略：优先使用缓存，否则从网络加载
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // 网页自适应屏幕
                loadWithOverviewMode = true
                // 使用宽视口模式
                useWideViewPort = true

                // 禁用缩放以提升性能
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false

                // 设置默认字体大小（100 表示正常大小）
                textZoom = 100

                // 混合内容模式：兼容旧版本的混合内容处理方式
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                // 设置用户代理字符串，模拟移动端浏览器
                userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            }

            // 设置 WebViewClient 处理网页加载事件
            webViewClient = object : WebViewClient() {
                // 网页开始加载时调用
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // 显示进度条
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = 0

                    // 开始加载时阻止图片加载，优先加载文字内容
                    settings.blockNetworkImage = true
                }

                // 网页加载完成时调用
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 页面加载完成后再加载图片
                    settings.blockNetworkImage = false
                    // 隐藏进度条
                    binding.progressBar.visibility = View.GONE
                }

                // 拦截和重写资源请求，用于优化资源加载
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    // 获取请求 URL
                    val url = request?.url?.toString() ?: return null
                    // 过滤广告和不必要的资源（例如包含 "ad" 或 "analytics" 的 URL）
                    if (url.contains("ad") || url.contains("analytics")) {
                        // 返回空响应，阻止加载
                        return WebResourceResponse(null, null, null)
                    }
                    // 其他资源正常加载
                    return super.shouldInterceptRequest(view, request)
                }
            }

            // 设置 WebChromeClient 处理网页进度变化
            webChromeClient = object : WebChromeClient() {
                // 网页加载进度发生变化时调用
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // 更新进度条进度
                    binding.progressBar.progress = newProgress
                }
            }

            // 加载指定的 URL
            loadUrl(url)
        }
    }

    // 处理选项菜单项点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 如果点击的是返回按钮
        if (item.itemId == android.R.id.home) {
            // 返回上一页面
            onBackPressed()
            return true
        }
        // 其他情况交给父类处理
        return super.onOptionsItemSelected(item)
    }

    // 处理返回键按下事件
    override fun onBackPressed() {
        // 如果 WebView 可以返回上一页
        if (binding.webView.canGoBack()) {
            // 返回上一页
            binding.webView.goBack()
        } else {
            // 否则退出 Activity
            super.onBackPressed()
        }
    }

    // Activity 销毁时调用，清理缓存和资源
    override fun onDestroy() {
        binding.webView.apply {
            // 清除缓存
            clearCache(true)
            // 清除历史记录
            clearHistory()
        }
        // 调用父类的 onDestroy 方法
        super.onDestroy()
        // 销毁 WebView，释放资源
        binding.webView.destroy()
    }
}
