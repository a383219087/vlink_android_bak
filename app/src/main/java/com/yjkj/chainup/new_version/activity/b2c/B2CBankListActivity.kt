package com.yjkj.chainup.new_version.activity.b2c

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.B2CBankListAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_bank_list.*
import org.json.JSONObject

/**
 * @description:银行账户列表(B2C)
 * @author Bertking
 * @date 2019-10-24 AM
 *
 *
 *
 */
@Route(path = RoutePath.B2CBankListActivity)
class B2CBankListActivity : NBaseActivity() {
    val adapter = B2CBankListAdapter()

    @JvmField
    @Autowired(name = ParamConstant.BANK_ID)
    var bankNo = ""


    override fun setContentView() = R.layout.activity_bank_list
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        fiatAllBank()
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM

        rv_banks?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_banks?.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.isNotEmpty()) {
                val bank = (adapter.data[position] as JSONObject).toString()
                intent?.putExtra(ParamConstant.BANK_JSON, bank)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        fiatAllBank()
    }

    /**
     * 银行列表
     */
    private fun fiatAllBank() {
        addDisposable(getMainModel().fiatAllBank(symbol = PublicInfoDataService.getInstance().coinInfo4B2c,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONArray("data").run {
                            if (this?.length() != 0) {
                                this?.run {
                                    Log.d(TAG, "========bankID:$bankNo=====")
                                    adapter.setSelectedID(bankNo)
                                    val list = JSONUtil.arrayToList(this)
                                    adapter.setList(list)
                                }
                            }

                        }
                    }
                }))
    }
}
