package com.yjkj.chainup.new_version.activity.documentary.vm


import com.yjkj.chainup.base.BaseViewModel
import io.reactivex.functions.Consumer


class ApplyTradersViewModel : BaseViewModel() {





    fun currentStatus() {
        startTask(apiService.currentStatus(), Consumer {


        }, Consumer {

        });

    }









}