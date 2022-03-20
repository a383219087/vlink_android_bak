package com.yjkj.chainup.new_version.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.databinding.FragmentCoinMapEtfBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.wedegit.WrapContentViewPager
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import com.yjkj.chainup.databinding.FragmentCoinMapIntroBinding
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_coin_map_etf.*


/**
 * @description :  币种简介
 * @date 2019-4-15
 * @author Bertking
 *
 */
class CoinMapEtfFragment : Fragment() {
    val TAG = CoinMapEtfFragment::class.java.simpleName

    private var viewPager: WrapContentViewPager? = null

    private var symbol = "btcusdt"
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            symbol = it.getString(ParamConstant.symbol) ?: "btcusdt"
            index = it.getInt(ParamConstant.CUR_INDEX)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var binding: FragmentCoinMapEtfBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_coin_map_etf, container, false);

        var view = binding.root
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, index)
        }
        if (PublicInfoDataService.getInstance().klineThemeMode == 1) {
            binding.isblack = true
        }

        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tv_publish_time?.text = LanguageUtil.getString(context, "etf_info_lever")
        tv_publish_total_amount?.text = LanguageUtil.getString(context, "etf_text_networth")

        tv_currentTotal?.text = LanguageUtil.getString(context, "etf_info_rules")
        tv_web?.text = LanguageUtil.getString(context, "etf_info_rules_no")
        tv_block?.text = LanguageUtil.getString(context, "sl_str_funds_rate")

        NLiveDataUtil.observeData(this, Observer {
            if (null != it && MessageEvent.symbol_switch_type == it.msg_type) {
                if (null != it.msg_content) {
                    symbol = it.msg_content as String
                    fetchCoinIntro()
                }
            }
        })

        fetchCoinIntro()
    }

    private fun fetchCoinIntro() {
        val jsonObject = NCoinManager.getSymbolObj(symbol)
        tv_coin_name?.text = jsonObject.etfItemKlineShowTitle(context!!)
        tv_coin_etf_name?.text = jsonObject.etfItemKlineInfo(activity!!)
        tv_block_explorer?.text = symbol.getFundRate()
    }

    companion object {
        fun newInstance(viewPager: WrapContentViewPager, symbol: String?, index: Int = 0) =
                CoinMapEtfFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putString(ParamConstant.symbol, symbol)
                        putInt(ParamConstant.CUR_INDEX, index)
                    }
                }
    }


    private fun initView(jsonObject: JSONObject) {
        jsonObject.run {
            // 净值
            tv_auth_amount?.text = optString("price") ?: "--"
            val topNum = optString("maxLeverValue","--")
            tv_issue_date?.text = "%s/%s".format(topNum, optString("realLeverValue", "--"))
            tv_official_web?.text = LanguageUtil.getString(context, "etf_notes_manual_lever_time").format(topNum)
        }
    }

    fun changeETF(jsonObject: JSONObject?) {
        // 处理最大 当前净值 最大
        jsonObject?.apply {
            initView(this)
        }
    }


}
