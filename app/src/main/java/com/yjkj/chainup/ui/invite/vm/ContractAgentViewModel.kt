package com.yjkj.chainup.ui.invite.vm


import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class ContractAgentViewModel : BaseViewModel() {


    val codeList: ObservableList<AgentCodeBean> = ObservableArrayList()
    var rate = MutableLiveData(0)
    var code = MutableLiveData("")
    var url = MutableLiveData( UserDataService.getInstance()?.inviteUrl)
    var bean = MutableLiveData<AgentCodeBean>()
    var myBonusBean = MutableLiveData<InviteBean>()
    var isShowDialog = MutableLiveData(0)
    var activity = MutableLiveData<FragmentActivity>()

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
        })

    }

    fun myBonus() {
        startTask(apiService.myBonus(), Consumer {
            myBonusBean.value = it.data
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
//        InvitationPostersDialog().apply {
//            val bundle = Bundle()
//            bundle.putString("code",bean.value!!.inviteCode)
//            this.arguments = bundle
//        }.showDialog(activity.value?.supportFragmentManager,"")

    }
}