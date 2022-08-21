package com.chainup.contract.app;


import com.chainup.contract.R;
import com.chainup.contract.utils.CpContextUtil;

/**
 * @Description:
 * @Author: wanghao
 * @CreateDate: 2019-08-26 19:47
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-08-26 19:47
 * @UpdateRemark: 更新说明
 */
public class CpAppConfig {

    public static final int cacheSize = 10 * 1024 * 1024;
    public static final long read_time = 15 * 1000;
    public static final long write_time = 15 * 1000;
    public static final long connect_time = 15 * 1000;

    public static final String app_name = CpContextUtil.getString(R.string.app_name);
    public static String app_ver = "1.0.0";
    public static String down_cl = "guanfang";


    public static final boolean IS_DEBUG = true;//Log日志开关，true为打开日志,上线需要关闭改为false


    public static final String default_host = "https://www.baidu.com/";
}
