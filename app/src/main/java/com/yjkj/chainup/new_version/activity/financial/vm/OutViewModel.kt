package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.bean.ProjectInfo
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer
import java.util.*

class OutViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    var bean = MutableLiveData<Pos>()


    var text = MutableLiveData<String>()


    fun allOnClick() {
        text.value = bean.value?.userCurrentAmount.toString()

    }


    fun save() {
        if (text.value.isNullOrEmpty()) {
            ToastUtils.showToast("请输入金额")
            return
        }
        val map = TreeMap<String, String>()
        map["amount"] = text.value.toString()
        map["projectId"] = bean.value?.projectId.toString()
        startTask(apiService.redeem(toRequestBody(DataHandler.encryptParams(map))), Consumer {
          ToastUtils.showToast("申请成功")


        }, Consumer {
            ToastUtils.showToast("申请失败")
        })

    }

}