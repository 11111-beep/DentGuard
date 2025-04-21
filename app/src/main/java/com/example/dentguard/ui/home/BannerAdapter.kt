package com.example.dentguard.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dentguard.databinding.ItemBannerBinding

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
    private var bannerItems: List<BannerItem> = listOf()

    inner class BannerViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BannerItem) {
            Glide.with(binding.root)
                .load(item.imageResId) // 加载本地资源 ID
                .into(binding.bannerImage)
            binding.bannerTitle.text = item.title
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BannerViewHolder(binding)
    }


    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerItems[position % bannerItems.size])
    }

    override fun getItemCount(): Int = if (bannerItems.isEmpty()) 0 else Int.MAX_VALUE

    fun submitList(items: List<BannerItem>) {
        bannerItems = items
        notifyDataSetChanged()
    }
}