package com.yjkj.chainup.ui.financial.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.common.sdk.LibCore
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.ProjectInfo
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer
import java.util.*

class SaveViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    var bean = MutableLiveData<ProjectInfo>()
    var id = MutableLiveData<String>()


    var text = MutableLiveData<String>()


    fun allOnClick() {
        text.value = bean.value?.userNormalAmount.toString()

    }

    fun getData(id:String) {
        val map = TreeMap<String, String>()
        map["id"] = id
        startTask(apiService.projectInfo(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            bean.value=it.data


        })
    }

    fun save() {
        if (text.value.isNullOrEmpty()) {
            ToastUtils.showToast(LibCore.context.getString(R.string.financial_text31))
            return
        }
        val map = TreeMap<String, String>()
        map["amount"] = text.value.toString()
        map["projectId"] = id.value.toString()
        startTask(apiService.apply(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            ToastUtils.showToast(LibCore.context.getString(R.string.financial_text32))


        })

    }

}