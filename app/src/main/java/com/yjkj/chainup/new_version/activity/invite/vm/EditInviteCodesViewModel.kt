package com.yjkj.chainup.new_version.activity.invite.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import io.reactivex.functions.Consumer


class EditInviteCodesViewModel : BaseViewModel() {

    var type = MutableLiveData(1)

    var checkNum = MutableLiveData(1)

    var remark = MutableLiveData("")

    var rate = MutableLiveData(0)

    var isCheck = MutableLiveData(false)


    fun checkNum(type: Int) {
        checkNum.value = type
        when (type) {
            1 -> rate.value = 0
            2 -> rate.value = 30
            3 -> rate.value = 50
            4 -> rate.value = 70
            5 -> rate.value = 90
        }

    }

     fun initData(bean:AgentCodeBean?){
         remark.value=bean?.remark
         rate.value = bean?.rateInt
         when (bean?.rateInt) {
             0 -> checkNum.value = 1
             30 -> checkNum.value = 2
             50 -> checkNum.value = 3
             70 -> checkNum.value = 4
             90 -> checkNum.value = 5
         }
         isCheck.value=bean?.isDefault=="1"

     }



    fun onSure() {
        val map = HashMap<String, Any>()
        map["rate"] = rate.value.toString()
        map["remark"] = remark.value.toString()
        map["isDefault"] = if (isCheck.value == true) "1" else "0"
        startTask(apiService.createInviteCode(map), Consumer{
            finish()
        }, Consumer {

        });

    }
}