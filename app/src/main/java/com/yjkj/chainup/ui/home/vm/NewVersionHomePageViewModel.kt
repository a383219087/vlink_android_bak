package com.yjkj.chainup.ui.home.vm


import android.content.Context
import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.utils.CpClLogicContractSetting.getThemeMode
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.NLanguageUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.util.*


class NewVersionHomePageViewModel : HomePageViewModel() {



    /**
     * 顶部广告图列表
     */
    var bannerImgUrls: ObservableList<String> = ObservableArrayList()

    /**
     * 列表
     */
    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: Item) {

            when (item.index.value) {
                /**
                 * 充值收款
                 */
                0 -> {
                    homeRecharge()
                }
                /**
                 * 提现转账
                 */
                1 -> {
                    withdrawalTransfer()
                }
                /**
                 * 合约
                 */
                2 -> contractClick()
                /**
                 * 期权
                 */
                3 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    var clean = false
                    if (SPUtils.getInstance().getString("optionUrl", "") != item.version.value) {
                        clean = true
                        SPUtils.getInstance().put("optionUrl", item.version.value)
                    }
                    val token = UserDataService.getInstance().token
                    val lang = NLanguageUtil.getLanguage()
                    val style = if (getThemeMode(mActivity.value) == 0) "white" else "black"
                    val url = "${ChainUpApp.url?.optionUrl}?token=${token}&lang=${lang}&type=${style}"
                    val bundle = Bundle()
                    bundle.putString(ParamConstant.URL_4_SERVICE, url)
                    bundle.putBoolean(ParamConstant.DEFAULT_NAME_ERROR, clean)
                    ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)

                }
                /**
                 * 跟单
                 */
                4 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    ArouterUtil.navigation(RoutePath.DocumentaryActivity, null)
                }
                /**
                 * 理财
                 */
                5 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    ArouterUtil.navigation(RoutePath.FinancialActivity, null)
                }

                /**
                 * 密聊
                 */
                7 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    startTask(apiService.level(), {
                        val bundle = Bundle()
                        bundle.putString(ParamConstant.URL_4_SERVICE, ChainUpApp.url?.chatUrl)
                        bundle.putString(ParamConstant.homeTabType, it.data)
                        ArouterUtil.greenChannel(RoutePath.ChatWebViewActivity, bundle)
                    }, {
                        val bundle = Bundle()
                        bundle.putString(ParamConstant.URL_4_SERVICE, ChainUpApp.url?.chatUrl)
                        bundle.putString(ParamConstant.homeTabType, "0")
                        ArouterUtil.greenChannel(RoutePath.ChatWebViewActivity, bundle)
                    }
                    )

                }
                /**
                 * 分享有礼
                 */
                8 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
                }
                /**
                 * 客服
                 */
                9 -> {
                    customerService()
                }


            }


        }
    }


    fun phoneCertification(type: Int = 2): Boolean {
        if (PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)) {
            if (UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingMustPermissionsDialog(mActivity.value!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }
                }, type = type, title = LanguageUtil.getString(mActivity.value, "withdraw_tip_bindGoogleFirst"))
                return true
            }
        } else {
            if (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingPermissionsDialog(mActivity.value!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证
                            //.enter2(context!!)
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }
                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }

                }, type = 2, title = LanguageUtil.getString(mActivity.value, "otcSafeAlert_action_bindphoneOrGoogle"))
                return true
            }
        }

        return false
    }
    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_new_version_item).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()

    interface OnItemListener {
        fun onClick(item: Item)

    }

    class Item {
        var index = MutableLiveData(0)
        var resImg = MutableLiveData(0)
        var title = MutableLiveData("")
        var version = MutableLiveData("")

    }


    /**
     * 获取首页配置
     */

