package com.yjkj.chainup.ui.invite.vm


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.BitmapUtils
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.ToastUtils


class InvitationPostersViewModel : BaseViewModel() {


    var url = MutableLiveData("")

    var check = MutableLiveData("1")

    var userAccount = MutableLiveData(UserDataService.getInstance().userAccount)

    var navigationContent = MutableLiveData("")

    var activity = MutableLiveData<Context>()
    var bitmap = MutableLiveData<Bitmap>()




    @SuppressLint("CheckResult")
    fun saveBitmap(view: View){
//        val view: ImageView = view as ImageView
        val bitmap = bitmap.value
//        val rxPermissions = RxPermissions(view.context)
//        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            .subscribe { granted ->
//                if (granted) {
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
//                } else {
//                    ToastUtils.showToast("保存失败")
//                }
//            }
    }



    fun setShare(type: Int) {
        check.value = type.toString()

    }

    fun getData(context: Context) {
        navigationContent.value = String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name"))
        bitmap.value = BitmapUtils.generateBitmap(url.value, 300, 300)

    }


}