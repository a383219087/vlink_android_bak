package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.bean.BlackListData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 *Created by Bertking on 2018/10/15.
 */
open class OTCBlackListAdapter(data: ArrayList<BlackListData.Relationship>) :
        BaseQuickAdapter<BlackListData.Relationship, BaseViewHolder>(R.layout.item_black_otc, data) {


    override fun convert(helper: BaseViewHolder, item: BlackListData.Relationship) {


        helper?.setText(R.id.tv_nickname, item?.otcNickName)



    }




    /**
     * 移除好友
     */
    fun removeRelationFromBlack(userId: String, position: Int) {
        HttpClient.instance.removeRelationFromBlack(friendId = userId.toInt())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        //TODO 移除该Item
                        remove(position)
                    }

                })
    }


}