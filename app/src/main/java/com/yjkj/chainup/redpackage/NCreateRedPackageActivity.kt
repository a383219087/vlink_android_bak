package com.yjkj.chainup.redpackage

import android.content.Intent
import android.graphics.Typeface
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.redpackage.CreateRedPackageActivity
import com.yjkj.chainup.new_version.redpackage.GrantRedPackageListActivity
import com.yjkj.chainup.new_version.redpackage.ReceiveRedPackageListActivity
import com.yjkj.chainup.new_version.redpackage.bean.CreatePackageBean
import com.yjkj.chainup.new_version.redpackage.bean.PayBean
import com.yjkj.chainup.new_version.redpackage.bean.RedPackageInitInfo
import com.yjkj.chainup.new_version.redpackage.bean.TempBean
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DecimalDigitsInputFilter
import com.yjkj.chainup.util.KeyBoardUtils
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
 * @Date：2019/9/4-10:26 AM
 * @Description: 发红包界面
 *
 */
class NCreateRedPackageActivity : NBaseActivity() {

    var redPackageType = LUCKY_RED_PACKAGE

    var redPackageDialog: TDialog? = null

    var subTitleList = ArrayList<String>()

    var selectedSymbol: RedPackageInitInfo.Symbol? = null

    var redPackageInitInfo: RedPackageInitInfo? = null

    var createPackageBean: CreatePackageBean? = null

    var googleDialog: TDialog? = null

    //1.只针对新用户 0.不做限制
    private var isNew = 0


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

    override fun setContentView() = R.layout.activity_create_red_package

    override fun loadData() {
        redPackageInitInfo()
        subTitleList.add(LanguageUtil.getString(this,"redpacket_sendout_sendPackets"))
        subTitleList.add(LanguageUtil.getString(this,"redpacket_received_received"))
    }

    override fun initView() {
        loadView()

        cb_new_user?.setOnCheckedChangeListener { _, isChecked ->
            isNew = if (isChecked) 1 else 0
        }

        /**
         * 切换红包类型
         */
        rg_red_package_type?.setOnCheckedChangeListener { _, checkedId ->
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
                        tv_total_money?.text = LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_total") + " ${BigDecimalUtils.mul(s.toString(), et_money?.text.toString()).toPlainString()}"
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        /**
         * 监听红包额度
         */

        et_money?.run {
            val precision = DataManager.getCoinByName(selectedSymbol?.coinSymbol
                    ?: "")?.showPrecision
                    ?: 4
            filters = arrayOf(DecimalDigitsInputFilter(precision))
            addTextChangedListener(object : TextWatcher {
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
                            tv_total_money?.text = LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_total") + " ${BigDecimalUtils.mul(s.toString(), et_mount?.textContent.toString()).toPlainString()}"
                        }
                    }

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }


        /**
         * 塞进红包
         */
        create_red_package?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                val tip = et_greetings?.run {
                    if (TextUtils.isEmpty(et_greetings.textContent)) {
                        hint.toString()
                    } else {
                        textContent
                    }
                }

                Log.d(TAG, "=======mount: ${et_mount.textContent},${et_mount?.text.toString()},money:${et_money?.text.toString()}=====")

                if (BigDecimalUtils.compareTo(et_mount?.textContent, selectedSymbol?.singleCountMax.toString()) > 0) {
                    showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_numNotExceed").format(selectedSymbol?.singleCountMax.toString()), isSuc = false)
                    return
                }


                val totalMoney = if (redPackageType == LUCKY_RED_PACKAGE) {
                    et_money?.text.toString()
                } else {
                    BigDecimalUtils.mul(et_money?.text.toString(), et_mount?.text?.toString()).toPlainString()
                }

                if (BigDecimalUtils.compareTo(totalMoney, selectedSymbol?.amount.toString()) > 0) {
                    showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_noMoney"), isSuc = false)
                    return
                }

                val coinName = selectedSymbol?.coinSymbol.toString().toUpperCase()

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
                        showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_notExceed").format(BigDecimal(maxAmount.toString()).toPlainString() + coinName),
                                isSuc = false)
                        return
                    }

