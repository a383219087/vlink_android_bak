package com.yjkj.chainup.new_version.home.vm


import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.EnableModuleInfo
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.util.LogUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.util.*


class NewVersionHomePageViewModel : HomePageViewModel() {

    /**
     * 顶部广告图列表
     */
    var bannerImgUrls : ObservableList<String> = ObservableArrayList()

    /**
     * 列表
     */
    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: Item) {
            if (!LoginManager.checkLogin(mActivity.value, true)) {
                return
            }
            when (item.index.value) {
                /**
                 * 期权
                 */
                0 -> ArouterUtil.navigation(RoutePath.BinaryActivity, null)
                /**
                 * 跟单
                 */
                1 -> ArouterUtil.navigation(RoutePath.DocumentaryActivity, null)
                /**
                 * 理财
                 */
                2 -> ArouterUtil.navigation(RoutePath.FinancialActivity, null)
                /**
                 * 游戏
                 */
                3 -> ArouterUtil.navigation(RoutePath.BinaryActivity, null)
                /**
                 * 分享有礼
                 */
                4 -> ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)


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
    fun getPublicInfo(context: Context) {
        val map = TreeMap<String, String>()
        startTask(contractApiService.getPublicInfo1(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            val futures = it.data.enable_module_info.futures
            val trader = it.data.enable_module_info.trader
            val increment = it.data.enable_module_info.increment
            val game = it.data.enable_module_info.game
            val share = it.data.enable_module_info.share

            items.clear()
            if (futures == 1) {
                val item = Item()
                item.index.value = 0
                item.resImg.value = R.mipmap.qiquan
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text1")
                items.add(item)
            }
            if (trader == 1) {
                val item = Item()
                item.index.value = 1
                item.resImg.value = R.mipmap.gendan
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text2")
                items.add(item)
            }
            if (increment == 1) {
                val item = Item()
                item.index.value = 2
                item.resImg.value = R.mipmap.licai
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text3")
                items.add(item)
            }
            if (game == 1) {
                val item = Item()
                item.index.value = 3
                item.resImg.value = R.mipmap.youxi
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text4")
                items.add(item)
            }
            if (share == 1) {
                val item = Item()
                item.index.value = 4
                item.resImg.value = R.mipmap.fenxiangyouli
                item.title.value = LanguageUtil.getString(context, "NewVersionHomePageViewModel_text5")
                items.add(item)
            }
        }, Consumer {
            LogUtil.d("我是", it.message)
        })

    }





}
