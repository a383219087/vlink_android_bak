package com.yjkj.chainup.new_version.activity.b2c

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.B2CWithdrawAccountAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_b2_cwithdraw_account_list.*
import org.json.JSONObject

/**
 * @description:提现账户列表(B2C)
 * @author Bertking
 * @date 2019-10-23 AM
 *
 */
@Route(path = RoutePath.B2CWithdrawAccountListActivity)
class B2CWithdrawAccountListActivity : NBaseActivity() {
    val list = arrayListOf<JSONObject>()
    val adapter = B2CWithdrawAccountAdapter(list)

    var symbol: String = PublicInfoDataService.getInstance().coinInfo4B2c

    override fun setContentView() = R.layout.activity_b2_cwithdraw_account_list

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        fiatBankList()
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            expandedTitleGravity = Gravity.BOTTOM
        }

        tv_add?.setOnClickListener {
            // 添加提现账户
            ArouterUtil.navigation(RoutePath.B2CWithdrawAccountActivity, Bundle().apply { putInt(ParamConstant.TYPE, ParamConstant.TYPE_ADD) })
        }

        rv_withdraw_account_b2c?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this ))
        rv_withdraw_account_b2c?.adapter = adapter


        adapter.setOnItemChildClickListener { _, view, position ->
            if(list.isNotEmpty()){
                val id = list[position].optString("id")
                when (view.id) {
                    R.id.cl_main -> {
                        intent?.putExtra(ParamConstant.WITHDRAW_ID,id)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }

                    R.id.btn_edit -> {
                        // 编辑银行卡详情
                        ArouterUtil.navigation(RoutePath.B2CWithdrawAccountActivity, Bundle().apply {
                            putInt(ParamConstant.TYPE, ParamConstant.TYPE_EDIT)
                            putString(ParamConstant.WITHDRAW_ID, id)
                        })
                    }
                }
            }
        }


        /**
         * 长按删除
         */
        adapter.setOnItemLongClickListener { _, _, position ->
            // 删除
            val id = (adapter.data[position] as JSONObject).optString("id")
            NewDialogUtils.showNormalDialog(mActivity, "确定删除?", object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    fiatDeleteBank(id)
                }
            }, "删除")

            return@setOnItemLongClickListener true
        }
    }


    /**
     * 用户提现银行列表
     */
    private fun fiatBankList() {
        Log.d(TAG, "=====symbol:$symbol====")
        addDisposable(getMainModel().fiatBankList(symbol = symbol,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        list.clear()
                        jsonObject.optJSONObject("data")?.optJSONArray("list").run {
                            if (this?.length() != 0) {
                                this?.run {
                                    for (i in 0 until this.length()) {
                                        list.add(this.optJSONObject(i))
                                    }
                                }
                            }
                            adapter.setList(list)
                        }
                    }
                }))
    }


    /**
     * 删除提现银行
     */
    fun fiatDeleteBank(id: String) {
        addDisposable(getMainModel().fiatDeleteBank(id = id,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        fiatBankList()
                    }
                }))
    }

}
