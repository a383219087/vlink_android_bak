package com.yjkj.chainup.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yjkj.chainup.R;
import com.yjkj.chainup.db.service.CheckUpdateDataService;
import com.yjkj.chainup.manager.LanguageUtil;
import com.yjkj.chainup.model.model.MainModel;
import com.yjkj.chainup.net_new.NetUrl;
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver;
import com.yjkj.chainup.new_version.home.GuideKt;
import com.yjkj.chainup.update.ApkDownloadListener;
import com.yjkj.chainup.update.ApkDownloadUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;

import cn.ljuns.logcollector.util.FileUtils;
import io.reactivex.functions.Consumer;

public class CheckUpdateUtil {

    private static final String TAG = "CheckUpdateUtil";

    /**
     * 检查升级
     * AppUpdateBean(build = 0, downloadUrl = null, force = 0, title = null, version = null, content = null)
     * <p>
     * force : 0 - 不强制 ; 1 - 强制升级
     *
     * @param isAutoUpdate true为自动升级，false为手动点击检查更新
     */
    public static void update(final Activity activity, final boolean isAutoUpdate) {
        Activity loadingActivity = activity;
        if (isAutoUpdate) {
            loadingActivity = null;
        }
        LogUtil.d(TAG, "CheckUpdateUtil==");

        new MainModel().getAppVersion(new NDisposableObserver(loadingActivity, false) {
            @Override
            public void onResponseSuccess(@NotNull JSONObject jsonObject) {
                LogUtil.d(TAG, "CheckUpdateUtil==onResponseSuccess==jsonObject is " + jsonObject);

                String code = jsonObject.optString("code");
                if (!"0".equals(code)) {
                    String msg = jsonObject.optString("msg");
                    if (!isAutoUpdate) {
                        NToastUtil.showTopToastNet(activity, true, msg);
                    }
                    return;
                }

                JSONObject data = jsonObject.optJSONObject("data");
                if (null != data) {
                    int build = data.optInt("build");

                    int localVersionCode = UpdateHelper.getLocalVersion(activity);
                    if (localVersionCode < build) {
                        boolean isForce = (1 == data.optInt("force"));
                        boolean hideDialog = CheckUpdateDataService.getInstance().isHideUpdate(build);
                        if (hideDialog && !isForce && isAutoUpdate) {
                            return;
                        }
                        showUpdateDialog(activity, data);
                    } else {
                        if (!isAutoUpdate) {
                            NToastUtil.showTopToastNet(activity, true, LanguageUtil.getString(activity, "the_latest_version"));
                        }
                    }
                }
            }

            @Override
            public void onResponseFailure(int code, @Nullable String msg) {
                super.onResponseFailure(code, msg);
                LogUtil.d(TAG, "CheckUpdateUtil==onResponseFailure==code is " + code + ",msg is " + msg);
                if (!isAutoUpdate) {
                    NToastUtil.showTopToastNet(activity, false, msg);
                }
            }
        });

    }

    /**
     * 升级的弹窗
     */
    private static Dialog showUpdateDialog(Activity activity, JSONObject data) {
        if (GuideKt.getDialogType() != 0) return null;
        boolean isForce = (1 == data.optInt("force"));
        String downloadUrl = data.optString("downloadUrl");
        String android_apk_url = data.optString("android_apk_url");
        String title = data.optString("title");
        int version =  data.optInt("build");
        String content = data.optString("content");

        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_update, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_content = view.findViewById(R.id.tv_content);
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView btn_confirm = view.findViewById(R.id.btn_confirm);
        LinearLayout ll_download_progress = view.findViewById(R.id.ll_download_progress);
        RelativeLayout rl_update_ctrl = view.findViewById(R.id.rl_update_ctrl);
        ProgressBar pb_progress = view.findViewById(R.id.pb_progress);
        TextView tv_progress = view.findViewById(R.id.tv_progress);

        tv_title.setText("" + title);
        tv_content.setText("" + content);

        btn_cancel.setVisibility(isForce ? View.GONE : View.VISIBLE);

        GuideKt.setDialogType(1);
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GuideKt.setDialogType(0);
                CheckUpdateDataService.getInstance().saveUpdateData(CheckUpdateDataService.hideDialog,version);
                dialog.dismiss();
            }
        });
        String storePath = FileUtils.getCacheFileDir(activity, "apks");
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        RxPermissions mRxPermissions = new RxPermissions(activity);
        RxView.clicks(btn_confirm)
                .compose(mRxPermissions.ensureEach(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(
                        new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) throws Exception {
                                if (permission.granted) {
                                    ll_download_progress.setVisibility(View.VISIBLE);
                                    rl_update_ctrl.setVisibility(View.GONE);
                                    LogUtil.e("下载地址：", android_apk_url);
                                    new ApkDownloadUtils(NetUrl.baseUrl(), new ApkDownloadListener() {
                                        @Override
                                        public void onStartDownload() {
                                            LogUtil.e("下载开始", "");
                                        }

                                        @Override
                                        public void onProgress(int progress) {
                                            LogUtil.e("下载进度：", progress + "%");
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pb_progress.setProgress(progress);
                                                    tv_progress.setText(progress + "%");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFinishDownload() {
                                            LogUtil.e("下载完成", "");
                                            dialog.dismiss();
                                            GuideKt.setDialogType(0);
                                            CheckUpdateDataService.getInstance().clearUpdateData();
                                        }

                                        @Override
                                        public void onFail(String errorInfo) {
                                            LogUtil.e("下载失败：", errorInfo);
                                            NToastUtil.showTopToastNet(activity, false, errorInfo);
                                            dialog.dismiss();
                                        }
                                    }).download(android_apk_url, storePath + File.separator + "newVersion.apk", activity);

                                } else {
                                    NToastUtil.showTopToastNet(activity, true, LanguageUtil.getString(activity, "warn_storage_permission"));
                                }
                            }
                        });


//        btn_confirm.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (!isForce) {
//                    dialog.dismiss();
//                }
//                CheckUpdateDataService.getInstance().saveData(0);
//                IntentUtil.forwardBrowse(activity, downloadUrl);
//
//
////               new RxPermissions(activity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
////                        .subscribe { granted ->
////                    if (granted) {
////                        rl_share_layout.isDrawingCacheEnabled = true
////                        rl_share_layout.buildDrawingCache()
////                        val bitmap: Bitmap = Bitmap.createBitmap(rl_share_layout.drawingCache)
////                        if (bitmap != null) {
////                            ShareToolUtil.sendLocalShare(mActivity, bitmap)
////                        } else {
////                            DisplayUtil.showSnackBar(window?.decorView, getString(R.string.warn_storage_permission), false)
////                        }
////                    } else {
////                        DisplayUtil.showSnackBar(window?.decorView, getString(R.string.warn_storage_permission), false)
////                    }
////
////                }
//            }
//        });
        dialog.setView(view);
        dialog.setCancelable(false);
        if (!activity.isFinishing()) {
            dialog.show();
        }
        return dialog;
    }

}