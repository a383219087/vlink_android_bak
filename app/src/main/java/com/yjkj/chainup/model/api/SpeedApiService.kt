package com.yjkj.chainup.model.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @Author lianshangljl
 * @Date 2020-06-18-18:02
 * @Email buptjinlong@163.com
 * @description
 */
interface SpeedApiService {
    /**
     * 获取接口是否通
     */
    @GET
    fun getHealth(@Url url: String ): Observable<HttpResultUrlData>
}

data class HttpResultUrlData(
    val baseUrl: String,
    val contractSocketAddress: String,
    val contractUrl: String,
    val httpHostUrlContractV2: String,
    val otcBaseUrl: String,
    val otcSocketAddress: String,
    val redPackageUrl: String,
    val socketAddress: String,
    val wssHostContractV2: String,
    val optionUrl: String,
    val blocksUrl: String,
    val crazyUrl: String,
    val chatUrl: String
)