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
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.util.ClipboardUtil
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.wedegit.WrapContentViewPager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_coin_map_intro.*
import kotlinx.android.synthetic.main.item_payment_information.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import com.yjkj.chainup.databinding.FragmentCoinMapIntroBinding
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.ColorUtil


/**
 * @description :  币种简介
 * @date 2019-4-15
 * @author Bertking
 *
 */
class CoinMapIntroFragment : Fragment() {
    val TAG = CoinMapIntroFragment::class.java.simpleName

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
        val binding: FragmentCoinMapIntroBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_coin_map_intro,container,false);

        val view = binding.root
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, index)
        }
        if (PublicInfoDataService.getInstance().klineThemeMode == 1){
            binding.isblack = true
        }

        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tv_publish_time?.text = LanguageUtil.getString(context, "market_text_publishtime")
        tv_publish_total_amount?.text = LanguageUtil.getString(context, "market_text_publishTotal")

        tv_currentTotal?.text = LanguageUtil.getString(context, "market_text_currentTotal")
        tv_web?.text = LanguageUtil.getString(context, "market_text_coinHomepage")
        tv_block?.text = LanguageUtil.getString(context, "market_text_blockSearch")

        tv_introduction?.text = LanguageUtil.getString(context, "market_text_coinInfo")

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
        var coinName = "BTC"
        val symbolObj = NCoinManager.getSymbolObj(symbol)
        if (null != symbolObj || symbolObj!!.length() >= 0) {
            var name = symbolObj.optString("name")
            if (name.contains("/")) {
                coinName = name.split("/")[0]
            }
        }
        Log.d(TAG, "=======获取币种简介2===$coinName====")
        getCoinIntro(coinName)
    }

    companion object {
        fun newInstance(viewPager: WrapContentViewPager, symbol: String?, index: Int = 0) =
                CoinMapIntroFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putString(ParamConstant.symbol, symbol)
                        putInt(ParamConstant.CUR_INDEX, index)
                    }
                }
    }


    private fun initView(jsonObject: JSONObject) {
        jsonObject.run {
            val symbolName = this.optString("symbolName")
            if (symbolName.isNotEmpty()) {
                tv_coin_name?.text = this.optString("coinSymbol")
            } else {
                tv_coin_name?.text = NCoinManager.setsymbolNameGetShowName(this.optString("coinSymbol"))
            }
            tv_issue_date?.text = this.optString("publishTimeStr")
            tv_auth_amount?.text = this.optString("publishAmount")
            tv_fluent_amount?.text = this.optString("currencyAmount")
            tv_official_web?.text = this.optString("officialUrl")
            tv_block_explorer?.text = this.optString("blockchainUrl")
            tv_intro?.text = this.optString("introduction")
            tv_block_explorer?.onClick {
                ClipboardUtil.copy(tv_block_explorer)
                DisplayUtil.showSnackBar(activity?.window?.decorView, LanguageUtil.getString(context, "common_tip_copySuccess"), true)
            }
            tv_official_web?.onClick {
                ClipboardUtil.copy(tv_official_web)
                DisplayUtil.showSnackBar(activity?.window?.decorView, LanguageUtil.getString(context, "common_tip_copySuccess"), true)
            }
        }
    }

    /**
     * 获取币种简介
     */
    private fun getCoinIntro(coinName: String) {
        CompositeDisposable().add(MainModel().getCoinIntro(coin = coinName, consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(data: JSONObject) {
                LogUtil.d(TAG, "getCoinIntro==data is " + data)
                initView(data.optJSONObject("data"))
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                LogUtil.d(TAG, "getCoinIntro==code is " + code + ",msg is " + msg)
            }


        })!!)
    }


}
