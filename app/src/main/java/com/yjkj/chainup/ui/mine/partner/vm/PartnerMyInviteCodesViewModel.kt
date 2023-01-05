package com.yjkj.chainup.ui.mine.partner.vm


import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.common.sdk.LibCore
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.mine.invite.EditInviteCodesDialog
import com.yjkj.chainup.ui.mine.partner.PartnerEditInviteCodesDialog
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class PartnerMyInviteCodesViewModel : BaseViewModel() {

    var context = MutableLiveData<FragmentActivity>()

    var isShowDialog = MutableLiveData<AgentCodeBean>(null)



    interface OnItemListener {
        fun onClick(item: AgentCodeBean,view: View)
        fun onEditClick(item: AgentCodeBean)
        fun onDefault(item: AgentCodeBean)
    }

    var dialog: TDialog? = null
    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: AgentCodeBean,view: View) {
            val activity: FragmentActivity= context.value!!
            val list: ArrayList<String> = arrayListOf()
            val url = UserDataService.getInstance()?.inviteUrl?.split(UserDataService.getInstance()?.inviteCode!!)?.get(0) + item.inviteCode
            list.add(url)
            list.add(url)
            dialog = NewDialogUtils.showInvitationPosters(activity, list, object : NewDialogUtils.DialogSharePostersListener {
                override fun saveIamgePosters(imageUrl: String, shareView: View, type: Int) {
                    createShareView(activity,shareView, type)
                    dialog?.dismiss()
                }

                override fun saveIamgePostersNew(imageUrl: String) {

                }
            })


        }

        override fun onEditClick(item: AgentCodeBean) {
            isShowDialog.value=item

        }

        override fun onDefault(item: AgentCodeBean) {
            if (item.isDefault=="1"){
                return
            }
            val map = HashMap<String, Any>()
            map["inviteCode"] = item.inviteCode
            map["rate"] = item.rate
            map["remark"] = item.remark
            map["isDefault"] = "1"
            startTask(apiService.updateDefaultCode(map), Consumer {
                myInviteCodes()
            })
        }
    }

    private fun createShareView(context: FragmentActivity,shareView: View, type: Int) {
        if (type == 1) {
            ShareToolUtil.sendLocalShare(context, createViewBitmap(shareView))
        } else {
            ShareToolUtil.saveImageToGallery(context, createViewBitmap(shareView))
        }
        ToastUtils.showToast(LibCore.context.getString(R.string.share_text43))
    }

    private fun createViewBitmap(v: View): Bitmap? {
        val bitmap: Bitmap = Bitmap.createBitmap(
            v.width, v.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
    }
    fun onclickRightIcon() {
        PartnerEditInviteCodesDialog().apply {
            val bundle = Bundle()
            bundle.putInt("type", 1)
            this.arguments = bundle

        }.showDialog(context.value!!.supportFragmentManager,"")

    }

    val itemBinding =
        ItemBinding.of<AgentCodeBean>(BR.item, R.layout.item_invite_code_partner).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<AgentCodeBean> = ObservableArrayList()

    var onRefreshCommand = BindingCommand<Any>(BindingAction {
        myInviteCodes()
    })


    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer {
            items.clear()
            for (i in 0 until it.data.size) {
                val bean = it.data[i]
                bean.rateInt = it.data[i].rate.toDouble().toInt()
                bean.inviteUrl=
                    UserDataService.getInstance()?.inviteUrl?.split(UserDataService.getInstance()?.inviteCode!!)?.get(0) ?:""
                items.add(bean )
            }


        })

    }

}