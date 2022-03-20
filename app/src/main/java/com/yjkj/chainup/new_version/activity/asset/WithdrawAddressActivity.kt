package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.address.AddressBean
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.adapter.WithdrawAddressAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.DisplayUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_withdraw_address.*


/**
 * @description 提现地址列表
 */
class WithdrawAddressActivity : NewBaseActivity() {
    var symbol: String = ""
    var showSymbol: String = ""
    var selectAddress: String = ""

    var list = arrayListOf<AddressBean.Address>()
    lateinit var adapter: WithdrawAddressAdapter


    companion object {
        const val REQUEST_CODE_ADDRESS = 2088

        const val OBJECT_ADDRESS = "address"

        const val SELECT_ADDRESS = "select_address"

        const val SYMBOL = "Symbol"
        const val SHOWSYMBOL = "showSymbol"

        fun enter4Result(activity: Activity, symbol: String?, showSymbol: String = "", selectAddress: String = "") {
            val intent = Intent(activity, WithdrawAddressActivity::class.java)
            intent.putExtra(SYMBOL, symbol)
            intent.putExtra(SHOWSYMBOL, showSymbol)
            intent.putExtra(SELECT_ADDRESS, selectAddress)
            activity.startActivityForResult(intent, REQUEST_CODE_ADDRESS)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_address)
        title_layout?.setContentTitle(showSymbol + LanguageUtil.getString(this, "common_text_address"))
        context = this
        symbol = intent.getStringExtra(SYMBOL) ?: ""
        showSymbol = intent.getStringExtra(SHOWSYMBOL) ?: ""
        selectAddress = intent.getStringExtra(SELECT_ADDRESS) ?: ""
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        if (UserDataService.getInstance().isLogined) {
            getAddressList(symbol)
        }

        initViews()
    }


    private fun initViews() {
        adapter = WithdrawAddressAdapter(list)
        adapter?.setAddress(selectAddress)
        rv_address?.layoutManager = LinearLayoutManager(this)
        adapter.setEmptyView(EmptyForAdapterView(context ?: return))
        rv_address?.adapter = adapter

        /**
         * 删除地址
         */
        adapter.setOnItemLongClickListener { adapter, view, position ->


            NewDialogUtils.showNormalDialog(this@WithdrawAddressActivity, LanguageUtil.getString(this, "common_text_confirmDelete"), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    /**
                     * 若无Google或者Mobile，则不验证...
                     */
                    HttpClient.instance.delWithdrawAddress(list[position].id.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NetObserver<Any>() {
                                override fun onHandleSuccess(t: Any?) {
                                    list.removeAt(position)
                                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@WithdrawAddressActivity, "address_tip_deleteSuccess"), isSuc = true)
                                    adapter.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@WithdrawAddressActivity, "address_tip_deleteSuccess"), isSuc = true)
                                    Log.d(TAG, "=====删除地址：=suc=" + t.toString())

                                }

                                override fun onHandleError(code: Int, msg: String?) {
                                    super.onHandleError(code, msg)
                                    Log.d(TAG, "=====删除地址：=failed=" + msg)
                                    DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)


                                }

                            })
                }

            }, "", LanguageUtil.getString(this, "common_text_btnConfirm"), LanguageUtil.getString(this, "common_text_btnCancel"))


            true
        }

        /**
         * 点击地址
         */
        adapter.setOnItemClickListener { adapter, view, position ->
            val intent = Intent()
            intent.putExtra(OBJECT_ADDRESS, list[position])
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    var onclickItem = false

    private fun initClickListener() {
        /**
         * 添加地址
         */
        cbt_add_address?.isEnable(true)
        cbt_add_address.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                NewVersionAddAddressActivity.enter2(this@WithdrawAddressActivity, symbol, showSymbol)
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onclickItem) {
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        return super.onKeyDown(keyCode, event)
    }


    /**
     * 获取地址列表
     * @showSymbol 为空的情况下返回所有数据
     */
    private fun getAddressList(symbol: String = "") {
        HttpClient.instance.getAddressList(symbol)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AddressBean>() {
                    override fun onHandleSuccess(t: AddressBean?) {

                        if (null != t?.addressList) {
                            list = ArrayList(t?.addressList)
                        }
                        adapter.setList(list)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }

    var tDialog: TDialog? = null

    /**
     * 显示二次验证的Dialog
     *
     * 12-修改数字货币地址
     * @param 地址id
     * @param 删除的位置
     *
     */
    fun showVerifyDialog(id: String, position: Int) {

        tDialog = NewDialogUtils.showSecondDialog(this@WithdrawAddressActivity, AppConstant.CHANGE_WITHDRAW_ADDRESS, object : NewDialogUtils.DialogSecondListener {
            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                HttpClient.instance.delWithdrawAddress(id, smsCode = phone!!, googleCode = googleCode!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NetObserver<Any>() {
                            override fun onHandleSuccess(t: Any?) {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@WithdrawAddressActivity, "address_tip_deleteSuccess"), isSuc = true)
                                Log.d(TAG, "=====删除地址：==" + t.toString())
                                adapter.remove(position)
                                adapter.notifyItemRemoved(position)
                                tDialog?.dismiss()
                                onclickItem = true
                            }

                            override fun onHandleError(code: Int, msg: String?) {
                                super.onHandleError(code, msg)
                                DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                                tDialog?.dismiss()

                                Log.d(TAG, "=====删除地址：==" + msg)
                            }

                        })
            }
        }, loginPwdShow = false)
    }

}
