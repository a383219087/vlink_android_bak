package com.yjkj.chainup.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.NetworkLineErrorService
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class SystemV2Utils {
    companion object {
        fun getProcessName(context: Context?): String? {
            if (context == null) return null
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfo = manager.runningAppProcesses
            processInfo.forEach {
                if (it.pid == android.os.Process.myPid()) {
                    return it.processName
                }
            }
            return null
        }

        /**  * 保存二维码到本地相册   */
        fun saveImageToPhotos(context: Context, bmp: Bitmap, mHandler: Handler) {
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
                mHandler.obtainMessage(1).sendToTarget()
                return
            }

            // 最后通知图库更新
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(file)
            intent.data = uri
            context.sendBroadcast(intent)
            mHandler.obtainMessage(0).sendToTarget()
        }

        fun getFontFamily(): Typeface {
            return Typeface.createFromAsset(ChainUpApp.app.assets,
                    "fonts/dinpro_medium.otf")
        }

        /**
         * 旁门正道标题题
         */
        fun getFontFamily2(): Typeface {
            return Typeface.createFromAsset(ChainUpApp.app.assets,
                "fonts/pmzdbtt.ttf")
        }

        /**
         * 旁门正道标题题
         */
        fun getFontFamily3(): Typeface {
            return Typeface.createFromAsset(ChainUpApp.app.assets,
                "fonts/BlackItalic.otf")
        }
    }
}

fun changeNetworkError() {
    if (!UserDataService.getInstance().isNetworkCheckIng) {
        UserDataService.getInstance().isNetworkCheckIng = true
        val context = ChainUpApp.app
        val intent = Intent(context, NetworkLineErrorService::class.java)
        context.startService(intent)
    }
}