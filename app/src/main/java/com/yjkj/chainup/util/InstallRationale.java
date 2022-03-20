
package com.yjkj.chainup.util;


import android.content.Context;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yjkj.chainup.R;
import com.yjkj.chainup.wedegit.HiCoinAlertDialog;

import java.io.File;


public class InstallRationale implements Rationale<File> {
    @Override
    public void showRationale(Context context, File data, final RequestExecutor executor) {
      new HiCoinAlertDialog(context).setTitleText("提示")
                .showCancelButton(false)
                .setContentText("请允许【"+context.getString(R.string.app_name)+"】安装应用程序！")
                .setConfirmText("前往设置")
                .setConfirmClickListener(new HiCoinAlertDialog.OnHiCoinClickListener() {
                    @Override
                    public void onClick(HiCoinAlertDialog mHiCoinAlertDialog) {
                        mHiCoinAlertDialog.dismiss();
                        executor.execute();
                    }
                }).show();
    }
}