package com.yjkj.chainup.new_version.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.GlideUtils
import com.yjkj.chainup.util.ToastUtils
import kotlinx.android.synthetic.main.activity_show_imagectivity.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


/**
 * @date 2018-10-22
 * @author Bertking
 * @description 显示图片页面
 */
class ShowImageActivity : NewBaseActivity() {
    var imageUrl = ""

    var isQRCode = false


    private val SAVE_SUCCESS = 0//保存图片成功
    private val SAVE_FAILURE = 1//保存图片失败
    private val SAVE_BEGIN = 2//开始保存图片
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SAVE_BEGIN -> {
                    btn_save.isClickable = false
                }
                SAVE_SUCCESS -> {
                    ToastUtils.showToast(LanguageUtil.getString(this@ShowImageActivity,"common_tip_saveImgSuccess"))
                    btn_save.isClickable = true
                }
                SAVE_FAILURE -> {
                    ToastUtils.showToast(LanguageUtil.getString(this@ShowImageActivity,"common_tip_saveImgFail"))
                    btn_save.isClickable = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_imagectivity)
        context = this
        imageUrl = intent?.getStringExtra(QRCODE_URL)!!


        Log.d(TAG, "image_URL:" + imageUrl)

        isQRCode = intent?.getBooleanExtra(IS_QRCODE, false)!!


        GlideUtils.loadImage(context, imageUrl, iv_scale)


        iv_scale.setOnClickListener {
            finish()
        }

        if (isQRCode) {
            btn_save.visibility = View.VISIBLE
        } else {
            btn_save.visibility = View.GONE
        }

        /***
         * 保存图片
         */
        btn_save.setOnClickListener {
            val rxPermissions: RxPermissions = RxPermissions(context as ShowImageActivity)
            /**
             * 获取读写权限
             */
            rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { granted ->
                        if (granted) {

                            btn_save.isClickable = false//不可重复点击
                            //保存图片必须在子线程中操作，是耗时操作
                            Thread(Runnable {
                                mHandler.obtainMessage(SAVE_BEGIN).sendToTarget()
                                val bp = returnBitMap(imageUrl)!!
                                saveImageToPhotos(context, bp)
                            }).start()


//                            val bitmap =  returnBitMap(imageUrl)
//
//                            if (bitmap != null) {
//                                val saveImageToGallery = ImageTools.saveImageToGallery(context, bitmap)
//                                if (saveImageToGallery) {
//                                    ToastUtils.showToast(getString(R.string.save_success))
//                                } else {
//                                    ToastUtils.showToast(getString(R.string.save_fail))
//                                }
//                            } else {
//                                ToastUtils.showToast(getString(R.string.save_fail))
//
//                            }
                        } else {
                            ToastUtils.showToast(LanguageUtil.getString(this,"common_tip_saveImgFail"))
                        }

                    }
        }
        iv_cancel.setOnClickListener { finish() }
    }

    companion object {
        /**
         * 付款图片RUL
         */
        val QRCODE_URL = "qrcode_url"

        val IS_QRCODE = "isqrcode"


        fun enter2(context: Context, url: String, isQRCode: Boolean = true) {
            var intent = Intent(context, ShowImageActivity::class.java)
            intent.putExtra(QRCODE_URL, url)
            intent.putExtra(IS_QRCODE, isQRCode)
            context.startActivity(intent)
        }
    }


    fun returnBitMap(url: String): Bitmap? {
        val myFileUrl: URL
        var bitmap: Bitmap? = null
        try {
            myFileUrl = URL(url)
            val conn: HttpURLConnection
            conn = myFileUrl.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connect()
            val `is` = conn.inputStream
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }


    /**  * 保存二维码到本地相册   */
    private fun saveImageToPhotos(context: Context, bmp: Bitmap) {
        // 首先保存图片
        val appDir = File(Environment.getExternalStorageDirectory(), "Boohee")
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.contentResolver,
                    file.absolutePath, fileName, null)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            mHandler.obtainMessage(SAVE_FAILURE).sendToTarget()
            return
        }

        // 最后通知图库更新
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val uri = Uri.fromFile(file)
        intent.data = uri
        context.sendBroadcast(intent)
        mHandler.obtainMessage(SAVE_SUCCESS).sendToTarget()
    }

}
