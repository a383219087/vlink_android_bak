package com.yjkj.chainup.new_version.activity.personalCenter

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.request.RequestOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.InvitationImgBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invit_firends.*

/**
 * @Author lianshangljl
 * @Date 2019/6/18-9:33 PM
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.InvitFirendsActivity)
class InvitFirendsActivity : NewBaseActivity() {


    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invit_firends)
        title_layout?.slidingShowTitle(false)
        title_layout?.setContentTitle(LanguageUtil.getString(this,"common_action_inviteFriend"))
        tv_invitecode_title?.text = LanguageUtil.getString(this,"register_text_inviteCode")
        tv_common_action_copy?.text = LanguageUtil.getString(this,"common_action_copy")
        cbtn_save_image?.setBottomTextContent(LanguageUtil.getString(this,"common_action_savePoster"))
        cbtn_save_url?.setBottomTextContent(LanguageUtil.getString(this,"common_action_copyLink"))
        getinvitationImgs()
        initView()
        setonClick()
    }

    fun setTopLogo() {
        var logoBean = PublicInfoDataService.getInstance().getApp_logo_list_new(null)
        if (null == logoBean || logoBean.isEmpty()) return
        if (!TextUtils.isEmpty(logoBean[0])) {
            GlideUtils.loadImageHeader(this, logoBean[0], iv_logo ?: return)
        }
    }

    fun initView() {

        tv_invitecode_content?.text = UserDataService.getInstance()?.inviteCode
        setTopLogo()
        /**
         * 二维码图片
         */
        if (!TextUtils.isEmpty(UserDataService.getInstance()?.inviteUrl)) {
            iv_invit_url?.setImageBitmap(BitmapUtils.generateBitmap(UserDataService.getInstance()?.inviteUrl, 300, 300))
        }


        cbtn_save_url?.isEnable(true)
        cbtn_save_image?.isEnable(true)

    }

    fun setonClick() {
        /**
         * 复制邀请码
         */
        ll_copy_layout.setOnClickListener {
            Utils.copyString(UserDataService.getInstance()?.inviteCode)
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this,"common_tip_copySuccess"), true)
            return@setOnClickListener
        }

        /**
         * 复制链接
         */
        cbtn_save_url?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                Utils.copyString(UserDataService.getInstance()?.inviteUrl)
                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@InvitFirendsActivity,"common_tip_copySuccess"), true)
            }
        }

        /**
         * 保存海报
         */
        cbtn_save_image?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                val rxPermissions: RxPermissions = RxPermissions(this@InvitFirendsActivity)
                /**
                 * 获取读写权限
                 */
                rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe { granted ->
                            if (granted) {
                                bitmap = ScreenShotUtil.getScreenshotBitmap(rl_save_layout
                                        ?: return@subscribe)
                                if (bitmap != null) {
                                    val saveImageToGallery = ImageTools.saveImageToGallery(this@InvitFirendsActivity, bitmap)
                                    if (saveImageToGallery) {
                                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@InvitFirendsActivity,"common_tip_saveImgSuccess"), true)
                                    } else {
                                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@InvitFirendsActivity,"common_tip_saveImgFail"), false)
                                    }
                                } else {
                                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@InvitFirendsActivity,"common_tip_saveImgFail"), false)
                                }
                            } else {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@InvitFirendsActivity,"common_tip_saveImgFail"), false)
                            }

                        }
            }

        }

    }

    fun getinvitationImgs() {
        HttpClient.instance.getInvitationImg()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<InvitationImgBean>() {
                    override fun onHandleSuccess(t: InvitationImgBean?) {

                        if (SystemUtils.isZh()) {
                            if (t?.online_img_cn?.isNotEmpty() == true) {
                                iv_logo?.visibility = View.GONE
                                tv_rv_contract?.visibility = View.GONE
                            }
                            var options = RequestOptions().placeholder(R.drawable.online_chinese)
                                    .error(R.drawable.online_chinese)
                            GlideUtils.load(this@InvitFirendsActivity, t?.online_img_cn, iv_posters_layout
                                    ?: return, options)
                        } else {
                            if (t?.online_img_en?.isNotEmpty() == true) {
                                iv_logo?.visibility = View.GONE
                                tv_rv_contract?.visibility = View.GONE
                            }
                            var options = RequestOptions().placeholder(R.drawable.online_english)
                                    .error(R.drawable.online_english)
                            GlideUtils.load(this@InvitFirendsActivity, t?.online_img_en, iv_posters_layout
                                    ?: return, options)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        if (SystemUtils.isZh()) {
                            var options = RequestOptions().placeholder(R.drawable.online_chinese)
                                    .error(R.drawable.online_chinese)
                            GlideUtils.load(this@InvitFirendsActivity, "", iv_posters_layout
                                    ?: return, options)
                        } else {
                            var options = RequestOptions().placeholder(R.drawable.online_english)
                                    .error(R.drawable.online_english)
                            GlideUtils.load(this@InvitFirendsActivity, "", iv_posters_layout
                                    ?: return, options)
                        }
                    }
                })
    }


}