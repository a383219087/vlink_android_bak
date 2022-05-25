package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.ShowImageActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.activity.asset.FIRST_INDEX
import com.yjkj.chainup.new_version.bean.ImageTokenBean
import com.yjkj.chainup.new_version.bean.OTCOrderDetailBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.OnSaveSuccessListener
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.UploadHelper
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_complaint.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/4/22-10:29 AM
 * @Email buptjinlong@163.com
 * @description  申诉
 */

/**
 * 问题类型
 * 1.意见与建议 2.充值提现 3.交易相关 4.安全相关 5.个人信息 6.实名认证,7.场外申诉,8.对方未放行,9.买方未付款
 * 此处只用7和8  默认7
 */
const val SELECTITEM = "7"
const val OTC_COMPLAIN_ORDER = "otc_complain_order"
const val OTC_TRADING_TYPE = "otc_trading_type"
const val OTC_COMPLAINT_TYPE = "otc_complaint_type"
const val OTC_COMPLAINT_SYMBOL = "otc_complaint_symbol"

class NewComplaintActivity : NewBaseActivity() {

    var orderId = ""
    var complaintType = "7"
    var symbol = ""

    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null

    var tradingType = true

    var normalStatus = false

    /**
     * 上传图片 oss  获取token
     */
    var imageTokenBean: ImageTokenBean = ImageTokenBean()

    companion object {
        /**
         * orderId 订单id
         */
        fun enter2(context: Context, orderId: String, tradingType: Boolean, complaintType: String, symbol: String) {
            var intent = Intent(context, NewComplaintActivity::class.java)
            intent.putExtra(OTC_COMPLAIN_ORDER, orderId)
            intent.putExtra(OTC_TRADING_TYPE, tradingType)
            intent.putExtra(OTC_COMPLAINT_TYPE, complaintType)
            intent.putExtra(OTC_COMPLAINT_SYMBOL, symbol)
            context.startActivity(intent)
        }
    }

