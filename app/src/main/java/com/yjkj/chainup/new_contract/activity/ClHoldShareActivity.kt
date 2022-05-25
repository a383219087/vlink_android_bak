package com.yjkj.chainup.new_contract.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_contract.bean.ClContractPositionBean
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.BitmapUtils
import com.yjkj.chainup.util.DisplayUtil
import kotlinx.android.synthetic.main.cl_activity_hold_share.*

/**
 * 仓位分享
 */
class ClHoldShareActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_hold_share
    }

    var mPricePrecision = 0
    private var mContractPositionBean: ClContractPositionBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isLandscape = true
        super.onCreate(savedInstanceState)
        mContractPositionBean = intent.getSerializableExtra("ContractPositionBean") as ClContractPositionBean?
        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(this, mContractPositionBean?.contractId!!)
        loadData()
        val itemView = findViewById<View>(R.id.ll_share_layout)
        val layoutParams = itemView.layoutParams
        layoutParams.height = DisplayUtil.getScreenHeight() * 3 / 4 - 10
        initShareView(itemView, false)
        initShareView(ll_real_share_layout, true)
        initListener()
    }


    override fun loadData() {

    }

    private fun initListener() {
        ll_share_layout.setOnClickListener {
        }
        ll_container.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
        bt_share.textContent = getLineText("sl_str_share_confirm")
        bt_share.isEnable(true)
        bt_share.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                doShare()
            }

        }
    }

    private fun doShare() {
        val rxPermissions = RxPermissions(this@ClHoldShareActivity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        rl_share_layout.isDrawingCacheEnabled = true
                        rl_share_layout.buildDrawingCache()
                        val bitmap: Bitmap = Bitmap.createBitmap(rl_share_layout.drawingCache)
                        if (bitmap != null) {
                            ShareToolUtil.sendLocalShare(mActivity, bitmap)
                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, getString(R.string.warn_storage_permission), false)
                        }
                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, getString(R.string.warn_storage_permission), false)
                    }

                }
    }

    private fun initShareView(itemView: View, isRealShare: Boolean) {
        val tvType = itemView.findViewById<TextView>(R.id.tv_type)
        val tvContractValue = itemView.findViewById<TextView>(R.id.tv_contract_value)
        val tvLatestPrice = itemView.findViewById<TextView>(R.id.tv_latest_price)
        val tvLatestPriceValue = itemView.findViewById<TextView>(R.id.tv_latest_price_value)
        val tvOpenPriceValue = itemView.findViewById<TextView>(R.id.tv_open_price_value)
        val tvEarned = itemView.findViewById<TextView>(R.id.tv_earned)
        val ivQrCode = itemView.findViewById<ImageView>(R.id.iv_qr_code)
        val tvIntro = itemView.findViewById<TextView>(R.id.tv_intro)
        val tvContractNameLabel = itemView.findViewById<TextView>(R.id.tv_contract_name_label)
        val ivShareHeader = itemView.findViewById<ImageView>(R.id.iv_share_header)
        //文本动态设置
        itemView.findViewById<TextView>(R.id.sl_str_profit_rate_label).onLineText("cl_calculator_text8")
        itemView.findViewById<TextView>(R.id.tv_contract_name_label).onLineText("sl_str_swap")
//        itemView.findViewById<TextView>(R.id.tv_open_price_label).onLineText("sl_str_cost_price")
        itemView.findViewById<TextView>(R.id.tv_down_app).onLineText("sl_str_platform_des")
        itemView.findViewById<TextView>(R.id.tv_scan_down_app).onLineText("sl_str_scan_down_app")
        //cl_roi_str
        if (isRealShare) {
            ivShareHeader.setImageResource(R.drawable.sl_share_header_big_bg_default)
        } else {
            ivShareHeader.setImageResource(R.drawable.sl_share_header_small_bg_default)
        }
        //收益率=盈亏/保证金
        var realizedAmount = mContractPositionBean?.openRealizedAmount//盈亏
        var positionType = mContractPositionBean?.positionType //持仓类型 1 全仓, 2 逐仓
        var marginValue = if (positionType == 1) {
            mContractPositionBean?.totalMargin
        } else {
            mContractPositionBean?.isolatedMargin
        }
        var profitRate = NumberUtil.getDecimal(2).format(MathHelper.round(MathHelper.mul(mContractPositionBean?.returnRate, "100"), 2)).toString()
        val symbol = if (BigDecimalUtils.compareTo(profitRate, "0") != -1) "+" else ""
        if (BigDecimalUtils.compareTo(profitRate, "0") != -1) {

        }
        val profitRateBuff = symbol + profitRate
        tvEarned.text = profitRateBuff + "%"
        if (mContractPositionBean?.orderSide.equals("BUY")) { //多仓
            tvType.onLineText("cl_calculator_text19")
            tvType.setBackgroundResource(R.drawable.sl_border_green_fill2)
        } else if (mContractPositionBean?.orderSide.equals("SELL")) { //空仓
            tvType.onLineText("cl_calculator_text20")
            tvType.setBackgroundResource(R.drawable.sl_border_red_fill2)
        }

        if (BigDecimalUtils.compareTo(profitRate, "0") == 1) {
            tvEarned.setTextColor(mActivity.resources.getColor(R.color.main_green))
        } else {
            tvEarned.setTextColor(mActivity.resources.getColor(R.color.main_red))
        }

        tvOpenPriceValue.text = BigDecimalUtils.showSNormal(mContractPositionBean?.openAvgPrice, mPricePrecision)
        tvLatestPriceValue.text = BigDecimalUtils.showSNormal(mContractPositionBean?.indexPrice, mPricePrecision)
        tvContractValue.text = LogicContractSetting.getContractShowNameById(mActivity, mContractPositionBean?.contractId!!)

        var contractType = mContractPositionBean?.contractName?.split("-")!![0]
        tvContractNameLabel.text = when (contractType) {
            "E" -> getString(R.string.cl_market_text4)
            "S" -> getString(R.string.cl_market_text7)
            else -> getString(R.string.cl_market_text6)
        }
        tvIntro.text = if (BigDecimalUtils.compareTo(profitRateBuff, "0") >= 0 && BigDecimalUtils.compareTo(profitRateBuff, "5") < 0) {
            LanguageUtil.getString(this, "sl_str_win_intro1")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "5") >= 0 && BigDecimalUtils.compareTo(profitRateBuff, "20") < 0) {
            LanguageUtil.getString(this, "sl_str_win_intro2")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "20") >= 0 && BigDecimalUtils.compareTo(profitRateBuff, "50") < 0) {
            LanguageUtil.getString(this, "sl_str_win_intro3")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "50") >= 0 && BigDecimalUtils.compareTo(profitRateBuff, "100") < 0) {
            LanguageUtil.getString(this, "sl_str_win_intro4")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "100") >= 0) {
            LanguageUtil.getString(this, "sl_str_win_intro5")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "0") < 0 && BigDecimalUtils.compareTo(profitRateBuff, "-5") >= 0) {
            LanguageUtil.getString(this, "sl_str_lose_intro1")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "-5") < 0 && BigDecimalUtils.compareTo(profitRateBuff, "-20") >= 0) {
            LanguageUtil.getString(this, "sl_str_lose_intro2")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "-20") < 0 && BigDecimalUtils.compareTo(profitRateBuff, "-50") >= 0) {
            LanguageUtil.getString(this, "sl_str_lose_intro3")
        } else if (BigDecimalUtils.compareTo(profitRateBuff, "-50") < 0 && BigDecimalUtils.compareTo(profitRateBuff, "-100") >= 0) {
            LanguageUtil.getString(this, "sl_str_lose_intro4")
        } else {
            LanguageUtil.getString(this, "sl_str_lose_intro5")
        }

        //二维码
        var imgUrl = UserDataService.getInstance()?.inviteUrl
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = "error!"
        }
        val bmp: Bitmap? = BitmapUtils.generateBitmap(imgUrl, 480, 480)
        ivQrCode.setImageBitmap(bmp)
    }


    companion object {
        fun show(activity: Activity, mContractPositionBean: ClContractPositionBean) {
            val intent = Intent(activity, ClHoldShareActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("ContractPositionBean", mContractPositionBean)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

}