package com.yjkj.chainup.new_version.home.adapter

import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.home.viewholder.ImageHolder
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.util.BannerUtils


class ImageNetAdapter(mDatas: List<String>) : BannerAdapter<String, ImageHolder>(mDatas) {
    override fun onCreateHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val imageView: ImageView = BannerUtils.getView(parent, R.layout.banner_image) as ImageView
        return ImageHolder(imageView)
    }

    override fun onBindView(holder: ImageHolder, data: String, position: Int, size: Int) {
        //通过图片加载器实现圆角，你也可以自己使用圆角的imageview，实现圆角的方法很多，自己尝试哈
        Glide.with(holder.itemView)
                .load(data)
                .into(holder.imageView)
    }
}