package com.yjkj.chainup.util.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.yjkj.chainup.util.HttpsUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream

@GlideModule
class ChainUpAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val okHttpClient = OkHttpClient.Builder().protocols(listOf(Protocol.HTTP_1_1))
        try {
            val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)
            if (sslParams != null) {
                okHttpClient.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            }
            val factory: OkHttpUrlLoader.Factory = OkHttpUrlLoader.Factory(okHttpClient.build())
            registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}