package com.yjkj.chainup.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * @Author lianshangljl
 * @Date 2020/11/16-7:37 PM
 * @Email buptjinlong@163.com
 * @description
 */
class QRCodeUtils{
    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。感谢 https://github.com/devilsen 提的 PR
     *
     * @param picturePath 本地图片文件路径
     */
    companion object{
        fun getDecodeAbleBitmap(picturePath: String): Bitmap? {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(picturePath, options)
                var sampleSize = options.outHeight / 400
                if (sampleSize <= 0) {
                    sampleSize = 1
                }
                options.inSampleSize = sampleSize
                options.inJustDecodeBounds = false

                return BitmapFactory.decodeFile(picturePath, options)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }
    }

}
