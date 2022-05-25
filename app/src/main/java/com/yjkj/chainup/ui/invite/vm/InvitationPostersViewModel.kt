package com.yjkj.chainup.ui.invite.vm


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.BitmapUtils
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.ToastUtils


class InvitationPostersViewModel : BaseViewModel() {


    var url = MutableLiveData("")

    var check = MutableLiveData("1")

    var userAccount = MutableLiveData(UserDataService.getInstance().userAccount)

    var navigationContent = MutableLiveData("")


    var bitmap = MutableLiveData<Bitmap>()


    @SuppressLint("CheckResult")
    var listener = BindingCommand(BindingConsumer<View> {
        val view: ImageView = it as ImageView
        val bitmap = (view.drawable as BitmapDrawable).bitmap
        val rxPermissions = RxPermissions(view.context as Activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted ->
                if (granted) {
                    if (bitmap != null) {
                        val saveImageToGallery = ImageTools.saveImageToGallery4ContractAgent(view.context, bitmap)
                        if (saveImageToGallery) {
                            ToastUtils.showToast("保存成功")
                        } else {
                            ToastUtils.showToast("保存失败")
                        }
                    } else {
                        ToastUtils.showToast("保存失败")
                    }
                } else {
                    ToastUtils.showToast("保存失败")
                }
            }

    })


    fun setShare(type: Int) {
        check.value = type.toString()

    }

    fun getData(context: Context) {
        navigationContent.value = String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name"))
        bitmap.value = BitmapUtils.generateBitmap(url.value, 300, 300)

    }


}