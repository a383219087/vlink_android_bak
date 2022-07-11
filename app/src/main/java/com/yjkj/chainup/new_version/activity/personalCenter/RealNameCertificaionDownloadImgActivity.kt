package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.bean.AccountCertificationBean
import com.yjkj.chainup.new_version.bean.AccountCertificationLanguageBean
import com.yjkj.chainup.new_version.bean.ImageTokenBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.OnSaveSuccessListener
import com.yjkj.chainup.new_version.view.UploadHelper
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_real_name_certification_download_img.*
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream


/**
 * @Author lianshangljl
 * @Date 2019/4/24-9:49 AM
 * @Email buptjinlong@163.com
 * @description 实名制认证上传图片
 */
class RealNameCertificaionDownloadImgActivity : NewBaseActivity() {
    /**
     * 这里需要传 国家号 + 国家码
     */
    var areaInfo: String = ""
    var areaCode: String = ""

    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null

    /**
     * 证件号
     */
    var certNum = ""

    /**
     * 照片位置
     */
    var curIndex = FIRST_INDEX

    /**
     * 姓名
     */
    var realName = ""

    /**
     * 姓（非中国）
     */
    var fristName = ""

    /**
     * 名（非中国）
     */
    var surName = ""


    var firstImgPath = ""
    var secondImgPath = ""
    var thirdImgPath = ""

    companion object {
        const val IDCARD = 1 // 身份证
        const val PASSPORT = 2 // 护照
        const val ORHERID = 3 // 其他
        const val DRIVERLICENSE = 4 // 其他

        /**
         * 作为标记照片位置
         */
        const val FIRST_INDEX = 0 // 第一张照片
        const val SECOND_INDEX = 1 // 第二张照片
        const val THIRD_INDEX = 2// 第三张照片

        const val CREDENTIALS_TYPE = 1//证件
        const val PHOTO_TYPE = 2//图片选择
        const val AREA_INFO = "area_info"//国家号 + 国家码
        const val REAL_NAME = "real_name"//名字
        const val SURNAME_NAME = "surname_name"//名
        const val FRISTNAME_NAME = "fristName_name"//姓
        const val CERT_NUM = "cert_num"//身份证号
        const val CREDENTIALSTYPE = "credentials_type"//姓

        /**
         * 中国
         */
        fun enter2(context: Context, areaInfo: String, certNum: String, realName: String, credentials_type: Int, areaCode: String) {
            var intent = Intent()
            intent.setClass(context, RealNameCertificaionDownloadImgActivity::class.java)
            intent.putExtra(AREA_INFO, areaInfo)
            intent.putExtra(REAL_NAME, realName)
            intent.putExtra(SURNAME_NAME, "")
            intent.putExtra(FRISTNAME_NAME, "")
            intent.putExtra(ParamConstant.AREA_CODE, areaCode)
            intent.putExtra(CREDENTIALSTYPE, credentials_type)
            intent.putExtra(CERT_NUM, certNum)
            context.startActivity(intent)
        }

        /**
         * 非中国
         */
        fun enter2(context: Context, areaInfo: String, certNum: String, surname: String, fristName: String, credentials_type: Int, areaCode: String) {
            var intent = Intent()
            intent.setClass(context, RealNameCertificaionDownloadImgActivity::class.java)
            intent.putExtra(AREA_INFO, areaInfo)
            intent.putExtra(REAL_NAME, "")
            intent.putExtra(SURNAME_NAME, surname)
            intent.putExtra(FRISTNAME_NAME, fristName)
            intent.putExtra(ParamConstant.AREA_CODE, areaCode)
            intent.putExtra(CREDENTIALSTYPE, credentials_type)
            intent.putExtra(CERT_NUM, certNum)
            context.startActivity(intent)
        }
    }

    /**
     * 证件类型
     * 默认：身份证
     */
    var credentials_type: Int = IDCARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_name_certification_download_img)
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout.slidingShowTitle(status)
            }
        }
        val rxPermissions = RxPermissions(this)
        if (PublicInfoDataService.getInstance().isInterfaceSwitchOpen(null)) {
            AccountCertificationLanguage()
        }
