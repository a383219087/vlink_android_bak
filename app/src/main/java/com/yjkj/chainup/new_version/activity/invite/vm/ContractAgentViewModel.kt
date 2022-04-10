package com.yjkj.chainup.new_version.activity.invite.vm


import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.ToastUtils

import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class ContractAgentViewModel : BaseViewModel() {


    val codeList: ObservableList<AgentCodeBean> = ObservableArrayList()
    var rate = MutableLiveData(0)
    var code = MutableLiveData("")
    var bean = MutableLiveData<AgentCodeBean>()
    var myBonusBean = MutableLiveData<InviteBean>()
    var isShowDialog = MutableLiveData(0)

    val itemBinding = ItemBinding.of<InviteBean>(BR.item, R.layout.item_invite_rank)
    val items: ObservableList<InviteBean> = ObservableArrayList()


    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer {
            codeList.clear()
            codeList.addAll(it.data)
            bean.value = it.data.first { it.isDefault == "1" }
            bean.value!!.rateInt = bean.value!!.rate.toDouble().toInt()
            rate.value = bean.value!!.rate.toDouble().toInt()
            code.value = bean.value!!.inviteCode
        }, Consumer {

        })

    }

    fun myBonus() {
        startTask(apiService.myBonus(), Consumer {
            myBonusBean.value = it.data
        }, Consumer {

        })

    }

    fun top10() {
        startTask(apiService.top10(), Consumer {
            items.clear()
            for (i in 0 until it.data.size) {
                val bean = it.data[i]
                bean.index = i
                items.add(bean)
            }

        }, Consumer {

        })

    }


    fun toMyInviteCodesActivity() {
        ArouterUtil.navigation(RoutePath.MyInviteCodesActivity, null)
    }

    fun toMoreNextInvite() {
        ArouterUtil.navigation(RoutePath.MyInviteActivity, null)
    }

    fun onEditClick(view: View) {
        isShowDialog.value = isShowDialog.value!! + 1
    }


    fun onShareClick(view: View) {
        val list: ArrayList<String> = arrayListOf(bean.value!!.inviteCode, bean.value!!.inviteCode)
        var dialog = NewDialogUtils.showInvitationPosters(
            view.context as Activity,
            list,
            object : NewDialogUtils.DialogSharePostersListener {
                @SuppressLint("CheckResult")
                override fun saveIamgePosters(imageUrl: String, shareView: ImageView) {
                    var bitmap = (shareView.drawable as BitmapDrawable).bitmap
                    val rxPermissions = RxPermissions(view.context as Activity)
                    /**
                     * 获取读写权限
                     */
                    rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

                }

                override fun saveIamgePostersNew(imageUrl: String) {

                }
            })

    }
}