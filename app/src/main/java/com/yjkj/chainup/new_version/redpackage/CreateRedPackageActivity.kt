package com.yjkj.chainup.new_version.redpackage

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.KeyBoardUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.DecimalDigitsInputFilter
import com.yjkj.chainup.new_version.redpackage.bean.CreatePackageBean
import com.yjkj.chainup.new_version.redpackage.bean.PayBean
import com.yjkj.chainup.new_version.redpackage.bean.RedPackageInitInfo
import com.yjkj.chainup.new_version.redpackage.bean.TempBean
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_red_package.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019/6/29-16:26 AM
 * @Description: 发红包界面
 *
 * Dialog:dialog_create_red_package
 */

@Route(path = RoutePath.CreateRedPackageActivity)
class CreateRedPackageActivity : NewBaseActivity() {

    var redPackageType = LUCKY_RED_PACKAGE

    var redPackageDialog: TDialog? = null

    var subTitleList = ArrayList<String>()

    var selectedSymbol: RedPackageInitInfo.Symbol? = null

    var redPackageInitInfo: RedPackageInitInfo? = null

    var createPackageBean: CreatePackageBean? = null

    var googleDialog: TDialog? = null

    var precision = 4

    //1.只针对新用户 0.不做限制
    var isNew = 0


    companion object {
        /**
         * 普通红包
         */
        const val NORMAL_RED_PACKAGE = 0
        /**
         * 拼手气红包
         */
        const val LUCKY_RED_PACKAGE = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_red_package)

        rb_lucky?.text = LanguageUtil.getString(this, "redpacket_send_random")
        rb_normal?.text = LanguageUtil.getString(this, "redpacket_send_identical")
        tv_redpacket_send_currency?.text = LanguageUtil.getString(this, "redpacket_send_currency")
        tv_money_title?.text = LanguageUtil.getString(this, "redpacket_send_each")
        cb_new_user?.text = LanguageUtil.getString(this, "redpacket_send_new")
        tv_overtime?.text = LanguageUtil.getString(this, "redpacket_send_prompt")
        tv_select_coin?.hint = LanguageUtil.getString(this, "redpacket_send_inputCoin")
        et_mount?.hint = LanguageUtil.getString(this, "redpacket_send_enterNumber")
        tv_redpacket_send_num?.text = LanguageUtil.getString(this, "redpacket_send_num")
        et_money?.hint = LanguageUtil.getString(this, "redpacket_send_inputAmount")
        tv_redpacket_send_wishes?.hint = LanguageUtil.getString(this, "redpacket_send_wishes")
        create_red_package?.setContent(LanguageUtil.getString(this, "redpacket_send_prepare"))
        redPackageInitInfo()

        iv_close?.setOnClickListener {
            KeyBoardUtils.closeKeyBoard(context)
            finish()
        }

        subTitleList.add( LanguageUtil.getString(context, "redpacket_sendout_sendPackets"))
        subTitleList.add( LanguageUtil.getString(context, "redpacket_received_received"))

        // loadView(redPackageType)

