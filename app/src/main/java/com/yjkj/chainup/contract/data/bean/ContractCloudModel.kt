package com.yjkj.chainup.contract.data.bean

import com.alibaba.fastjson.JSON

/**
 * @author ZhongWei
 * @time 2020/8/18 17:59
 * @description
 **/
class ContractCloudModel {

    var code: String? = null

    var msg: String? = null

    var message: String? = null

    var data: String? = null

    /**
     * 获取data中数据
     */
    fun getDataKeyValue(key: String): String? {
        if (data == null) {
            return null
        }
        return try {
            JSON.parseObject(data).getString(key)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}