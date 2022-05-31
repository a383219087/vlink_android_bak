package com.yjkj.chainup.ui.invite.vm


import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.ui.invite.EditInviteCodesDialog
import com.yjkj.chainup.ui.invite.InvitationPostersDialog
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyInviteCodesViewModel : BaseViewModel() {

    var context = MutableLiveData<FragmentActivity>()

    var isShowDialog = MutableLiveData<AgentCodeBean>(null)


    interface OnItemListener {
        fun onClick(item: AgentCodeBean,view: View)
        fun onEditClick(item: AgentCodeBean)
        fun onDefault(item: AgentCodeBean)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: AgentCodeBean,view: View) {
            val activity: FragmentActivity= context.value!!

            InvitationPostersDialog().apply {
                val bundle = Bundle()
                bundle.putString("code",item.inviteCode)
                this.arguments = bundle
            }.showDialog(activity.supportFragmentManager,"")

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
    fun onclickRightIcon() {
        EditInviteCodesDialog().apply {
            val bundle = Bundle()
            bundle.putInt("type", 1)
            this.arguments = bundle

        }.showDialog(context.value!!.supportFragmentManager,"")

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


        })

    }

}