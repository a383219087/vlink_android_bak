package com.yjkj.chainup.ui.home.vm


import android.content.Context
import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.chainup.contract.utils.CpClLogicContractSetting.getThemeMode
import com.common.sdk.LibCore.context
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.NLanguageUtil
import com.yjkj.chainup.util.ToastUtils
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
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    ToastUtils.showToast("正在开发")
                }
                /**
                 * 提现转账
                 */
                1 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    ToastUtils.showToast("正在开发")
                }
                /**
                 * 合约
                 */
                2 -> EventBusUtil.post(MessageEvent(MessageEvent.contract_switch_type))
                /**
                 * 期权
                 */
                3 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    val token = UserDataService.getInstance().token
                    val lang = NLanguageUtil.getLanguage()
                    val style = if (getThemeMode(context) == 0) "white" else "black"
                    val url = "${ChainUpApp.url?.optionUrl}?token=${token}&lang=${lang}&style=${style}"
                    val bundle = Bundle()
                    bundle.putString(ParamConstant.URL_4_SERVICE, url)
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
                 * 猜区块
                 */
                6 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    val token = UserDataService.getInstance().token
                    val lang = NLanguageUtil.getLanguage()
                    val style = if (getThemeMode(context) == 0) "white" else "black"
                    val url = "${ChainUpApp.url?.blocksUrl}?token=${token}&lang=${lang}&style=${style}"
                    val bundle = Bundle()
                    bundle.putString(ParamConstant.URL_4_SERVICE, url)
                    ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)

                }
                /**
                 * 密聊
                 */
                7 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    val bundle = Bundle()
                    bundle.putString(ParamConstant.URL_4_SERVICE,ChainUpApp.url?.chatUrl)
                    ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
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
                 * 疯狂竞猜
                 */
                9 -> {
                    if (!LoginManager.checkLogin(mActivity.value, true)) {
                        return
                    }
                    val token = UserDataService.getInstance().token
                    val lang = NLanguageUtil.getLanguage()
                    val style = if (getThemeMode(context) == 0) "white" else "black"
                    val url = "${ChainUpApp.url?.crazyUrl}?token=${token}&lang=${lang}&style=${style}"
                    val bundle = Bundle()
                    bundle.putString(ParamConstant.URL_4_SERVICE, url)
                    ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
                }


            }


        }
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
                item.resImg.value = R.mipmap.qiquan
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
            /**
             * 猜区块
             */
            if (blocks == 1) {
                val item = Item()
                item.index.value = 6
                item.resImg.value = R.mipmap.youxi
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text7")
                items.add(item)
            }
            /**
             * 密聊
             */
            if (chat == 1) {
                val item = Item()
                item.index.value = 7
                item.resImg.value = R.mipmap.miliao
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text8")
                items.add(item)
            }
            /**
             * 分享有礼
             */
            if (share == 1) {
                val item = Item()
                item.index.value = 8
                item.resImg.value = R.mipmap.fenxiangyouli
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text9")
                items.add(item)
            }
            /**
             * 疯狂
             */
            if (crazy == 1) {
                val item = Item()
                item.index.value = 9
                item.resImg.value = R.mipmap.youxi
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text14")
                items.add(item)
            }
        })

    }

    /**
     * 快捷买币
     */
    fun quickBuy(){
        if (!LoginManager.checkLogin(mActivity.value, true)) {
            return
        }
        ArouterUtil.navigation(RoutePath.QuickBuyActivity, null)
    }

    /**
     * p2p买币
     */
    fun p2pBuy(){
        ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
    }

}