//    "withdraw":1,
//    "futures":1,
//    "options":1,
//    "trader":1,
//    "increment":1,
//    "game":1,
//    "share":1
    fun getPublicInfo(context: Context) {
        val map = TreeMap<String, String>()
        startTask(apiService.getPublicInfo1(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            if (it.data == null || it.data?.enable_module_info == null) {
                return@Consumer
            }
            val contract = it.data?.enable_module_info?.futures
            val trader = it.data?.enable_module_info?.trader
            val increment = it.data?.enable_module_info?.increment
            val blocks = it.data?.enable_module_info?.blocks
            val crazy = it.data?.enable_module_info?.crazy
            val share = it.data?.enable_module_info?.share
            val withdraw = it.data?.enable_module_info?.withdraw
            val futures = it.data?.enable_module_info?.options
            val recharge = it.data?.enable_module_info?.recharge
            val chat = it.data?.enable_module_info?.chat
            items.clear()
            /**
             * 充值收款
             */
            if (recharge == 1) {
                val item = Item()
                item.index.value = 0
                item.resImg.value = R.mipmap.chongzhi
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text1")
                items.add(item)
            }
            /**
             * 提现转账
             */
            if (withdraw == 1) {
                val item = Item()
                item.index.value = 1
                item.resImg.value = R.mipmap.tixian_zhuanzhang
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text2")
                items.add(item)
            }
            /**
             * 合约
             */
            if (contract == 1) {
                val item = Item()
                item.index.value = 2
                item.resImg.value = R.mipmap.heyue
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text3")
                items.add(item)
            }
            /**
             * 期权
             */
            if (futures == 1) {
                val item = Item()
                item.index.value = 3
                item.resImg.value = R.mipmap.youxi
                item.version.value = it.data?.enable_module_info?.options_version
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text4")
                items.add(item)
            }
            /**
             * 跟单
             */
            if (trader == 1) {
                val item = Item()
                item.index.value = 4
                item.resImg.value = R.mipmap.gendan
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text5")
                items.add(item)
            }
            /**
             * 理财
             */
            if (increment == 1) {
                val item = Item()
                item.index.value = 5
                item.resImg.value = R.mipmap.licai
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text6")
                items.add(item)
            }

//            /**
//             * 密聊
//             */
//            if (chat == 1&&false) {
//                val item = Item()
//                item.index.value = 7
//                item.resImg.value = R.mipmap.miliao
//                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text8")
//                items.add(item)
//            }
//            /**
//             * 分享有礼
//             */
//            if (share == 1) {
//                val item = Item()
//                item.index.value = 8
//                item.resImg.value = R.mipmap.fenxiangyouli
//                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text9")
//                items.add(item)
//            }
            /**
             * 客服
             */
                val item = Item()
                item.index.value = 9
                item.resImg.value = R.mipmap.kefu
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text114")
                items.add(item)

        })

    }

    /**
     * 快捷买币
     */
    fun quickBuy() {
        if (!LoginManager.checkLogin(mActivity.value, true)) {
            return
        }
        ArouterUtil.navigation(RoutePath.QuickBuyActivity, null)
    }

    /**
     * p2p买币
     */
    fun p2pBuy() {
        ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
    }

    /**
     * 代码抽出   与RecyclerView item点击公用
     */
    //充值
    fun homeRecharge(){
        if (!LoginManager.checkLogin(mActivity.value, true)) {
            return
        }
        if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
            NewDialogUtils.KycSecurityDialog(mActivity.value!!, mActivity.value!!.getString(R.string.common_kyc_chargeAndwithdraw), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            NToastUtil.showTopToastNet(mActivity.value, false, mActivity.value!!.getString(R.string.noun_login_pending))
                        }

                        2, 3 -> {
                            ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }
            })
            return
        }
        ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
            putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
            putBoolean(ParamConstant.COIN_FROM, true)
        })
    }

    //提现/转账
    fun withdrawalTransfer(){
        if (!LoginManager.checkLogin(mActivity.value, true)) {
            return
        }
        if (phoneCertification()) return
        if (PublicInfoDataService.getInstance().withdrawKycOpen && UserDataService.getInstance().authLevel != 1) {
            NewDialogUtils.KycSecurityDialog(mActivity.value!!, mActivity.value?.getString(R.string.common_kyc_chargeAndwithdraw)
                ?: "", object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            NToastUtil.showTopToastNet(mActivity.value, false, mActivity.value?.getString(R.string.noun_login_pending))
                        }

                        2, 3 -> {
                            ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }
            })
            return
        }

        ArouterUtil.navigation(RoutePath.WithdrawSelectCoinActivity, Bundle().apply {
            putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
            putBoolean(ParamConstant.COIN_FROM, true)
        })
    }

    //合约
    fun contractClick(){
        EventBusUtil.post(MessageEvent(MessageEvent.contract_switch_type))
    }

    //客服
    fun customerService(){
        if (!LoginManager.checkLogin(mActivity.value, true)) {
            return
        }
        val lang = NLanguageUtil.getLanguage()
        val style = if (getThemeMode(mActivity.value) == 0) "white" else "black"
        val url = "http://47.250.37.185/index/index/home?theme=7571f9&visiter_id=${UserDataService.getInstance().userInfo4UserId}" +
                "&visiter_name=${UserDataService.getInstance().nickName}&avatar=&business_id=1&groupid=0" +
                "&style=${style}&lan =${lang}"

        val bundle = Bundle()
        bundle.putString(ParamConstant.URL_4_SERVICE, url)
        bundle.putBoolean(ParamConstant.DEFAULT_NAME_ERROR, false)
        ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
    }

}
