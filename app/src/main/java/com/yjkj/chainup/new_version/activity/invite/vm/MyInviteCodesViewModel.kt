package com.yjkj.chainup.new_version.activity.invite.vm


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.BR
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.ToastUtils

import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyInviteCodesViewModel : BaseViewModel() {

    var context = MutableLiveData<Context>()

    interface OnItemListener {
        fun onClick(item: AgentCodeBean,view: View)
        fun onEditClick(item: AgentCodeBean)
        fun onDefault(item: AgentCodeBean)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: AgentCodeBean,view: View) {
            val list: ArrayList<String> = arrayListOf(item.inviteCode,item.inviteCode)
            var dialog = NewDialogUtils.showInvitationPosters(view.context as Activity, list, object : NewDialogUtils.DialogSharePostersListener {
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

        override fun onEditClick(item: AgentCodeBean) {
            ARouter.getInstance().build(RoutePath.EditInviteCodesActivity)
                .withInt("type", 2)
                .withSerializable("bean", item)
                .navigation()
        }

        override fun onDefault(item: AgentCodeBean) {
            if (item.isDefault=="1"){
                return
            }
            val map = HashMap<String, Any>()
            map["inviteCode"] = item.inviteCode
            startTask(apiService.updateDefaultCode(map), Consumer {
                myInviteCodes()
            }, Consumer {

            })
        }


    }


    val itemBinding =
        ItemBinding.of<AgentCodeBean>(BR.item, R.layout.item_invite_code).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<AgentCodeBean> = ObservableArrayList()

    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer {
            items.clear()
            for (i in 0 until it.data.size) {
                val bean = it.data[i]
                bean.rateInt = it.data[i].rate.toDouble().toInt()
                items.add(bean)
            }


        }, Consumer {

        });

    }

}