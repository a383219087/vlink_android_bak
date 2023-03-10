package com.chainup.contract.api

import android.annotation.SuppressLint
import android.util.Log
import com.chainup.contract.app.CpAppConstant
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-08-28 17:00

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-08-28 17:00

 * @UpdateRemark:   更新说明

 */
class CpNDataHandler {

    companion object {
        fun encryptParams(map: TreeMap<String, String>): Map<String, String> {

            val builder: StringBuilder = StringBuilder()
            map.forEach {
                builder.append(it.key)
                builder.append(it.value)
            }
            builder.append(CpAppConstant.SECRET)
            Log.i(" 签名之前：===", builder.toString())
            map.put("sign", string2MD5(builder.toString()))
            return map
        }

        fun encryptParamsV1(map: TreeMap<String, Any>): Map<String, Any> {

            val builder: StringBuilder = StringBuilder()
            map.forEach {
                builder.append(it.key)
                builder.append(it.value)
            }
            builder.append(CpAppConstant.SECRET)
            Log.i(" 签名之前：===", builder.toString())
            map.put("sign", string2MD5(builder.toString()))
            return map
        }

        //字符串转32位MD5
        fun string2MD5(text: String): String {
            var result = ""
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(text.toByteArray())
                val b = md.digest()
                var i: Int
                val buf = StringBuffer("")
                for (offset in b.indices) {
                    i = b[offset].toInt()
                    if (i < 0)
                        i += 256
                    if (i < 16)
                        buf.append("0")
                    buf.append(Integer.toHexString(i))
                }
                result = buf.toString()
            } catch (e: NoSuchAlgorithmException) {
                System.out.println(e)
            }
            Log.i(" 签名：===", result)
            return result
        }


        /*
  * 将时间转换为时间戳
  */
        @SuppressLint("SimpleDateFormat")
        fun dateToStamp(s: String): String {
            val res: String
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            val date = simpleDateFormat.parse(s)
            val ts = date.time
            res = ts.toString()
            return res
        }
    }


}