//        accountCertification()
        /**
         * 获取读写权限
         */
        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({ granted ->
                    if (granted) {
                        Log.d(TAG, "---拿到权限---")
                        imageTool = ImageTools(this)
                    } else {
                    }

                })
        getData()
        if (PublicInfoDataService.getInstance().getUploadImgType(null) == "1") {
            getImageToken(operate_type = "1")
        }

        setOnclick()
        title_layout?.setContentTitle(LanguageUtil.getString(this, "otcSafeAlert_action_identify"))
        tv_common_action_uploadFrontView?.text = LanguageUtil.getString(this, "common_action_uploadFrontView")
        tv_common_action_uploadBackView?.text = LanguageUtil.getString(this, "common_action_uploadBackView")
        tv_common_action_uplodadIdInHand?.text = LanguageUtil.getString(this, "common_action_uplodadIdInHand")
        tv_common_tip_uploadImgRequire?.text = LanguageUtil.getString(this, "common_tip_uploadImgRequire")
        tv_kyc_explain_photoTip?.text = LanguageUtil.getString(this, "kyc_explain_photoTip")
        tv_4_content?.text = LanguageUtil.getString(this, "kyc_explain_lastTip")
        cub_next?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
    }


    var imageMenuDialog: TDialog? = null
    fun showBottomMenu(index: Int) {
        curIndex = index
        imageMenuDialog = NewDialogUtils.showBottomListDialog(this@RealNameCertificaionDownloadImgActivity, arrayListOf(
            LanguageUtil.getString(this, "noun_camera_takeAlbum"), LanguageUtil.getString(this, "noun_camera_takephoto")), 0
                , object : NewDialogUtils.DialogOnclickListener {
            override fun clickItem(data: ArrayList<String>, item: Int) {
                when (item) {
                    0 -> {
                        imageTool?.openGallery("")
                    }
                    1 -> {
                        openCamera()
                    }
                }
                imageMenuDialog?.dismiss()
            }

        })
    }

    fun getData() {
        if (intent != null) {
            areaInfo = intent.getStringExtra(AREA_INFO) ?: ""
            areaCode = intent.getStringExtra(ParamConstant.AREA_CODE) ?: ""
            certNum = intent.getStringExtra(CERT_NUM) ?: ""
            realName = intent.getStringExtra(REAL_NAME) ?: ""
            fristName = intent.getStringExtra(FRISTNAME_NAME) ?: ""
            surName = intent.getStringExtra(SURNAME_NAME) ?: ""
            credentials_type = intent.getIntExtra(CREDENTIALSTYPE, IDCARD)
        }
    }

    fun setOnclick() {
        cub_next?.isEnable(true)
        cub_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                setAuthRealName()
            }
        }
        iv_frist_close?.setOnClickListener {
            iv_frist_close?.visibility = View.GONE
            iv_frist?.setImageResource(R.drawable.personal_positiveupload)
            firstImgPath = ""
        }
        iv_second_close?.setOnClickListener {
            iv_second_close?.visibility = View.GONE
            iv_second?.setImageResource(R.drawable.personal_uploadreverse)
            secondImgPath = ""
        }
        iv_third_close?.setOnClickListener {
            iv_third_close?.visibility = View.GONE
            iv_third?.setImageResource(R.drawable.personal_uploadreverse)
            thirdImgPath = ""
        }

        iv_frist?.setOnClickListener {
            showBottomMenu(FIRST_INDEX)
        }
        iv_second?.setOnClickListener {
            showBottomMenu(SECOND_INDEX)
        }
        iv_third?.setOnClickListener {
            showBottomMenu(THIRD_INDEX)
        }

    }

    /**
     * 获取相机权限
     */
    private fun openCamera() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe({ granted ->
                    if (granted) {
                        Log.d(TAG, "---拿到权限---")
                        imageTool?.openCamera("")
                    } else {
                        ToastUtils.showToast(LanguageUtil.getString(this, "warn_camera_permission"))
                    }

                })
    }

    /**
     * 实名制认证
     */
    fun setAuthRealName() {
        if (firstImgPath.isEmpty() || secondImgPath.isEmpty() || thirdImgPath.isEmpty()) {
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_pleaseUpload"), isSuc = false)
            return
        }
        HttpClient.instance.authVerify(countryCode = areaInfo, certType = credentials_type, certNum = certNum,
                userName = realName, firstPhoto = firstImgPath, secondPhoto = secondImgPath,
                thirdPhoto = thirdImgPath, familyName = surName, name = fristName, numberCode = areaCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_cert_summit_suc"), isSuc = true)

                        var json = UserDataService.getInstance().userData
                        json.put("authLevel", 0)
                        UserDataService.getInstance().saveData(json)
                        ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)

                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)


                    }
                })
    }

    var imageTokenBean: ImageTokenBean = ImageTokenBean()

    fun loadingImage(path: String) {
        showProgressDialog()
        var uploadHelper = UploadHelper.uploadImage(path, imageTokenBean.AccessKeyId, imageTokenBean.AccessKeySecret, imageTokenBean.bucketName,
                imageTokenBean.ossUrl, imageTokenBean.SecurityToken, imageTokenBean.catalog)
        if (TextUtils.isEmpty(uploadHelper)) {
            isRefresh = true
            getImageToken(operate_type = "1", path = path)
            return
        } else if (uploadHelper == "error") {
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "toast_upload_pic_failed"), isSuc = false)
            return
        }


        /**
         * 身份证
         */
        when (curIndex) {
            FIRST_INDEX -> {
                var options = RequestOptions().placeholder(R.drawable.personal_positiveupload)
                        .error(R.drawable.personal_positiveupload)

                firstImgPath = path
                if (uploadHelper.indexOf(imageTokenBean.catalog) > 0 && uploadHelper.indexOf(imageTokenBean.catalog) < uploadHelper.length) {
                    firstImgPath = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))
                }

                GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_frist, options)
                iv_frist_close?.visibility = View.VISIBLE

            }
            SECOND_INDEX -> {
                var options = RequestOptions().placeholder(R.drawable.personal_uploadreverse)
                        .error(R.drawable.personal_uploadreverse)

                secondImgPath = path
                secondImgPath = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))
                GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_second, options)
                iv_second_close?.visibility = View.VISIBLE
            }
            THIRD_INDEX -> {
                var options = RequestOptions().placeholder(R.drawable.personal_handhelddocuments)
                        .error(R.drawable.personal_handhelddocuments)
                thirdImgPath = path
                thirdImgPath = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))
                GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_third, options)
                iv_third_close?.visibility = View.VISIBLE
            }
        }
        cancelProgressDialog()
    }

    /**
     * 上传照片  旧接口
     */
    fun uploadImg(imageBase: String, index: Int, path: String) {
        showProgressDialog(LanguageUtil.getString(this, "pic_uploading"))
        HttpClient.instance.uploadImg(imgBase64 = imageBase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        if (t == null) return
                        var json = JSONObject(t.toString())
                        cancelProgressDialog()

                        Log.d(TAG, "===上传图片======json:" + json.toString())
                        val baseImgUrl = json.getString("base_image_url")
                        val fileName = json.getString("filename")

                        var finalImageURL = ""

                        if (json.has("filenameStr")) {
                            val fileNameStr = json.getString("filenameStr")
                            if (TextUtils.isEmpty(fileNameStr)) {
                                if (TextUtils.isEmpty(fileName)) {
                                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_upload_pic_failed"), isSuc = false)
                                    return
                                } else {
                                    finalImageURL = fileName
                                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_upload_pic_suc"), isSuc = true)

                                }
                            } else {
                                finalImageURL = fileNameStr
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_upload_pic_suc"), isSuc = true)

                            }

                        } else {
                            if (TextUtils.isEmpty(fileName)) {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_upload_pic_failed"), isSuc = false)
                                return
                            } else {
                                finalImageURL = fileName
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@RealNameCertificaionDownloadImgActivity, "toast_upload_pic_suc"), isSuc = true)
                            }

                        }



                        when (index) {
                            FIRST_INDEX -> {
                                firstImgPath = finalImageURL
                                var options = RequestOptions().placeholder(R.drawable.personal_positiveupload)
                                        .error(R.drawable.personal_positiveupload)
                                if (TextUtils.isEmpty(firstImgPath)) {
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_frist, options)
                                } else {
                                    iv_frist_close.visibility = View.VISIBLE
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_frist, options)
                                }

                            }
                            SECOND_INDEX -> {
                                secondImgPath = finalImageURL
                                var options = RequestOptions().placeholder(R.drawable.personal_uploadreverse)
                                        .error(R.drawable.personal_uploadreverse)
                                if (TextUtils.isEmpty(secondImgPath)) {
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_second, options)
                                } else {
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_second, options)
                                    iv_second_close?.visibility = View.VISIBLE
                                }

                            }
                            THIRD_INDEX -> {
                                thirdImgPath = finalImageURL
                                var options = RequestOptions().placeholder(R.drawable.personal_handhelddocuments)
                                        .error(R.drawable.personal_handhelddocuments)
                                if (TextUtils.isEmpty(thirdImgPath)) {
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_third, options)
                                } else {
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, path, iv_third, options)
                                    iv_third_close?.visibility = View.VISIBLE
                                }

                            }
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        when (index) {
                            FIRST_INDEX -> {
                                firstImgPath = ""
                                if (TextUtils.isEmpty(firstImgPath)) {
                                    var options = RequestOptions().placeholder(R.drawable.personal_positiveupload)
                                            .error(R.drawable.personal_positiveupload)
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_frist, options)
                                }

                            }
                            SECOND_INDEX -> {
                                secondImgPath = ""

                                if (TextUtils.isEmpty(secondImgPath)) {
                                    var options = RequestOptions().placeholder(R.drawable.personal_uploadreverse)
                                            .error(R.drawable.personal_uploadreverse)
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_second, options)
                                }

                            }
                            THIRD_INDEX -> {
                                thirdImgPath = ""
                                if (TextUtils.isEmpty(thirdImgPath)) {
                                    var options = RequestOptions().placeholder(R.drawable.personal_handhelddocuments)
                                            .error(R.drawable.personal_handhelddocuments)
                                    GlideUtils.load(this@RealNameCertificaionDownloadImgActivity, "", iv_third, options)
                                }

                            }
                        }
                    }
                })
    }
    fun getFileSize(file: File): Int {
        var size = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            fis = FileInputStream(file)
            size = fis.available()
        }
        return size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imageTool?.onAcitvityResult(requestCode, resultCode, data
        ) { _, path ->
            val bitmap = QRCodeUtils.getDecodeAbleBitmap(path)
            if (PublicInfoDataService.getInstance().getUploadImgType(null) == "1") {
                Utils.saveBitmap(path, object : OnSaveSuccessListener {
                    override fun onSuccess(path: String) {
                        if (path != null) {
                            loadingImage(path)
                        }
                    }
                })
            } else {

                /**
                 * 身份证
                 */
                when (curIndex) {
                    FIRST_INDEX -> {
                        val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                        uploadImg(bitmap2Base64 ?: return@onAcitvityResult, FIRST_INDEX, path)
                    }
                    SECOND_INDEX -> {
                        val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                        uploadImg(bitmap2Base64 ?: return@onAcitvityResult, SECOND_INDEX, path)
                    }
                    THIRD_INDEX -> {

                        val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                        uploadImg(bitmap2Base64 ?: return@onAcitvityResult, THIRD_INDEX, path)
                    }
                }

            }

        }
    }

    var isRefresh = false

    /**
     * 新接口 获取token 图片
     */
    fun getImageToken(operate_type: String = "1", path: String = "") {
        showProgressDialog()
        HttpClient.instance.getImageToken(operate_type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ImageTokenBean>() {
                    override fun onHandleSuccess(t: ImageTokenBean?) {
                        cancelProgressDialog()
                        t ?: return
                        imageTokenBean = t
                        if (path.isNotEmpty()) {
                            if (isRefresh) {
                                isRefresh = false
                                loadingImage(path)
                            }
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
     * 获取实名制认证token
     */
    fun accountCertification() {
        showProgressDialog()
        HttpClient.instance.AccountCertification()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AccountCertificationBean>() {
                    override fun onHandleSuccess(t: AccountCertificationBean?) {
                        if (t?.openAuto == "0") {
                            if (t.language.isNotEmpty()) {
                                tv_4_content?.text = "4.${t?.language}"
                            }
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@RealNameCertificaionDownloadImgActivity, false, msg)
                    }
                })
    }

    /**
     * 获取实名制认证token
     *
     */
    fun AccountCertificationLanguage() {
        HttpClient.instance.AccountCertificationLanguage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AccountCertificationLanguageBean>() {
                    override fun onHandleSuccess(t: AccountCertificationLanguageBean?) {
                        t ?: return
                        if (t.language.isNotEmpty()) {
                            tv_4_content?.text = "4.${t?.language}"
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                    }
                })
    }

}