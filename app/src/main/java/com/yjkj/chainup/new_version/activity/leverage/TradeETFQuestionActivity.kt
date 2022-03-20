package com.yjkj.chainup.new_version.activity.leverage

import android.os.Bundle
import android.text.SpannableString
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.trade.ETFQuestAdapter
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.addETFSenSpanBean
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_levert_list.*
import org.json.JSONObject

/**
 * 借贷记录详情
 */
@Route(path = RoutePath.TradeETFQuestionActivity)
class TradeETFQuestionActivity : NBaseActivity() {
    val list = arrayListOf<String>()
    val questions = arrayListOf<Map<String, Pair<Int, Collection<String>>>>()
    val mAdapter = ETFQuestAdapter(list)
    var position = 0
    override fun setContentView() = R.layout.activity_levert_list

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initView()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun initView() {
        title_layout?.setContentTitle(LanguageUtil.getString(this, "etf_question_title"))
        title_layout?.slidingShowTitle(false)
        rv_detail?.layoutManager = LinearLayoutManager(mActivity)
        mAdapter.setEmptyView(EmptyForAdapterView(this))
        rv_detail?.adapter = mAdapter
        val qustion_1 =  LanguageUtil.getString(this,"etf_question_anwsersa")
        val qustion_2 =  LanguageUtil.getString(this,"etf_question_anwsersb")
        val qustion_3 =  LanguageUtil.getString(this,"etf_question_anwsersc")
        val qustion_4 =  LanguageUtil.getString(this,"etf_question_anwsersd")
        val qustion_5 =  LanguageUtil.getString(this,"etf_question_anwserse")

        val qustion1 = mapOf("etf_question_a" to Pair(0, qustion_1.split("//").toList()))
        val qustion2 = mapOf("etf_question_b" to Pair(1, qustion_2.split("//").toList()))
        val qustion3 = mapOf("etf_question_c" to Pair(1, qustion_3.split("//").toList()))
        val qustion4 = mapOf("etf_question_d" to Pair(0, qustion_4.split("//").toList()))
        val qustion5 = mapOf("etf_question_e" to Pair(2, qustion_5.split("//").toList()))

        questions.add(qustion1)
        questions.add(qustion2)
        questions.add(qustion3)
        questions.add(qustion4)
        questions.add(qustion5)
        changeQustion(position)
        btn_confirm_next?.isEnable(false)
        btn_confirm_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (position < questions.size - 1) {
                    position++
                    changeQustion(position)
                } else {
                    readTradeETF()
                }
            }
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            mAdapter.select = position
            mAdapter.notifyDataSetChanged()
            btn_confirm_next?.isEnable(position == mAdapter.success)
        }
    }

    private fun changeQustion(index: Int) {
        val item = questions[index]
        if (index == questions.size - 1) {
            btn_confirm_next.setContent(LanguageUtil.getString(this@TradeETFQuestionActivity, "common_start_trade"))
        }
        for ((key, value) in item) {
            val comment = "[${index + 1}/${questions.size}]"
            val text = comment + " " + LanguageUtil.getString(this, key)
            val span = SpannableString(text)
            span.addETFSenSpanBean(text, comment, R.color.normal_text_color)
            tv_title_comment?.text = span
            mAdapter.setList(value.second)
            mAdapter.success = value.first
            mAdapter.select = -1
        }
    }

    fun readTradeETF() {
        addDisposable(MainModel().saveETFStatus(consumer = object : NDisposableObserver(mActivity, true) {
            override fun onResponseSuccess(data: JSONObject) {
                MainModel().saveUserInfo()
                finish()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(mActivity, false, msg)
            }
        }))
    }


}

