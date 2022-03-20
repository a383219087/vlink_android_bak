package com.yjkj.chainup.db.service;

import com.yjkj.chainup.db.MMKVDb;

/**
 * @Description: 涨跌幅颜色设置
 * @Author: wanghao
 * @CreateDate: 2019-09-30 19:49
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-09-30 19:49
 * @UpdateRemark: 更新说明
 */
public class CheckUpdateDataService {

    private MMKVDb mMMKVDb;

    public static final int hideDialog = 1;
    private static final String checkupdate = "checkupdate";
    private static final String checkUpdateVersion = "checkUpdateVersion";

    private CheckUpdateDataService() {
        mMMKVDb = new MMKVDb();
    }

    private static CheckUpdateDataService mCheckUpdateDataService;

    public static CheckUpdateDataService getInstance() {
        if (null == mCheckUpdateDataService)
            mCheckUpdateDataService = new CheckUpdateDataService();
        return mCheckUpdateDataService;
    }

    public void saveData(int value) {
        mMMKVDb.saveIntData(checkupdate, value);
    }

    public boolean hideDialog() {
        return hideDialog == mMMKVDb.getIntData(checkupdate, 0);
    }

    public void saveHideVersion(int value) {
        mMMKVDb.saveIntData(checkUpdateVersion, value);
    }

    public int hideVersion() {
        return mMMKVDb.getIntData(checkUpdateVersion, -1);
    }

    /**
     * 是否忽略升级
     */
    public boolean isHideUpdate(int version) {
        boolean isHide = hideDialog();
        // 未点击过取消 提示升级
        if (!isHide) {
            return false;
        }
        int preVersion = hideVersion();
        // 旧版本没有 标记 默认提示升级
        if (preVersion == -1) {
            return false;
        }
        if (preVersion < version) {
            return false;
        }
        // 本地版本小于新版本
        return true;

    }

    public void saveUpdateData(int value, int version) {
        saveData(value);
        if (version != 0) {
            saveHideVersion(version);
        }
    }

    public void clearUpdateData() {
        mMMKVDb.removeValueForKey(checkupdate);
        mMMKVDb.removeValueForKey(checkUpdateVersion);
    }

}
