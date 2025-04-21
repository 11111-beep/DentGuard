package com.example.dentguard.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dentguard.R
import com.example.dentguard.Refreshable
import com.example.dentguard.databinding.FragmentHomeBinding
import com.example.dentguard.ui.WebViewActivity
import com.example.dentguard.ui.dashboard.DashboardViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(), Refreshable {

    // UI绑定的变量，使用ViewBinding简化视图操作
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // 不可变性获取绑定对象

    // Banner适配器，用于展示广告轮播
    private lateinit var bannerAdapter: BannerAdapter
    // 用于处理Banner自动轮播的协程任务
    private var bannerJob: Job? = null



    // 日期处理相关工具
    private val chipDateFormat = SimpleDateFormat("d", Locale.getDefault())  // 日期格式化工具，用于显示日期的天


    // 记录RecyclerView的状态，用于在配置更改后恢复列表状态
    private var recyclerViewState: Parcelable? = null

    private lateinit var dashboardViewModel: DashboardViewModel

    /**
     * 创建并返回Fragment的视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 在视图创建完成后初始化组件
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化Banner
        setupBanner()
        // 初始化签到相关的数据和UI组件
        setupSignInDays()
        // 初始化知识列表的数据和布局
        setupKnowledgeList()
        // 设置各个UI组件的点击事件监听器
        setupClickListeners()

        // 恢复RecyclerView的状态，如果有保存的实例状态
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable("recycler_view_state")
        }
    }

    /**
     * 在视图状态恢复时应用RecyclerView的状态
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // 从保存的实例状态中恢复RecyclerView的状态
        savedInstanceState?.let {
            recyclerViewState = it.getParcelable("recycler_view_state")
            binding.knowledgeList.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
    }

    /**
     * 实现刷新接口，重新加载数据并重置UI
     */
    override fun refreshData() {
        // 检查binding是否为空
        if (_binding == null) return
        
        // 显示刷新提示
        Toast.makeText(context, "正在刷新...", Toast.LENGTH_SHORT).show()

        // 在协程作用域中执行刷新操作
        lifecycleScope.launch {
            // 模拟数据刷新延时
            delay(1200)

            // 检查binding是否为空
            if (_binding == null) return@launch

            // 更新Banner内容
            setupBanner()

            // 重新构建签到相关的UI组件
            setupSignInDays()

            // 更新知识列表的内容
            setupKnowledgeList()

            // 显示刷新完成提示
            Toast.makeText(context, "刷新完成", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 初始化签到相关的数据和UI组件
     */
    private fun setupSignInDays() {
        // 观察用户数据变化
        dashboardViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                // 更新连续签到天数显示
                binding.signInDays.text = "已连续签到${it.consecutiveDays}天"
                
                // 更新签到按钮状态
                val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                binding.btnSignIn.isEnabled = it.lastSignInDay != today
                
                // 更新签到日期显示
                updateSignInDates(it.consecutiveDays)
            }
        }
    }

    private fun updateSignInDates(consecutiveDays: Int) {
        val today = Calendar.getInstance()
        val chipGroup = binding.daysChipGroup
        
        // 清除现有的日期标签
        chipGroup.removeAllViews()
        
        // 添加最近7天的日期标签
        for (i in 6 downTo 0) {
            val date = today.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            
            val chip = Chip(requireContext()).apply {
                text = chipDateFormat.format(date.time)
                isClickable = false
                setChipBackgroundColorResource(R.color.chip_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text))
                
                // 如果这一天已经签到，设置不同的样式
                if (i <= consecutiveDays - 1) {
                    setChipBackgroundColorResource(R.color.gradient_end)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_signed_text))
                }
            }
            
            chipGroup.addView(chip)
        }
    }

    /**
     * 初始化知识列表的数据和布局
     */
    private fun setupKnowledgeList() {
        // 创建知识项列表
        val knowledgeItems = listOf(
            KnowledgeItem(
                "X光片",
                "X射线可以帮助牙科医生在牙齿之间或者在牙齿边缘看到牙齿问题。",
                R.drawable.ic_xgp,
                "https://cn.dentalhealth.org/x-rays"
            ),
            KnowledgeItem(
                "上下颌问题和头痛",
                "如果你的下巴和牙齿排列不正确,它不仅会影响你的咬合,还可能导致严重的头痛和下巴疼痛。",
                R.drawable.ic_sxe,
                "https://cn.dentalhealth.org/jaw-problems-and-headaches"
            ),
            KnowledgeItem(
                "义齿",
                "人们戴假牙,塑料或金属,以代替丢失或缺失的牙齿,以便他们享受健康的饮食和微笑并充满信心。",
                R.drawable.ic_yc,
                "https://cn.dentalhealth.org/dentures"
            ),
            KnowledgeItem(
                "义齿性口腔炎",
                "鹅口疮可能出现在身体的其他部位，但是当它影响到嘴巴时，它可能被称为假牙口腔炎并且是由酵母引起的。",
                R.drawable.ic_ycx,
                "https://cn.dentalhealth.org/denture-stomatitis"
            ),
            KnowledgeItem(
                "义齿清洁",
                "治疗你的假牙是很重要的，就像你会对待你的天然牙齿一样，一般的规则是刷牙，再次浸泡和刷牙。",
                R.drawable.ic_ycq,
                "https://cn.dentalhealth.org/denture-cleaning"
            ),
            KnowledgeItem(
                "健康的牙龈,健康的身体",
                "由于不良的牙齿健康可能导致或恶化的一些问题包括心脏病、中风和糖尿病。",
                R.drawable.ic_jkd,
                "https://cn.dentalhealth.org/healthy-gums-and-healthy-body"
            )

        )

        // 设置RecyclerView的布局管理器
        binding.knowledgeList.layoutManager = GridLayoutManager(context, 2)

        // 创建适配器并设置到RecyclerView
        val adapter = KnowledgeAdapter(knowledgeItems) { url ->
            // 使用WebViewActivity打开链接
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", url)
            }
            startActivity(intent)
        }
        binding.knowledgeList.adapter = adapter
    }

    /**
     * 设置各个UI组件的点击事件监听器
     */
    private fun setupClickListeners() {
        // 搜索按钮点击监听器
        binding.searchBarContainer.findViewById<Button>(R.id.search_button).setOnClickListener {
            val searchText = binding.searchBarContainer.findViewById<EditText>(R.id.search_edit_text).text.toString()
            if (searchText.isNotEmpty()) {
                // 构建搜索URL
                val searchUrl = "https://cn.bing.com/search?q=${Uri.encode(searchText)}"
                // 使用WebViewActivity打开搜索结果
                val intent = Intent(context, WebViewActivity::class.java).apply {
                    putExtra("url", searchUrl)
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "请输入搜索内容", Toast.LENGTH_SHORT).show()
            }
        }

        // 签到按钮点击监听器
        binding.btnSignIn.setOnClickListener {
            if (binding.btnSignIn.isEnabled) {
                // 播放签到动画
                playSignInAnimation()
                // 更新签到数据
                dashboardViewModel.signIn()
                // 刷新显示
                lifecycleScope.launch {
                    delay(500) // 等待数据更新
                    refreshData()
                }
            }
        }

        // 正确刷牙按钮点击监听器
        binding.btnBrushTeeth.setOnClickListener {
            // 显示刷牙指导提示
           Intent(context, correctBrushingActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }

        // 定期检查按钮点击监听器
        binding.btnCheckTeeth.setOnClickListener {
            // 跳转到定期检查活动
            Intent(context, regularCheckActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }

        binding.btnYaxian.setOnClickListener {
            // 跳转到使用牙刷活动
            Intent(context, useDentalFlossActivity::class.java).also { intent ->
                startActivity(intent)
            }
        }
        // 更多知识按钮点击监听器
        binding.btnMoreKnowledge.setOnClickListener {
            // 显示查看更多知识的提示
            sendRequestWithOkHttp()
        }


    }

    /**
     * 播放签到动画的方法
     */
    private fun playSignInAnimation() {
        val scaleX = ObjectAnimator.ofFloat(binding.btnSignIn, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.btnSignIn, "scaleY", 1f, 1.2f, 1f)
        val alpha = ObjectAnimator.ofFloat(binding.btnSignIn, "alpha", 1f, 0.5f, 1f)
        
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 500
            interpolator = OvershootInterpolator()
        }
        
        animatorSet.start()
    }

    /**
     * 设置Banner及其自动轮播
     */
    private fun setupBanner() {
        bannerAdapter = BannerAdapter()

        binding.bannerViewPager.apply {
            // 设置Banner适配器
            adapter = bannerAdapter

            // 设置页面间距
            val pageMargin = resources.getDimensionPixelOffset(R.dimen.page_margin)
            val pageOffset = resources.getDimensionPixelOffset(R.dimen.page_offset)

            // 修改页面变换效果
            setPageTransformer { page, position ->
                val myOffset = position * -(2 * pageOffset + pageMargin)
                when {
                    position < -1 -> {
                        page.translationX = -myOffset
                    }
                    position <= 1 -> {
                        val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
                        page.translationX = myOffset
                        page.scaleY = scaleFactor
                        page.alpha = scaleFactor
                    }
                    else -> {
                        page.alpha = 0f
                        page.translationX = myOffset
                    }
                }
            }

            // 设置ViewPager2的属性
            clipToPadding = false
            clipChildren = false

            // 设置左右padding，使中间item完整显示
            val padding = resources.getDimensionPixelOffset(R.dimen.page_margin)
            setPadding(padding, 0, padding, 0)

            // 设置每个item之间的间距
            (getChildAt(0) as? RecyclerView)?.apply {
                setPadding(pageOffset, 0, pageOffset, 0)
                clipToPadding = false
            }
        }

        // 定义Banner项
        val bannerItems = listOf(
            BannerItem(R.drawable.banner_1, "爱护牙齿人人有责"),
            BannerItem(R.drawable.banner_2, "牙科建模扫描根管治疗做牙冠的手术机器人"),
            BannerItem(R.drawable.banner_3, "亲子互动与健康习惯"),
            BannerItem(R.drawable.banner_4, "牙科治疗")
        )

        // 提交Banner数据
        bannerAdapter.submitList(bannerItems)

        // 启动自动轮播
        startAutoBanner()
    }

    /**
     * 启动自动轮播
     */
    private fun startAutoBanner() {
        // 取消之前的轮播任务
        bannerJob?.cancel()
        // 启动新的轮播任务
        bannerJob = lifecycleScope.launch {
            while (isActive) {
                delay(6000)  // 每3秒切换一次
                binding.bannerViewPager.setCurrentItem(
                    (binding.bannerViewPager.currentItem + 1) % bannerAdapter.itemCount,
                    true
                )
            }
        }
    }

    /**
     * 清理资源和取消轮播任务
     */
    override fun onDestroyView() {
        // 取消自动轮播任务
        bannerJob?.cancel()
        // 清理绑定对象
        _binding = null
        super.onDestroyView()
    }

    private fun sendRequestWithOkHttp() {
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("url", "https://cn.dentalhealth.org/pages/category/all-oral-health-information")
        }
        startActivity(intent)
    }
}
