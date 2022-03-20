package com.yjkj.chainup.freestaking

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import com.bumptech.glide.Glide
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.scwang.smartrefresh.layout.util.DensityUtil
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.freestaking.adapter.IncomeRecyclerAdapter
import com.yjkj.chainup.freestaking.bean.FreeStakingDetailBean
import com.yjkj.chainup.freestaking.bean.NotificationRefreshBean
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.CommonCountDownTimer
import com.yjkj.chainup.util.DisplayUtil
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pos_details.*
import kotlinx.android.synthetic.main.detail_head_layout.*
import kotlinx.android.synthetic.main.expected_return_layout.*
import kotlinx.android.synthetic.main.income_breakdown_layout.*
import kotlinx.android.synthetic.main.pos_rules_layout.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.textColor
import java.math.BigDecimal


/**
 * PoS详情页面
 */
const val PROJECT_NAME = "project_name"
const val PROJECT_INFO = "project_info"
fun BigDecimal.formatAmount(scale: Int = 2): BigDecimal {
    return this.setScale(scale, BigDecimal.ROUND_DOWN)
}

@Route(path = RoutePath.PosDetailsActivity)
class PosDetailsActivity : NewBaseActivity() {

    private var itemId: Int = 0
    private var projectType: Int = 0
    var msgUrl = ""
    var projectName: String? = ""
    var projectInfo: String? = ""
    lateinit var adapter: IncomeRecyclerAdapter
    var countDownTimer: CommonCountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos_details)
        this.itemId = intent.getIntExtra(ITEM_ID, 0)
        this.projectType = intent.getIntExtra(PROJECT_TYPE, 0)
        initView()
    }

    @SuppressLint("NewApi")
    private fun initView() {
        when (projectType) {
            1 -> {
                ll_activityProgressParent.visibility = View.GONE
                cb_agree.visibility = View.GONE
                btn_agree.visibility = View.GONE
            }
            3 -> {
                rl_activityProgressParent.visibility = View.GONE
            }

        }

        //返回按钮点击
        iv_return.setOnClickListener { finish() }
        cb_agree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && et_number.text.toString().isNotBlank()) {
                btn_agree.setBackgroundResource(R.drawable.agreebtn_click)
                btn_agree.isEnabled = true
            } else {
                btn_agree.setBackgroundResource(R.drawable.agreebtn_unclick)
                btn_agree.isEnabled = false

            }
        }

    }


    override fun onResume() {
        super.onResume()
        getProjectDetail(itemId.toString())

    }

    /**
     * 项目申购
     */
    fun requestTOBuy(amount: String, projectId: Int) {
        HttpClient.instance.requestToBuy(amount, projectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {

                        NewDialogUtils.showSuccessDialog(this@PosDetailsActivity, getString(R.string.pos_buy_success), true, object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                EventBus.getDefault().post(NotificationRefreshBean("refreshStatus"))
                                finish()
                                ArouterUtil.greenChannel(RoutePath.PosDetailsActivity, Bundle().apply {
                                    putInt(ITEM_ID, itemId)
                                    putInt(PROJECT_TYPE, projectType)
                                })
                            }
                        }, "", "", "")



                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, false)


                    }

                })
    }

    /**
     * 获取FreeStaking项目详情
     */
    private fun getProjectDetail(itemId: String) {
        showProgressDialog()
        HttpClient.instance.getProjectDetail(itemId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<FreeStakingDetailBean>() {
                    override fun onHandleSuccess(freeStakingDetailBean: FreeStakingDetailBean?) {
                        cancelProgressDialog()
                        freeStakingDetailBean?.let {
                            initData(it)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                    }

                })

    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }

    @SuppressLint("NewApi")
    private fun initData(freeStakingDetailBean: FreeStakingDetailBean?) {
        if (projectType == 3 && freeStakingDetailBean?.activeStatus == 0 && freeStakingDetailBean?.remainingTimeSeconds > 0) {
            countDownTimer = CommonCountDownTimer(freeStakingDetailBean?.remainingTimeSeconds.times(1000), 1000)
            countDownTimer?.setCountDownTimerListener(object : CommonCountDownTimer.OnCountDownTimerListener {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    freeStakingDetailBean.activeStatus = 1
                    initData(freeStakingDetailBean)

                }


            })
            countDownTimer?.start()

        }
        if (projectType == 3 && freeStakingDetailBean?.activeStatus == 1) {
            ll_lockNumber.visibility = View.VISIBLE
            expectedReturn.visibility = View.VISIBLE
            cb_agree.visibility = View.VISIBLE
            btn_agree.visibility = View.VISIBLE
            ll_cumulativeDistribution.visibility = View.GONE
            ll_youHaveObtained.visibility = View.GONE
            when (freeStakingDetailBean?.isShowBuy) {
                0 -> {
                    ll_lockNumber.visibility = View.GONE
                    cb_agree.visibility = View.GONE
                    btn_agree.visibility = View.GONE

                }

            }


        }
        //PoS记录
        tv_PoS.text = freeStakingDetailBean?.tipMine
        if (isDestroyed) {
            return
        }
        //币种logo
        Glide.with(this).load(freeStakingDetailBean?.logo).into(iv_logo)
        //BIKI
        tv_name.text = freeStakingDetailBean?.shortName
        //消息的url
        msgUrl = freeStakingDetailBean?.url.toString()
        tv_content.text = freeStakingDetailBean?.title
        projectName = freeStakingDetailBean?.name
        projectInfo = freeStakingDetailBean?.info
        tv_current.text = projectName
        tv_income.text = freeStakingDetailBean?.gainRate.toString() + "%"
        var totalAmount = freeStakingDetailBean?.totalAmount?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.toPlainString()
        var totalAmountString = SpannableStringBuilder(totalAmount)
        val totalGainAmount = freeStakingDetailBean?.totalGainAmount?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.toPlainString()
        val totalUserGainAmount = freeStakingDetailBean?.totalUserGainAmount?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.toPlainString()
        val totalGainAmountString = SpannableStringBuilder(totalGainAmount)
        val totalUserGainAmountString = SpannableStringBuilder(totalUserGainAmount)
        val foregroundColor = ForegroundColorSpan(ContextCompat.getColor(ChainUpApp.appContext, R.color.text_color))
        totalGainAmountString.setSpan(foregroundColor, 0, totalGainAmount!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalAmountString.setSpan(foregroundColor, 0, totalAmount!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalUserGainAmountString.setSpan(foregroundColor, 0, totalUserGainAmount!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        val absoluteSizeSpan = AbsoluteSizeSpan(16, true)
        totalGainAmountString.setSpan(absoluteSizeSpan, 0, totalGainAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalAmountString.setSpan(absoluteSizeSpan, 0, totalAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalAmountString.setSpan(StyleSpan(Typeface.BOLD), 0, totalAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalGainAmountString.setSpan(StyleSpan(Typeface.BOLD), 0, totalGainAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalUserGainAmountString.setSpan(absoluteSizeSpan, 0, totalUserGainAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        totalUserGainAmountString.setSpan(StyleSpan(Typeface.BOLD), 0, totalUserGainAmount.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        var shortNameString = SpannableStringBuilder(freeStakingDetailBean?.shortName)
        var gainCoin = freeStakingDetailBean?.gainCoin
        val gainCoinString = SpannableStringBuilder(gainCoin)
        val gainCoinStringColor = ForegroundColorSpan(ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color))
        gainCoinString.setSpan(gainCoinStringColor, 0, gainCoinString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        shortNameString.setSpan(gainCoinStringColor, 0, shortNameString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        val gainCoinStringSize = AbsoluteSizeSpan(12, true)
        gainCoinString.setSpan(gainCoinStringSize, 0, gainCoinString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        shortNameString.setSpan(gainCoinStringSize, 0, shortNameString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        var dailyIncome = SpannableStringBuilder(freeStakingDetailBean?.totalAmount?.multiply(freeStakingDetailBean?.lockDay.toBigDecimal())?.multiply(freeStakingDetailBean?.currencyExchangeRate)?.multiply((freeStakingDetailBean?.gainRate?.div(100)?.div(365))?.toBigDecimal())?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.stripTrailingZeros()?.toPlainString())
        dailyIncome.setSpan(foregroundColor, 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        dailyIncome.setSpan(absoluteSizeSpan, 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        dailyIncome.setSpan(StyleSpan(Typeface.BOLD), 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        if (!UserDataService.getInstance().isLogined) {
            when (projectType) {
                1 -> {
                    tv_grandTotalNumber.text = "- - - " + (freeStakingDetailBean?.gainCoin)
                    tv_grandTotalNumber.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_grandTotalNumber.textSize = 12f
                    tv_grandTotalNumber.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_alreadyNumber.text = "- - - " + (freeStakingDetailBean?.gainCoin)
                    tv_alreadyNumber.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_alreadyNumber.textSize = 12f
                    tv_alreadyNumber.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    incomeBreakdown.visibility = View.GONE

                }
                3 -> {
                    tv_lockPosition.text = "- - - " + (freeStakingDetailBean?.shortName)
                    tv_lockPosition.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_lockPosition.textSize = 12f
                    tv_lockPosition.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_cumulativeDistribution.text = "- - - " + (freeStakingDetailBean?.gainCoin)
                    tv_cumulativeDistribution.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_cumulativeDistribution.textSize = 12f
                    tv_cumulativeDistribution.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_alreadyGot.text = "- - - " + (freeStakingDetailBean?.gainCoin)
                    tv_alreadyGot.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_alreadyGot.textSize = 12f
                    tv_alreadyGot.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    availableBalance.text = getString(R.string.pos_string_available) + "- - - " + freeStakingDetailBean?.shortName
                    availableBalance.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    availableBalance.textSize = 12f
                    tv_twoDayIncomeNumber.text = "- - - " + (freeStakingDetailBean?.gainCoin)
                    tv_twoDayIncomeNumber.textColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color)
                    tv_twoDayIncomeNumber.textSize = 12f
                    tv_twoDayIncomeNumber.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    incomeBreakdown.visibility = View.GONE

                }


            }


        } else {
            when (freeStakingDetailBean?.projectType) {
                1 -> {
                    tv_grandTotalNumber.text = totalGainAmountString.append(" ").append(gainCoinString)
                    tv_alreadyNumber.text = totalUserGainAmountString.append(" ").append(gainCoinString)

                }
                3 -> {
                    tv_alreadyGot.text = totalUserGainAmountString.append(" ").append(gainCoinString)
                    tv_cumulativeDistribution.text = totalGainAmountString.append(" ").append(gainCoinString)
                    tv_lockPosition.text = totalAmountString.append(" ").append(shortNameString)
                    tv_twoDayIncomeNumber.text = dailyIncome.append(" ").append(gainCoinString)
                    et_number.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (s.toString().isBlank()) {
                                dailyIncome = SpannableStringBuilder((freeStakingDetailBean?.totalAmount.multiply(freeStakingDetailBean?.lockDay.toBigDecimal())?.multiply(freeStakingDetailBean?.currencyExchangeRate)?.multiply((freeStakingDetailBean?.gainRate?.div(100)?.div(365))?.toBigDecimal())?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.stripTrailingZeros()?.toPlainString()))

                            } else {
                                dailyIncome = SpannableStringBuilder((freeStakingDetailBean?.totalAmount.add(s.toString().toBigDecimal()))?.multiply(freeStakingDetailBean?.lockDay.toBigDecimal())?.multiply(freeStakingDetailBean?.currencyExchangeRate)?.multiply((freeStakingDetailBean?.gainRate?.div(100)?.div(365))?.toBigDecimal())?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.gainCoin.toString()))?.stripTrailingZeros()?.toPlainString())
                            }
                            dailyIncome.setSpan(foregroundColor, 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                            dailyIncome.setSpan(absoluteSizeSpan, 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                            dailyIncome.setSpan(StyleSpan(Typeface.BOLD), 0, dailyIncome.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                            tv_twoDayIncomeNumber.text = dailyIncome.append(" ").append(gainCoinString)


                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (cb_agree.isChecked && et_number.text.toString().isNotBlank()) {
                                btn_agree.setBackgroundResource(R.drawable.agreebtn_click)
                                btn_agree.isEnabled = true
                            } else {
                                btn_agree.setBackgroundResource(R.drawable.agreebtn_unclick)
                                btn_agree.isEnabled = false

                            }
                        }

                    })

                }

            }
        }
        ll_lockePosition.visibility = View.VISIBLE
        when (projectType) {
            1 -> {

            }
            3 -> {

                when (freeStakingDetailBean?.activeStatus) {
                    0 -> {
                        image_one.setImageResource(R.drawable.audit_uncomplete)
                        progress_one.progress = 0
                        image_two.setImageResource(R.drawable.audit_uncomplete)
                        progress_two.progress = 0
                        image_three.setImageResource(R.drawable.audit_uncomplete)
                        progress_three.progress = 0
                        image_four.setImageResource(R.drawable.audit_uncomplete)
                        ll_lockePosition.visibility = View.GONE
                        ll_lockNumber.visibility = View.GONE
                        expectedReturn.visibility = View.GONE
                        cb_agree.visibility = View.GONE
                        btn_agree.visibility = View.GONE
                        incomeBreakdown.visibility = View.GONE
                    }
                    1, 6 -> {
                        image_one.setImageResource(R.drawable.audit_com)
                        progress_one.progress = 50
                        image_two.setImageResource(R.drawable.audit_uncomplete)
                        progress_two.progress = 0
                        image_three.setImageResource(R.drawable.audit_uncomplete)
                        progress_three.progress = 0
                        image_four.setImageResource(R.drawable.audit_uncomplete)
                        ll_cumulativeDistribution.visibility = View.GONE
                        ll_youHaveObtained.visibility = View.GONE
                        incomeBreakdown.visibility = View.GONE
                        when (freeStakingDetailBean?.isShowBuy) {
                            0 -> {
                                ll_lockNumber.visibility = View.GONE
                                cb_agree.visibility = View.GONE
                                btn_agree.visibility = View.GONE

                            }

                        }


                    }
                    2 -> {
                        image_one.setImageResource(R.drawable.audit_com)
                        progress_one.progress = 100
                        image_two.setImageResource(R.drawable.audit_com)
                        progress_two.progress = 50
                        image_three.setImageResource(R.drawable.audit_uncomplete)
                        progress_three.progress = 0
                        image_four.setImageResource(R.drawable.audit_uncomplete)
                        ll_cumulativeDistribution.visibility = View.GONE
                        ll_youHaveObtained.visibility = View.GONE
                        ll_lockNumber.visibility = View.GONE
                        cb_agree.visibility = View.GONE
                        btn_agree.visibility = View.GONE
                        incomeBreakdown.visibility = View.GONE
                    }
                    3 -> {
                        image_one.setImageResource(R.drawable.audit_com)
                        progress_one.progress = 100
                        image_two.setImageResource(R.drawable.audit_com)
                        progress_two.progress = 100
                        image_three.setImageResource(R.drawable.audit_com)
                        progress_three.progress = 50
                        image_four.setImageResource(R.drawable.audit_uncomplete)
                        ll_lockNumber.visibility = View.GONE
                        expectedReturn.visibility = View.GONE
                        cb_agree.visibility = View.GONE
                        btn_agree.visibility = View.GONE

                    }
                    4, 5 -> {
                        image_one.setImageResource(R.drawable.audit_com)
                        progress_one.progress = 100
                        image_two.setImageResource(R.drawable.audit_com)
                        progress_two.progress = 100
                        image_three.setImageResource(R.drawable.audit_com)
                        progress_three.progress = 100
                        image_four.setImageResource(R.drawable.audit_com)
                        ll_lockNumber.visibility = View.GONE
                        cb_agree.visibility = View.GONE
                        btn_agree.visibility = View.GONE
                        expectedReturn.visibility = View.GONE


                    }

                }


            }
        }




        if (projectType == 3) {
            tv_progress.text = freeStakingDetailBean?.progress
            var progress = freeStakingDetailBean?.progress.toString().substring(0, freeStakingDetailBean?.progress.toString().length - 1)
            lock_progress.progress = progress.toInt()
            lock_count.text = (freeStakingDetailBean?.raiseAmount?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.shortName.toString()))?.toPlainString()) + freeStakingDetailBean?.shortName
            tv_numberLock.text = getString(R.string.pos_string_lockNumber) + ": "
            tv_numberLockMaxMin.text = "（" + getString(R.string.pos_string_minLimit) + ": " + ((freeStakingDetailBean?.buyAmountMin)?.stripTrailingZeros()?.toPlainString()) + (freeStakingDetailBean?.gainCoin) + " " + getString(R.string.pos_string_maxlockNumber) + ": " + (freeStakingDetailBean?.buyAmountMax)?.stripTrailingZeros()?.toPlainString() + (freeStakingDetailBean?.gainCoin) + "）"
            availableBalance.text = getString(R.string.pos_string_available) + ((freeStakingDetailBean?.balance)?.formatAmount(NCoinManager.getCoinShowPrecision(freeStakingDetailBean?.shortName.toString()))?.toPlainString()) + (freeStakingDetailBean?.shortName)
            tv_twoDayIncome.text = freeStakingDetailBean?.lockDay.toString() + getString(R.string.pos_string_twoDaysEarn)
            tv_yearMonthDayOne.text = freeStakingDetailBean.stime?.split(" ")?.get(0)
            tv_timeOne.text = freeStakingDetailBean?.stime?.split(" ")?.get(1)
            tv_yearMonthDayTwo.text = freeStakingDetailBean.etime?.split(" ")?.get(0)
            tv_timeTwo.text = freeStakingDetailBean?.etime?.split(" ")?.get(1)
            tv_yearMonthDayThree.text = freeStakingDetailBean.ltime?.split(" ")?.get(0)
            tv_timeThree.text = freeStakingDetailBean?.ltime?.split(" ")?.get(1)
            tv_yearMonthDayFour.text = freeStakingDetailBean.iasDate?.split(" ")?.get(0)
            tv_timeFour.text = freeStakingDetailBean?.iasDate?.split(" ")?.get(1)


        }

        var linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_incomeBreakdown.layoutManager = linearLayoutManager
        var lp = rv_incomeBreakdown.layoutParams
        lp.height = DensityUtil.dp2px(44.0f * 5)
        rv_incomeBreakdown.layoutParams = lp
        for (item in freeStakingDetailBean?.userGainList!!) {
            item.gainCoin = freeStakingDetailBean?.gainCoin

        }
        adapter = IncomeRecyclerAdapter(freeStakingDetailBean?.userGainList)
        rv_incomeBreakdown.adapter = adapter
//        adapter.bindToRecyclerView(rv_incomeBreakdown)
        adapter.setEmptyView(R.layout.item_new_empty)
        tv_rules.text = Html.fromHtml(freeStakingDetailBean?.details)
        //消息点击
        iv_message.setOnClickListener {
            var bundle = Bundle()
            bundle.putString(ParamConstant.web_url, msgUrl)
            bundle.putInt(ParamConstant.web_type, WebTypeEnum.Notice.value)
            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
        }
        //查看公告点击
        tv_details.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.ProjectDescriptionActivity, Bundle().apply {
                putString(PROJECT_NAME, projectName)
                putString(PROJECT_INFO, projectInfo)
            })
        }
        //全部
        tv_all.setOnClickListener {
            et_number.text = Editable.Factory.getInstance().newEditable(freeStakingDetailBean?.balance.toPlainString())
            et_number.setSelection(freeStakingDetailBean?.balance.toPlainString().length)
        }
        //全部收益明细
        tv_allDetail.setOnClickListener {
            var bundle = Bundle()
            bundle.putParcelableArrayList("userGainList", freeStakingDetailBean?.userGainList)
            ArouterUtil.greenChannel(RoutePath.IncomeDetailActivity, bundle)
        }

        //PoS记录点击
        tv_PoS.setOnClickListener {
            if (LoginManager.checkLogin(this, true)) {
                ArouterUtil.greenChannel(RoutePath.PositionRecordActivity, Bundle().apply {
                    putInt(PROJECT_TYPE, projectType)
                })
            }
        }

        //同意PoS点击事件
        btn_agree.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }
            if ((et_number.text.toString().toBigDecimal()).compareTo(freeStakingDetailBean?.buyAmountMin!!) < 0) {
                DisplayUtil.showSnackBar(window?.decorView, getString(R.string.pos_string_minquantityperLock) + freeStakingDetailBean.buyAmountMin + freeStakingDetailBean?.gainCoin, false)
                return@setOnClickListener
            }
            if (freeStakingDetailBean?.buyAmountMax!!.compareTo((et_number.text.toString().toBigDecimal().add(freeStakingDetailBean?.totalAmount))) < 0) {
                DisplayUtil.showSnackBar(window?.decorView, getString(R.string.pos_string_maxquantityLock) + freeStakingDetailBean.buyAmountMax + freeStakingDetailBean?.gainCoin, false)
                return@setOnClickListener
            }
            if ((freeStakingDetailBean?.balance)!!.compareTo(et_number.text.toString().toBigDecimal()) <= 0) {
                DisplayUtil.showSnackBar(window?.decorView, getString(R.string.pos_string_lockNotAvailable), false)
                return@setOnClickListener
            }
            requestTOBuy(et_number.text.toString(), itemId)
        }

    }


}
