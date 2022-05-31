package com.yjkj.chainup.ui.financial.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer
import java.util.*

class OutViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    var bean = MutableLiveData<Pos>()


    var text = MutableLiveData<String>()


    fun allOnClick() {
        text.value = DecimalUtil.cutValueByPrecision(bean.value?.userCurrentAmount.toString(),2)

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


        })

    }

}