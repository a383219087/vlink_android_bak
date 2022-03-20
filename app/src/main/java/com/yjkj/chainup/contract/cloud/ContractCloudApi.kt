package com.yjkj.chainup.contract.cloud

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

/**
 * @author ZhongWei
 * @time 2020/8/18 17:47
 * @description
 **/
interface ContractCloudApi {

    @POST("user/create_co_account_id")
    fun createCloudChildUser(@HeaderMap headers: HashMap<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("account/trans_ex_to_cocloud")
    fun cloudChildUserTransferIn(@HeaderMap headers: HashMap<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("account/trans_cocloud_to_ex")
    fun cloudChildUserTransferOut(@HeaderMap headers: HashMap<String, String>, @Body body: RequestBody): Call<ResponseBody>

}