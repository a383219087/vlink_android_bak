package com.yjkj.chainup.new_version.adapter.homedetail

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.home.callback.EmployeeDiffCallback
import com.yjkj.chainup.new_version.home.callback.MarketDiffCallback
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.LogUtil
import org.json.JSONObject
import java.util.ArrayList

class NewHomeMarketAdapter(private val context: Context, var data: ArrayList<JSONObject>) : RecyclerView.Adapter<NewHomeMarketAdapter.MyViewHolder>() {

    val TAG = "NewHomeMarketAdapter"
    override fun getItemViewType(position: Int): Int {
        var viewType = 0
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_homepage_rank, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val tick = data.get(position)
        var name = NCoinManager.showAnoterName(tick)
        holder.tv_ranking.text = "${position + 1}"
        if (position == 0 || position == 1 || position == 2) {
            holder.tv_ranking.setTextColor(ColorUtil.getMainColorType())
        } else {
            holder.tv_ranking.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
        }
        // TODO 待解决不包含/的情况
        if (null != name && name.contains("/")) {
            val split = name.split("/")
            holder.tv_coin_name?.setText(split[0])
            holder.tv_market_name?.setText("/" + split[1])
        }
        /**
         * 收盘价
         */
        var close = tick.optString("close")
        if (TextUtils.isEmpty(close)) {
            holder.tv_close_price.setText("--")
        } else {
            holder.tv_close_price.setText(BigDecimalUtils.showSNormal(close))
        }


        /**
         * 收盘价的汇率换算结果
         */
        var marketName = NCoinManager.getMarketName(tick.optString("name"))
        val result = RateManager.getCNYByCoinName(marketName, close)
        holder.tv_close_price_rmb?.setText(result)

        var vol = tick.optString("vol")
        if (TextUtils.isEmpty(vol)) {
            holder.tv_volume.setText(LanguageUtil.getString(context, "common_text_dayVolume") + "--")
        } else {
            holder.tv_volume.setText(LanguageUtil.getString(context, "common_text_dayVolume") + " " + BigDecimalUtils.showDepthVolume(vol))
        }

        val rose = tick.optString("rose")
        RateManager.getRoseText(holder.tv_rose, rose)
        holder.tv_rose.solid = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
//        LogUtil.e(TAG, "==NewHomeDetailFragment=payloads  ${payloads}= onBindViewHolder ${position} ")
//        if (payloads.isNotEmpty()) {
//            onBindViewHolder(holder, position)
//        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setDiffData(diffCallback: MarketDiffCallback) {
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        data = diffCallback.getNewData()

    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_coin_name: TextView? = null
        var ll_title_content: TextView? = null
        var tv_market_name: TextView? = null
        var tv_volume: TextView
        var tv_close_price: TextView
        var tv_close_price_rmb: TextView? = null
        var tv_ranking: TextView
        var tv_rose: SuperTextView

        init {
            tv_coin_name = itemView.findViewById(R.id.tv_coin_name)
            tv_market_name = itemView.findViewById(R.id.tv_market_name)
            tv_close_price = itemView.findViewById(R.id.tv_close_price)
            tv_close_price_rmb = itemView.findViewById(R.id.tv_close_price_rmb)
            tv_rose = itemView.findViewById(R.id.tv_rose)
            tv_ranking = itemView.findViewById(R.id.tv_ranking)
            tv_volume = itemView.findViewById(R.id.tv_volume)
        }
    }


}