                    /**
                     * 单个红包的额度
                     */
                    val perRedPackageAmount = BigDecimalUtils.div(et_money?.text.toString(), et_mount?.text.toString()).toPlainString()
                    if (BigDecimalUtils.compareTo(perRedPackageAmount, selectedSymbol?.singleAmountMin.toString()) < 0) {
                        showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_notLess").format(BigDecimal(selectedSymbol?.singleAmountMin?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                    if (BigDecimalUtils.compareTo(perRedPackageAmount, selectedSymbol?.singleAmountMax.toString()) > 0) {
                        showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_singleNotExceed").format(BigDecimal(selectedSymbol?.singleAmountMax?.toString()).toPlainString() + coinName)
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

                        showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_notLess").format(BigDecimal(selectedSymbol?.singleAmountMin?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                    if (BigDecimalUtils.compareTo(et_money?.text.toString(), selectedSymbol?.singleAmountMax.toString()) > 0) {
                        showSnackBar(LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_singleNotExceed").format(BigDecimal(selectedSymbol?.singleAmountMax?.toString()).toPlainString() + coinName)
                                , isSuc = false)
                        return
                    }
                }


                NewDialogUtils.order4RedPackage(mActivity, TempBean(selectedSymbol?.coinSymbol, totalMoney), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        createRedPackage(redPackageType, selectedSymbol?.coinSymbol.toString(), totalMoney, et_mount.textContent, tip
                                ?: "", isNew)
                    }
                })

            }
        }


    }

    fun isEnable(editText: EditText): Boolean {
        val string = editText.text.toString()
        return !(TextUtils.isEmpty(string) || string.toDouble() == 0.0)
    }

    override fun onClick(view: View) {
        super.onClick(view)
        iv_close?.setOnClickListener {
            KeyBoardUtils.closeKeyBoard(mActivity)
            finish()
        }

        /**
         * 查看红包
         */
        iv_menu?.setOnClickListener {
            redPackageDialog = NewDialogUtils.showBottomListDialog(mActivity, subTitleList, 0, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    redPackageDialog?.dismissAllowingStateLoss()
                    when (item) {
                        0 -> {
                            startActivity(Intent(mActivity, GrantRedPackageListActivity::class.java))
                        }

                        1 -> {
                            startActivity(Intent(mActivity, ReceiveRedPackageListActivity::class.java))
                        }
                    }
                }
            })
        }
    }

    /**
     * 根据红包类型来加载view
     */
    private fun loadView(type: Int = CreateRedPackageActivity.LUCKY_RED_PACKAGE) {
        tv_money_title?.text = if (type == CreateRedPackageActivity.LUCKY_RED_PACKAGE) {
            LanguageUtil.getString(this,"redpacket_send_total")
        } else {
            LanguageUtil.getString(this,"redpacket_send_each")
        }

        tv_total_money?.run {
            visibility = if (type == CreateRedPackageActivity.LUCKY_RED_PACKAGE) View.GONE else View.VISIBLE
            text = LanguageUtil.getString(this@NCreateRedPackageActivity,"redpacket_send_total")
        }

        et_mount?.isShowLine = true
        et_greetings?.isShowLine = true

        tv_available_balance?.text = LanguageUtil.getString(this,"withdraw_text_available")


        val filter = redPackageInitInfo?.symbolList?.filter {
            if (redPackageType == CreateRedPackageActivity.LUCKY_RED_PACKAGE) {
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
                if (redPackageType == CreateRedPackageActivity.LUCKY_RED_PACKAGE) {
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
            NewDialogUtils.selectSymbol4RedPackage(mActivity, ArrayList(filter), object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(position: Int) {
                    selectedSymbol = ArrayList(filter)[position]
                    selectCoin(ArrayList(filter)[position])
                }
            })
        }

    }


    fun selectCoin(selectedSymbol: RedPackageInitInfo.Symbol?) {
        tv_select_coin?.text = selectedSymbol?.coinSymbol
        tv_overtime?.text = LanguageUtil.getString(this,"redpacket_send_prompt").format(selectedSymbol?.expiredHour)
        tv_coin?.text = selectedSymbol?.coinSymbol
        tv_available_balance?.text = "${LanguageUtil.getString(this,"redpacket_send_availableBalance")} ${selectedSymbol?.amount} ${selectedSymbol?.coinSymbol}"
    }


    /**
     * 准备红包的初始信息
     */
    private fun redPackageInitInfo() {
        addDisposable(getRedPackageModel().redPackageInitInfo(consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {

                if(null==jsonObject)
                    return
                et_greetings?.hint = if (jsonObject?.has("defaultTip")) {
                    jsonObject?.getString("defaultTip")
                } else {
                    ""
                }

                loadView(redPackageType)

                if (jsonObject.has("symbolList")) {
                    val jsonArray = jsonObject.getJSONArray("symbolList")
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                    }

                }

            }

        }))



        HttpClient.instance
                .redPackageInitInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<RedPackageInitInfo>() {
                    override fun onHandleSuccess(bean: RedPackageInitInfo?) {
                        redPackageInitInfo = bean?.apply {
                            Log.d(TAG, "红包初始信息:${bean}")
                            et_greetings?.hint = bean.defaultTip.toString()
                            loadView(redPackageType)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        showSnackBar(msg, false)
                    }
                })
    }


    /**
     * 创建红包的信息
     *
     */
    private fun createRedPackage(type: Int, coinSymbol: String, amount: String, count: String, tip: String, onlyNew: Int) {
        addDisposable(getRedPackageModel().createRedPackage(type, coinSymbol, amount, count, tip, onlyNew, consumer = object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {

                /**
                 * 弹出Google认证
                 */
                googleDialog = NewDialogUtils.showSecondDialog(this@NCreateRedPackageActivity, 10, object : NewDialogUtils.DialogSecondListener {
                    override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {


                        val appKey = jsonObject?.getString("appKey")
                        val assetType = jsonObject?.getString("assetType")
                        val orderNum = jsonObject?.getString("assetType")
                        val userId = jsonObject?.getString("userId")
                        val payUrl = jsonObject?.getString("toPayUri") + "/toPay"

//                        payBack(appKey = appKey,assetT)


                        //TODO 调用开放平台的支付
//                        val payBean = PayBean(bean?.appKey, bean?.assetType.toString(), bean?.orderNum, bean?.userId.toString(), googleCode, phone)

//                        pay(bean?.toPayUri + "/toPay",
//                                Gson().toJson(payBean), amount)

                        KeyBoardUtils.closeKeyBoard(this@NCreateRedPackageActivity)
                        googleDialog?.dismiss()
                    }
                }, loginPwdShow = false)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
            }

        }))






        HttpClient.instance
                .createRedPackage(type, coinSymbol, amount, count, tip, onlyNew)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CreatePackageBean>() {
                    override fun onHandleSuccess(bean: CreatePackageBean?) {
                        closeLoadingDialog()
                        Log.d(TAG, "创建红包:${bean?.toString()}")
                        if (bean == null) return
                        createPackageBean = bean
                        /**
                         * 弹出Google认证
                         */
                        googleDialog = NewDialogUtils.showSecondDialog(mActivity, 10, object : NewDialogUtils.DialogSecondListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                                //TODO 调用开放平台的支付
                                val payBean = PayBean(bean?.appKey, bean?.assetType.toString(), bean?.orderNum, bean?.userId.toString(), googleCode, phone)

                                pay(bean?.toPayUri + "/toPay",
                                        Gson().toJson(payBean), amount)

                                KeyBoardUtils.closeKeyBoard(mActivity)
                                googleDialog?.dismiss()
                            }
                        }, loginPwdShow = false)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        closeLoadingDialog()
                        showSnackBar(msg, false)
                    }
                })
    }


    fun payBack(appKey: String, outOrderId: String, orderStatus: String) {
        addDisposable(getRedPackageModel().payCallback4redPackage(appKey, outOrderId, orderStatus, consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {

            }

        }))
    }


    /**
     * 第三方支付
     */
    fun pay(url: String, json: String, amount: String) {
        showLoadingDialog()


        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)
        val request = Request.Builder().url(url).post(requestBody).build()

        HttpClient.instance.mOkHttpClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    closeLoadingDialog()
                    showSnackBar(e.message, false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response?.body?.string()
                Log.d(TAG, "支付成功$string")
                runOnUiThread {
                    closeLoadingDialog()
                    try {
                        var json = JSONObject(string)
                        val msg = json.getString("msg")
                        val code = json.getString("code")
                        if (code == "0") {
                            val sub = BigDecimalUtils.sub(selectedSymbol?.amount.toString(), amount).toPlainString()
                            val intercept = BigDecimalUtils.intercept(sub, NCoinManager.getCoinShowPrecision(selectedSymbol?.coinSymbol
                                    ?: "")).toPlainString()
                            selectedSymbol?.amount = intercept
                            tv_available_balance?.text = "${LanguageUtil.getString(this@NCreateRedPackageActivity,"withdraw_text_available")} ${selectedSymbol?.amount} ${selectedSymbol?.coinSymbol}"
                            NewDialogUtils.share4RedPackage(mActivity, createPackageBean)
                        } else {
                            showSnackBar(msg, false)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }
            }
        })
    }


}
