package com.yjkj.chainup.net_new;

import android.text.TextUtils;
import android.util.Log;

import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.app.GlobalAppComponent;
import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.net.api.ApiConstants;
import com.yjkj.chainup.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @Description:
 * @Author: wanghao
 * @CreateDate: 2019-10-14 11:44
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-10-14 11:44
 * @UpdateRemark: 更新说明
 */
public class NetUrl {

    public static final String baseUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.baseUrl),true);
    }

    public static final String getotcBaseUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.otcBaseUrl),true);
    }

    public static final String getsocketAddress() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.socketAddress),false);
    }

    public static final String getotcSocketAddress() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.otcSocketAddress),false);
    }

    public static final String getcontractUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.contractUrl),true);
    }

    public static final String getContractSocketUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.contractSocketAddress),false);
    }

    public static final String getredPackageUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.redPackageUrl),true);
    }

    public static final String biki_monitor_appUrl = "https://security.biki.com/security-microspot/apis/v1/monitor/app";

    public static final String getContractNewUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.httpHostUrlContractV2),true);
    }

    public static final String getContractSocketNewUrl() {
        return Utils.returnAPIUrl(GlobalAppComponent.getContext().getString(R.string.wssHostContractV2),false);
    }


}