        /**
         * 查看红包
         */
        iv_menu?.setOnClickListener {
            redPackageDialog = NewDialogUtils.showBottomListDialog(context, subTitleList, 0, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    redPackageDialog?.dismissAllowingStateLoss()
                    when (item) {
                        0 -> {
                            startActivity(Intent(context, GrantRedPackageListActivity::class.java))
                        }

                        1 -> {
                            startActivity(Intent(context, ReceiveRedPackageListActivity::class.java))
                        }
                    }
                }
            })
        }


        cb_new_user?.setOnCheckedChangeListener { _, isChecked ->
            isNew = if (isChecked) 1 else 0
        }


        /**
         * 切换红包类型
         */
        rg_red_package_type?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_lucky -> {
                    redPackageType = LUCKY_RED_PACKAGE
                    rb_lucky?.typeface = Typeface.DEFAULT_BOLD
                    rb_normal?.typeface = Typeface.DEFAULT
                }
                R.id.rb_normal -> {
                    redPackageType = NORMAL_RED_PACKAGE
                    rb_normal?.typeface = Typeface.DEFAULT_BOLD
                    rb_lucky?.typeface = Typeface.DEFAULT
                }
            }
            loadView(redPackageType)
        }

        /**
         * 监听红包个数
         */
        et_mount?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isEnable(et_mount) && isEnable(et_money)) {
                    create_red_package?.isEnable(true)
                } else {
                    create_red_package?.isEnable(false)
                }
                /**
                 * 普通红包下:计算总金额
                 */
                if (redPackageType == NORMAL_RED_PACKAGE) {
                    if (!TextUtils.isEmpty(s.toString()) && !TextUtils.isEmpty(et_money?.text.toString())) {
                        tv_total_money?.text =  LanguageUtil.getString(context, "redpacket_send_total") + " ${BigDecimalUtils.mul(s.toString(), et_money?.text.toString()).toPlainString()}"
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        et_money?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isEnable(et_mount) && isEnable(et_money)) {
                    create_red_package?.isEnable(true)
                } else {
                    create_red_package?.isEnable(false)
                }
                /**
                 * 普通红包下:计算总金额
                 */
                if (redPackageType == NORMAL_RED_PACKAGE) {
                    if (!TextUtils.isEmpty(s.toString()) && !TextUtils.isEmpty(et_mount?.textContent.toString())) {
                        tv_total_money?.text =  LanguageUtil.getString(context, "redpacket_send_total") + " ${BigDecimalUtils.mul(s.toString(), et_mount?.textContent.toString()).toPlainString()}"
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        /**
         * 塞进红包
         */
        create_red_package?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                val tip: String = if (TextUtils.isEmpty(et_greetings.textContent)) {
                    et_greetings?.hint.toString()
                } else {
                    et_greetings?.textContent.toString()
                }

                Log.d(TAG, "=======mount: ${et_mount.textContent},${et_mount?.text.toString()},money:${et_money?.text.toString()}=====")

                if (BigDecimalUtils.compareTo(et_mount?.textContent, selectedSymbol?.singleCountMax.toString()) > 0) {
                    showSnackBar( LanguageUtil.getString(context, "redpacket_send_numNotExceed").format(selectedSymbol?.singleCountMax.toString()), isSuc = false)
                    return
                }


                val totalMoney = if (redPackageType == LUCKY_RED_PACKAGE) {
                    et_money?.text.toString()
                } else {
                    BigDecimalUtils.mul(et_money?.text.toString(), et_mount?.text?.toString()).toPlainString()
                }

                if (BigDecimalUtils.compareTo(totalMoney, selectedSymbol?.amount.toString()) > 0) {
                    showSnackBar( LanguageUtil.getString(context, "redpacket_send_noMoney"), isSuc = false)
                    return
                }

                val coinName = NCoinManager.getShowMarket(selectedSymbol?.coinSymbol).toString().toUpperCase()

                if (redPackageType == LUCKY_RED_PACKAGE) {
                    /**
                     * 凭手气红包
                     */

                    /**
                     * 红包总额
                     * 红包的最大份数 * 红包的输入数量
                     */
                    val maxAmount = BigDecimalUtils.mul(selectedSymbol?.singleAmountMax.toString(), et_mount?.textContent).toPlainString()
                    if (BigDecimalUtils.compareTo(et_money?.text.toString(), maxAmount) > 0) {
                        showSnackBar( LanguageUtil.getString(context, "redpacket_send_notExceed").format(BigDecimal(maxAmount.toString()).toPlainString() + coinName),
                                isSuc = false)
                        return
                    }

                    /**
                     * 单个红包的额度
                     */
                    val perRedPackageAmount = BigDecimalUtils.div(et_money?.text.toString(), et_mount?.text.toString()).toPlainString()
                    if (BigDecimalUtils.compareTo(perRedPackageAmount, selectedSymbol?.singleAmountMin.toString()) < 0) {
                        showSnackBar( LanguageUtil.getString(context, "redpacket_send_notLess").format(BigDecimal(selectedSymbol?.singleAmountMin?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                    if (BigDecimalUtils.compareTo(perRedPackageAmount, selectedSymbol?.singleAmountMax.toString()) > 0) {
                        showSnackBar( LanguageUtil.getString(context, "redpacket_send_singleNotExceed").format(BigDecimal(selectedSymbol?.singleAmountMax?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }


                } else {
                    /**
                     * 普通红包
                     */

                    /**
                     * 单个红包的额度
                     */
                    if (BigDecimalUtils.compareTo(et_money?.text.toString(), selectedSymbol?.singleAmountMin.toString()) < 0) {

                        showSnackBar( LanguageUtil.getString(context, "redpacket_send_notLess").format(BigDecimal(selectedSymbol?.singleAmountMin?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                    if (BigDecimalUtils.compareTo(et_money?.text.toString(), selectedSymbol?.singleAmountMax.toString()) > 0) {
                        showSnackBar( LanguageUtil.getString(context, "redpacket_send_singleNotExceed").format(BigDecimal(selectedSymbol?.singleAmountMax?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                }


                NewDialogUtils.order4RedPackage(context, TempBean(selectedSymbol?.coinSymbol, totalMoney), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        createRedPackage(redPackageType, selectedSymbol?.coinSymbol.toString(), totalMoney, et_mount.textContent, tip, isNew)
                    }
                })

            }
        }

    }

    fun loadView(type: Int = LUCKY_RED_PACKAGE) {
        tv_money_title?.text = if (type == LUCKY_RED_PACKAGE) {
             LanguageUtil.getString(context, "redpacket_send_total")
        } else {
             LanguageUtil.getString(context, "redpacket_send_each")
        }

        tv_total_money?.visibility = if (type == LUCKY_RED_PACKAGE) View.GONE else View.VISIBLE
        tv_total_money?.text =  LanguageUtil.getString(context, "redpacket_send_total")
        et_mount?.isShowLine = true
        et_greetings?.isShowLine = true

        tv_available_balance?.text =  LanguageUtil.getString(context, "withdraw_text_available")


        val filter = redPackageInitInfo?.symbolList?.filter {
            if (redPackageType == LUCKY_RED_PACKAGE) {
                /**
                 * 过滤支持"拼手气红包"
                 */
                it?.randomStatus == 1
            } else {
                /**
                 * 过滤支持"普通红包"
                 */
                it?.generalStatus == 1
            }
        }
        if (filter == null || filter.isEmpty()) return
        selectedSymbol = filter[0]
        selectCoin(selectedSymbol)

        tv_select_coin?.setOnClickListener {
            val filter = redPackageInitInfo?.symbolList?.filter {
                if (redPackageType == LUCKY_RED_PACKAGE) {
                    /**
                     * 过滤支持"拼手气红包"
                     */
                    it?.randomStatus == 1
                } else {
                    /**
                     * 过滤支持"普通红包"
                     */
                    it?.generalStatus == 1
                }
            }
            if (filter == null || filter.isEmpty()) return@setOnClickListener
            selectedSymbol = filter[0]
            selectCoin(selectedSymbol)
            NewDialogUtils.selectSymbol4RedPackage(context, ArrayList(filter), object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(position: Int) {
                    if (ArrayList(filter).size <= position) return
                    selectedSymbol = ArrayList(filter)[position]
                    selectCoin(ArrayList(filter)[position])
                }
            })
        }

        /**
         * 监听红包额度
         */
        precision = NCoinManager.getCoinShowPrecision(selectedSymbol?.coinSymbol)
        et_money?.filters = arrayOf(DecimalDigitsInputFilter(precision))

    }


    fun selectCoin(selectedSymbol: RedPackageInitInfo.Symbol?) {
        tv_select_coin?.text = NCoinManager.getShowMarket(selectedSymbol?.coinSymbol)
        tv_overtime?.text = LanguageUtil.getString(context, "redpacket_send_prompt").format(selectedSymbol?.expiredHour)
        tv_coin?.text = NCoinManager.getShowMarket(selectedSymbol?.coinSymbol)
        Log.d(TAG, "=======红包:${selectedSymbol?.amount},precision:${precision}===,${BigDecimalUtils.divForDown(selectedSymbol?.amount, precision).toPlainString()}==")
        tv_available_balance?.text = "${LanguageUtil.getString(context, "redpacket_send_availableBalance")} ${BigDecimalUtils.divForDown(selectedSymbol?.amount, precision).toPlainString()} ${NCoinManager.getShowMarket(selectedSymbol?.coinSymbol)}"
    }


    /**
     * 准备红包的初始信息
     */
    private fun redPackageInitInfo() {
        HttpClient.instance
                .redPackageInitInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<RedPackageInitInfo>() {
                    override fun onHandleSuccess(bean: RedPackageInitInfo?) {
                        if (bean == null) return
                        printLogcat("红包初始信息:${bean.toString()}")
                        et_greetings?.hint = bean.defaultTip.toString()
                        redPackageInitInfo = bean
                        loadView(redPackageType)
                        /**
                         * 过滤支持"拼手气红包"
                         */
                        bean.symbolList?.filter {
                            it?.randomStatus == 1
                        }
                        /**
                         * 过滤支持"普通红包"
                         */
                        bean.symbolList?.filter {
                            it?.generalStatus == 1
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@CreateRedPackageActivity,false, msg)
                    }
                })
    }


    /**
     * 创建红包的信息
     *
     */
    private fun createRedPackage(type: Int, coinSymbol: String, amount: String, count: String, tip: String, onlyNew: Int) {
        showProgressDialog()
        HttpClient.instance
                .createRedPackage(type, coinSymbol, amount, count, tip, onlyNew)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CreatePackageBean>() {
                    override fun onHandleSuccess(bean: CreatePackageBean?) {
                        cancelProgressDialog()
                        printLogcat("创建红包:${bean?.toString()}")
                        if (bean == null) return
                        createPackageBean = bean
                        /**
                         * 弹出Google认证
                         */
                        googleDialog = NewDialogUtils.showSecondDialog(context, 10, object : NewDialogUtils.DialogSecondListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                                //TODO
                                if (createPackageBean?.isVersion2 == 1) {
                                    pay4redPackage(bean?.orderNum ?: "", googleCode ?: "", phone
                                            ?: "", amount)
                                } else {
                                    val payBean = PayBean(bean?.appKey, bean?.assetType.toString(), bean?.orderNum, bean?.userId.toString(), googleCode, phone)
                                    pay(bean?.toPayUri + "/toPay",
                                            Gson().toJson(payBean), amount)
                                }
                                KeyBoardUtils.closeKeyBoard(context)
                                googleDialog?.dismiss()
                            }
                        }, loginPwdShow = false)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@CreateRedPackageActivity,false, msg)

                    }
                })
    }


    fun isEnable(editText: EditText): Boolean {
        val string = editText.text.toString()
        return !(TextUtils.isEmpty(string) || string.toDouble() == 0.0)
    }

    /**
     * 第三方支付
     */
    fun pay(url: String, json: String, amount: String) {
        showProgressDialog()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)
        val request = Request.Builder().url(url).post(requestBody).build()

        HttpClient.instance.mOkHttpClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    cancelProgressDialog()
                    NToastUtil.showTopToastNet(this@CreateRedPackageActivity,false, e.message)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response?.body?.string()
                printLogcat("支付成功" + string)
                runOnUiThread {
                    cancelProgressDialog()
                    try {
                        var json = JSONObject(string)
                        val msg = json.getString("msg")
                        val code = json.getString("code")
                        if (code == "0") {
                            showResult(amount)
                        } else {
                            NToastUtil.showTopToastNet(this@CreateRedPackageActivity,false, msg)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }
            }
        })
    }


    /**
     * 红包支付
     */
    private fun pay4redPackage(orderNum: String, googleCode: String = "", smsAuthCode: String = "", amount: String) {
        showProgressDialog()
        HttpClient.instance
                .pay4redPackage(orderNum = orderNum, googleCode = googleCode, smsAuthCode = smsAuthCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(bean: Any?) {
                        cancelProgressDialog()
                        showResult(amount)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@CreateRedPackageActivity,false, msg)
                    }
                })
    }

    private fun showResult(amount: String) {
        val sub = BigDecimalUtils.sub(selectedSymbol?.amount.toString(), amount).toPlainString()
        val intercept = BigDecimalUtils.intercept(sub, NCoinManager.getCoinShowPrecision(selectedSymbol?.coinSymbol
                ?: "")).toPlainString()
        selectedSymbol?.amount = intercept
        tv_available_balance?.text = "${LanguageUtil.getString(context, "withdraw_text_available")} ${selectedSymbol?.amount} ${NCoinManager.getShowMarket(selectedSymbol?.coinSymbol)}"
        NewDialogUtils.share4RedPackage(context, createPackageBean)
    }

}
