package com.yjkj.chainup.new_version.activity.personalCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.asset.FIRST_INDEX
import com.yjkj.chainup.new_version.bean.ImageTokenBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.OnSaveSuccessListener
import com.yjkj.chainup.new_version.view.UploadHelper
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.ToastUtils
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_info.*
import kotlinx.android.synthetic.main.activity_personal_info.title_layout
import org.json.JSONObject


/**
 * @author Bertking
 * @description 个人资料
 * @date 2018-6-5
 */

@Route(path = RoutePath.PersonalInfoActivity)
class PersonalInfoActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_personal_info
    }

    lateinit var comItemView: EditText




    /**
     * 获取相机权限
     */
    @SuppressLint("CheckResult")
    private fun openCamera() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted) {
                        Log.d(TAG, "---拿到权限---")
                        imageTool?.openCamera("")
                    } else {
                        ToastUtils.showToast(LanguageUtil.getString(this,"warn_camera_permission"))
                    }

                }
    }

    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null

    override fun onResume() {
        super.onResume()
        if (UserDataService.getInstance().isLogined) {
            getUserInfo()
            getSign()
        }
        initViews()
        initClickListener()
        title_layout?.setContentTitle(LanguageUtil.getString(this,"userinfo_text_data"))
        v_edit_nick?.setTitle(LanguageUtil.getString(this,"otcSafeAlert_action_nickname"))
        v_edit_sign?.setTitle(LanguageUtil.getString(this,"otcSafeAlert_action_sign"))
        v_account?.setTitle(LanguageUtil.getString(this,"userinfo_text_account"))
        v_account_state?.setTitle(LanguageUtil.getString(this,"userinfo_text_accountState"))
        v_verify?.setTitle(LanguageUtil.getString(this,"otcSafeAlert_action_identify"))
    }


    fun initViews() {
        val accountStatus = UserDataService.getInstance().accountStatus
        var status: String = ""
        // 账户状态 0.正常 1.冻结交易，冻结提现 2冻结交易 3冻结提现
        when (accountStatus) {
            0 -> {
                status = LanguageUtil.getString(this,"noun_account_normal")
            }

            1 -> {
                status = LanguageUtil.getString(this,"freeze_trade_withdraw")
            }

            2 -> {
                status = LanguageUtil.getString(this,"freeze_trade")
            }
            3 -> {
                status = LanguageUtil.getString(this,"freeze_withdraw")
            }
        }
        v_account_state.setStatusText(status)

        /**
         * 账户
         */
        v_account.setStatusText(UserDataService.getInstance().userAccount)

        /**
         * 昵称
         */
        if (TextUtils.isEmpty(UserDataService.getInstance().nickName)) {
            v_edit_nick.setStatusText(LanguageUtil.getString(this,"otc_no_nickname"))
        } else {
            v_edit_nick.setStatusText(UserDataService.getInstance().nickName)
        }

//        v_invite_code.setStatusText(userInfoData.inviteCode)

        /**
         * 认证类型
         *  认证状态 0、审核中，1、通过，2、未通过  3未认证
         */
        var auth = ""
        when (UserDataService.getInstance().authLevel) {
            0 -> {
                auth = LanguageUtil.getString(this,"noun_login_pending")
                v_verify.setShowArrow(false)
            }

            1 -> {
                auth = LanguageUtil.getString(this,"personal_text_verified")
                v_verify.setShowArrow(false)
            }
            /**
             * 审核未通过
             */
            2 -> {
                auth = LanguageUtil.getString(this,"personal_text_unverified")
                v_verify.setShowArrow(true)
            }

            3 -> {
                auth = LanguageUtil.getString(this,"personal_text_unverified")
                v_verify.setShowArrow(true)
            }
        }
        v_verify.setStatusText(auth)
    }

    var tDialog: TDialog? = null
    private fun initClickListener() {
        /**
         * 邀请码
         */
//        v_invite_code.setOnClickListener {
//            startActivity(Intent(context, InviteLinkActivity::class.java))
//        }
        /**
         * 设置头像
         */
//        v_edit_head.setOnClickListener {
//            tDialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(getString(R.string.subtitle_from_gallery), getString(R.string.subtitle_take_photo)), -1, object : NewDialogUtils.DialogOnclickListener {
//                override fun clickItem(data: ArrayList<String>, item: Int) {
//                    when (item) {
//                        0 -> {
//                            imageTool?.openGallery()
//                        }
//                        1 -> {
//                            openCamera()
//                        }
//                    }
//                }
//            })
//        }

        /**
         * 编辑昵称
         */
        v_edit_nick.setOnClickListener {
            showEditNickDialog()
        }

        /**
         * {"code":"0","msg":"成功","data":{"forceAuto":"0","openAuto":"1","toKenUrl":"https://api.megvii.com/faceid/lite/do?token=10ddc064bdf056d82302cc4f8a50336d"}}
         */
        v_verify.setOnClickListener {
            val authLevel = UserDataService.getInstance().authLevel
            when(authLevel){
                0 -> {
                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                }

                2,3->{
                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                }
            }

        }

    }

    var nickNameDialog: TDialog? = null
    /**
     * 编辑"Nick"的弹窗
     */
    fun showEditNickDialog() {
        nickNameDialog = NewDialogUtils.showAloneEdittextDialog(this, LanguageUtil.getString(this,"userinfo_text_nickname"), object : NewDialogUtils.DialogBottomAloneListener {
            override fun returnContent(content: String) {
                editNickname(nickname = content)
                nickNameDialog?.dismiss()
            }

        })
    }

    /**
     * 修改昵称
     */
    fun editNickname(nickname: String) {
        HttpClient.instance.editNickname(nickname)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@PersonalInfoActivity,"common_tip_editSuccess"), isSuc = true)

                        v_edit_nick.setStatusText(nickname)
                        val json = UserDataService.getInstance().userData
                        if (json != null) {
                            json.put("nickname", nickname)
                            UserDataService.getInstance().saveData(json)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }

                })
    }


    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        addDisposable(getMainModel().getUserInfo(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                UserDataService.getInstance().saveData(json)
            }

        }))
    }
    /**
     * 获取用户签名
     */
    private fun getSign() {
        addDisposable(getMainModel().getSign(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
              Log.d("我是",jsonObject.toString())
            }

        }))
    }


    var headImUrl = ""

    /**
     * 上传照片
     */
    fun uploadImg(imageBase: String, index: Int) {
        showLoadingDialog()
        HttpClient.instance.uploadImg(imgBase64 = imageBase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        if (t == null) return
                        var json = JSONObject(t.toString())
                        closeLoadingDialog()

                        Log.d(TAG, "===上传图片======json:" + json.toString())
                        val baseImgUrl = json.getString("base_image_url")
                        val fileName = json.getString("filename")


                        if (!TextUtils.isEmpty(fileName)) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@PersonalInfoActivity,"toast_upload_pic_suc"), isSuc = true)

                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@PersonalInfoActivity,"toast_upload_pic_failed"), isSuc = true)
                        }
                        headImUrl = baseImgUrl + fileName
                        if (TextUtils.isEmpty(headImUrl)) {
                            v_edit_head.setIvRightIcon(headImUrl)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        closeLoadingDialog()
                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@PersonalInfoActivity,"toast_upload_pic_failed"), isSuc = false)
                    }
                })
    }


    /**
     * TODO这里需要获取是否用新的上传图片
     */
    var isNewUpdateImage = false

    /**
     * 选择拍照或者相册返回后
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageTool?.onAcitvityResult(requestCode, resultCode, data
        ) { bitmap, path ->
            /**
             * 头像
             */
            if (isNewUpdateImage) {
                Utils.saveBitmap(path, object : OnSaveSuccessListener {
                    override fun onSuccess(path: String) {
                        if (path != null) {
                            loadingImage(path)
                        }
                    }
                })
            } else {
                v_edit_head.setIvRightIcon(path)

                val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                uploadImg(bitmap2Base64!!, FIRST_INDEX)
            }


        }
    }

    var imageTokenBean: ImageTokenBean? = null
    var isRefresh = false
    /**
     * 获取
     */
    fun getImageToken(path: String = "") {
        showLoadingDialog()
        HttpClient.instance.getImageToken("2")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ImageTokenBean>() {
                    override fun onHandleSuccess(t: ImageTokenBean?) {
                        closeLoadingDialog()
                        t ?: return
                        imageTokenBean = t
                        if (isRefresh) {
                            isRefresh = false
                            loadingImage(path)
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        closeLoadingDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }

                })
    }


    /**
     * 获取token的图片加载
     */
    fun loadingImage(path: String) {
        showLoadingDialog()
        var uploadHelper = UploadHelper.uploadImage(path, imageTokenBean?.AccessKeyId, imageTokenBean?.AccessKeySecret,
                imageTokenBean?.bucketName, imageTokenBean?.ossUrl, imageTokenBean?.SecurityToken, imageTokenBean?.catalog)
        closeLoadingDialog()
        if (TextUtils.isEmpty(uploadHelper)) {
            isRefresh = true
            getImageToken(path)
            return
        }


        closeLoadingDialog()
        headImUrl = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean?.catalog
                ?: ""))
        v_edit_head.setIvRightIcon(path)
    }

}

