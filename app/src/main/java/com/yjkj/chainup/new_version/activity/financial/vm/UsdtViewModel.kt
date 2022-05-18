package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.bean.ProjectInfo
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net.DataHandler
import io.reactivex.functions.Consumer
import java.util.*

class UsdtViewModel: BaseViewModel() {
    var bean = MutableLiveData<ProjectInfo>()
    var bean1 = MutableLiveData<ProjectBean>()






    fun getData(id:String) {
        val map = TreeMap<String, String>()
        map["id"] = id
        startTask(apiService.projectInfo(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            bean.value=it.data


        })
    }


    fun toSaveActivity(){
        ARouter.getInstance().build(RoutePath.SaveActivity)
            .withString("id",bean1.value?.id.toString())
            .navigation()

    }
}