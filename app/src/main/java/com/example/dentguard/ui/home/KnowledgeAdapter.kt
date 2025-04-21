package com.example.dentguard.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dentguard.R


// 数据类，用于存储知识项的信息
data class KnowledgeItem(
    val title: String, // 知识项的标题
    val description: String, // 知识项的描述
    val imageResId: Int, // 知识项的图标资源ID
    val url: String // 知识项的网页链接
)

// 适配器类，用于将知识项数据绑定到RecyclerView中显示
class KnowledgeAdapter(
    private val items: List<KnowledgeItem>, // 需要显示的知识项列表
    private val onItemClick: (String) -> Unit // 点击事件回调
) : RecyclerView.Adapter<KnowledgeAdapter.ViewHolder>() {

    // ViewHolder类，用于持有每个知识项的视图组件
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image) // 知识项的图标
        val title: TextView = view.findViewById(R.id.title) // 知识项的标题
        val description: TextView = view.findViewById(R.id.description) // 知识项的描述
    }

    // 创建ViewHolder实例，用于展示单个知识项
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用LayoutInflater将布局文件转换为View
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_knowledge, parent, false)
        // 返回ViewHolder实例
        return ViewHolder(view)
    }

    // 绑定知识项数据到ViewHolder的视图组件
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 获取当前位置的知识项
        val item = items[position]
        // 设置图标
        holder.image.setImageResource(item.imageResId)
        // 设置标题
        holder.title.text = item.title
        // 设置描述
        holder.description.text = item.description
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(item.url)
        }
    }

    // 获取知识项列表的大小
    override fun getItemCount() = items.size
}