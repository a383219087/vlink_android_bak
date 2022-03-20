/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.yjkj.chainup.new_version.activity.asset;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * use https://github.com/bumptech/glide as media loader.
 * can <b>not</b> be used in Production Environment.
 *
 * @author ChenSL
 */
public class BoxingGlideLoader implements IBoxingMediaLoader {

    @Override
    public void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height) {
        String path = "file://" + absPath;
        try {
            // https://github.com/bumptech/glide/issues/1531
            RequestOptions options = new RequestOptions()
                    .centerCrop()
//                    .placeholder(com.chad.library.R.drawable.ic_boxing_default_image)
                    .error(android.R.drawable.stat_notify_error)
                    .priority(Priority.HIGH)
                    //.skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop().override(width, height);

            Glide.with(img.getContext()).load(path).apply(options).into(img);
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Override
    public void displayRaw(@NonNull final ImageView img, @NonNull String absPath, int width, int height, final IBoxingCallback callback) {
        String path = "file://" + absPath;

        Glide.with(img.getContext())
                .asBitmap().
                load(path)
                .apply(new RequestOptions().override(width, height))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        // resource is your loaded Bitmap
                        return true;
                    }
                }).into(img);
    }

}