    fun setTextContent() {
        title_layout?.setContentTitle(getStringContent("otc_shensu"))
        cub_confirm?.setContent(getStringContent("common_text_btnConfirm"))
        tv_user_nick_title?.text = getStringContent("otcSafeAlert_action_nickname")
        tv_user_real_title?.text = getStringContent("new_otc_real_name")
        tv_money_title?.text = getStringContent("journalAccount_text_amount")
        tv_shensu_reason?.text = getStringContent("otc_shensu_reason_4")
        rg_other?.text = getStringContent("appeal_action_reasonOther")
        tv_shensu_img?.text = getStringContent("otc_shensu_img")
        tv_tip_tradeHintTitle?.text = getStringContent("otc_tip_tradeHintTitle")
        tv_trading_remind?.text = getStringContent("appeal_explain_warning")
        edt_buy_why?.hint = getStringContent("appeal_tip_reasonOtherPlaceholder")
    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_complaint)
        getData()
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }
        }
        getOrderDetail4OTC()
        val rxPermissions: RxPermissions = RxPermissions(this)
        /**
         * 获取读写权限
         */
        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({ granted ->
                    if (granted) {
                        imageTool = ImageTools(this)
                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "warn_storage_permission"), isSuc = false)
                    }

                })
        setTextContent()
    }


    fun getData() {
        if (intent != null) {
            orderId = intent.getStringExtra(OTC_COMPLAIN_ORDER) ?: ""
            symbol = intent.getStringExtra(OTC_COMPLAINT_SYMBOL) ?: ""
            complaintType = intent.getStringExtra(OTC_COMPLAINT_TYPE) ?: "7"
            tradingType = intent.getBooleanExtra(OTC_TRADING_TYPE, true)
        }
        if (tradingType) {
            rg_no_party?.text = LanguageUtil.getString(this, "appeal_action_reasonNotReceiveCoin")
            rg_amount_not_enough?.text = LanguageUtil.getString(this, "appeal_action_reasonOverPaid")

        } else {
            rg_no_party?.text = LanguageUtil.getString(this, "appeal_action_reasonNotPaid")
            rg_amount_not_enough?.text = LanguageUtil.getString(this, "appeal_action_reasonLessPaid")
        }
        rg_other?.text = LanguageUtil.getString(this, "appeal_action_reasonOther")

        title_layout?.rightIcon(R.drawable.fiat_message)
        edt_buy_why?.isFocusable = true
        edt_buy_why?.isFocusableInTouchMode = true
        edt_buy_why?.setOnFocusChangeListener { v, hasFocus ->
            edt_buy_why_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        if (!normalStatus) {
            edt_buy_why?.visibility = View.GONE
        }
        setEdtVisible(false)
        edt_buy_why?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    describeContent = s.toString()
                }
            }

        })
    }


    fun setEdtVisible(status: Boolean) {
        if (status) {
            edt_buy_why?.visibility = View.VISIBLE
            edt_buy_why_line?.visibility = View.VISIBLE
        } else {
            edt_buy_why?.visibility = View.GONE
            edt_buy_why_line?.visibility = View.GONE
        }
    }

    var complaintTypeEight = ""
    var complaintTypeNine = ""

    fun initView(bean: OTCOrderDetailBean) {

        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
//                OTCIMActivity.newIntent(this@NewComplaintActivity, bean?.complainId, bean?.coin, bean?.totalPrice.toString(), bean?.status.toString(),
//                        bean?.paycoin, DateUtils.longToString("yyyy-MM-dd HH:mm:ss", bean?.ctime))
            }

            override fun onclickName() {

            }

        }
        /**
         * 订单
         */
        tv_user_nick_title?.text = LanguageUtil.getString(this, "otc_text_orderId")
        /**
         * 订单号
         */
        tv_user_nick_name?.text = bean.sequence

        tv_user_real_title?.text = LanguageUtil.getString(this, "otcSafeAlert_action_nickname")
        /**
         * 名字
         */
        tv_user_real_name?.text = bean.seller.otcNickName
        describeContent = LanguageUtil.getString(this, "appeal_action_reasonNotReceiveCoin")

        tv_money_title?.text = LanguageUtil.getString(this, "appeal_text_amount") + "($symbol)"
        /**
         * 申诉金额
         */
        var presision = RateManager.getFiat4Coin(symbol)
        var totalPriceN = BigDecimalUtils.divForDown(bean?.totalPrice, presision).toPlainString()
        tv_money?.text = totalPriceN ?: ""

        rg_complaint_layout?.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {

            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when (checkedId) {
                    /**
                     *  对方迟迟不放币
                     */
                    R.id.rg_no_party -> {
                        complaintTypeEight = "8"
                        complaintTypeNine = ""
                        describeContent = LanguageUtil.getString(this@NewComplaintActivity, "appeal_action_reasonNotReceiveCoin")
                        setEdtVisible(false)
                        normalStatus = false

                    }
                    /**
                     * 我支付的金额大于订单金额
                     */
                    R.id.rg_amount_not_enough -> {
                        complaintTypeEight = ""
                        complaintTypeNine = "10"
                        describeContent = LanguageUtil.getString(this@NewComplaintActivity, "appeal_action_reasonOverPaid")
                        setEdtVisible(false)
                        normalStatus = false
                    }
                    /**
                     * 其他
                     */
                    R.id.rg_other -> {
                        describeContent = ""
                        complaintTypeEight = ""
                        complaintTypeNine = ""
                        setEdtVisible(true)
                        normalStatus = true
                    }
                }
            }

        })
        /**
         * 点击图片
         */
        iv_update_img?.setOnClickListener {
            if (firstImgPath.isNotEmpty()) {
                ShowImageActivity.enter2(this, LocalAddress, false)
            } else {
                showBottomDialog()
            }

        }

        iv_cancel?.setOnClickListener {
            firstImgPath = ""
            LocalAddress = ""
            iv_update_img?.setImageResource(R.drawable.assets_addingpaymentmethod)
            iv_cancel?.visibility = View.GONE
        }
        /**
         * 点击申诉
         */
        cub_confirm?.isEnable(true)
        cub_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (describeContent.isEmpty()) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewComplaintActivity, "appeal_text_reason"), isSuc = false)
                    return
                }
                NewDialogUtils.showNormalDialog(this@NewComplaintActivity, LanguageUtil.getString(this@NewComplaintActivity, "otc_tip_appealconfirm"), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        createProblem(if (normalStatus) edt_buy_why?.text.toString() else describeContent, complaintType, complaintTypeEight, complaintTypeNine, firstImgPath)
                    }
                })
            }
        }

    }

    var describeContent = ""


    var imageDialog: TDialog? = null

    /**
     * 照片选择 或者  证件选择
     * titleText 标题
     * fristText 第一个选项
     * secondTextUtils 第二个选项
     */
    fun showBottomDialog() {
        imageDialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(LanguageUtil.getString(this, "noun_camera_takeAlbum"), LanguageUtil.getString(this, "noun_camera_takephoto")), 0, object : NewDialogUtils.DialogOnclickListener {
            override fun clickItem(data: ArrayList<String>, item: Int) {
                when (item) {
                    0 -> {
                        if (isFinishing && isDestroyed) return
                        imageTool?.openGallery("")
                    }
                    1 -> {
                        if (isFinishing && isDestroyed) return
                        openCamera()
                    }
                }
                imageDialog?.dismiss()
            }
        })
    }


    /**
     * 获取订单详情
     */
    private fun getOrderDetail4OTC() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        orderId = intent?.getStringExtra(OTC_COMPLAIN_ORDER)!!
        showProgressDialog()
        HttpClient.instance
                .getOrderDetail4OTC(sequence = orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderDetailBean>() {
                    override fun onHandleSuccess(t: OTCOrderDetailBean?) {
                        cancelProgressDialog()
                        initView(t ?: return)

                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }
    /**
     * @param rqType
     * 1.意见与建议
     * 2.充值提现
     * 3.交易相关
     * 4.安全相关
     * 5.个人信息
     * 6.实名认证,
     * 7.场外申诉,
     * 8.对方未放行,
     * 9.买方未付款,
     * 10:支付金额大于订单金额,
     * 11:订单金额大于支付金额,
     * 12:场外其他原因(10,11,12新增)
     */
    /**
     * 提交申诉
     */
    fun createProblem(rqDescribe: String, rqType: String, rqUnreleased: String, rqUnpaid: String, imageDataStr: String) {
        HttpClient.instance.createProblem4OTC(rqDescribe, rqType, rqUnreleased, rqUnpaid, imageDataStr)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        Log.d("okhttp", t.toString())
                        var json = JSONObject(t.toString())
                        var complainId = json.getString("complainId")
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewComplaintActivity, "otc_tip_appealSuccess"), isSuc = true)
                        complain2changeOrderState4OTC(orderId, complainId)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }


    /**
     * 获取相机权限
     */
    private fun openCamera() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe({ granted ->
                    if (granted) {
                        imageTool?.openCamera("")
                    } else {
                        ToastUtils.showToast(LanguageUtil.getString(this, "warn_camera_permission"))
                    }

                })
    }

    var firstImgPath = ""

    /**
     * 上传照片
     */
    fun uploadImg(imageBase: String, index: Int) {
        showProgressDialog(LanguageUtil.getString(this, "pic_uploading"))
        HttpClient.instance.uploadImg(imgBase64 = imageBase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        if (t == null) return
                        var json = JSONObject(t.toString())
                        cancelProgressDialog()

                        val baseImgUrl = json.getString("base_image_url")
                        val fileName = json.getString("filename")

                        if (!TextUtils.isEmpty(fileName)) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewComplaintActivity, "toast_upload_pic_suc"), isSuc = true)

                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewComplaintActivity, "toast_upload_pic_failed"), isSuc = false)

                        }

                        iv_cancel?.visibility = View.VISIBLE
                        firstImgPath = baseImgUrl + fileName
                        if (TextUtils.isEmpty(fileName)) {
                            iv_update_img?.setImageResource(R.drawable.assets_addingpaymentmethod)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewComplaintActivity, "toast_upload_pic_failed"), isSuc = false)

                    }
                })
    }

    var LocalAddress = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageTool?.onAcitvityResult(requestCode, resultCode, data
        ) { bitmap, path ->
            LocalAddress = path
            if (PublicInfoDataService.getInstance().getUploadImgType(null) == "1") {
                Utils.saveBitmap(path, object : OnSaveSuccessListener {
                    override fun onSuccess(path: String) {
                        if (path != null) {
                            loadingImage(path)
                        }
                    }
                })
            } else {
                var options = RequestOptions().placeholder(R.drawable.assets_addingpaymentmethod)
                        .error(R.drawable.assets_addingpaymentmethod)
                GlideUtils.load(this, path, iv_update_img, options)
                val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                uploadImg(bitmap2Base64!!, FIRST_INDEX)
            }
        }
    }

    var isRefresh = false

    /**
     * 新接口 获取token 图片
     */
    fun getImageToken(operate_type: String = "2", path: String = "") {
        showProgressDialog()
        HttpClient.instance.getImageToken(operate_type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ImageTokenBean>() {
                    override fun onHandleSuccess(t: ImageTokenBean?) {
                        cancelProgressDialog()
                        t ?: return
                        imageTokenBean = t
                        if (isRefresh) {
                            isRefresh = false
                            loadingImage(path)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })

    }

    /**
     * 上传图片oss 方式
     */
    fun loadingImage(path: String) {
        showProgressDialog()
        var uploadHelper = UploadHelper.uploadImage(path, imageTokenBean.AccessKeyId, imageTokenBean.AccessKeySecret, imageTokenBean.bucketName,
                imageTokenBean.ossUrl, imageTokenBean.SecurityToken, imageTokenBean.catalog)
        cancelProgressDialog()
        if (TextUtils.isEmpty(uploadHelper)) {
            isRefresh = true
            getImageToken(operate_type = "2", path = path)
            return
        }
        var options = RequestOptions().placeholder(R.drawable.ic_sample)
                .error(R.drawable.ic_sample)

        cancelProgressDialog()
        iv_cancel?.visibility = View.VISIBLE
        firstImgPath = path
        firstImgPath = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))
        GlideUtils.load(this@NewComplaintActivity, path, iv_update_img, options)
    }


    /**
     * 修改订单
     */
    fun complain2changeOrderState4OTC(sequence: String, complainId: String) {
        HttpClient.instance.complain2changeOrderState4OTC(sequence, complainId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        if (tradingType) {
                            NewVersionBuyOrderActivity.enter2(this@NewComplaintActivity, orderId)
                        } else {
                            NewVersionSellOrderActivity.enter2(this@NewComplaintActivity, orderId)
                        }
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        ToastUtils.showToast(msg)
                    }

                })
    }


}