package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.yjkj.chainup.util.JsonUtils
import com.github.promeg.pinyinhelper.Pinyin
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.CountryInfo
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.adapter.AreaAdapter
import com.yjkj.chainup.wedegit.SectionDecoration
import kotlinx.android.synthetic.main.activity_select_area.*
import org.greenrobot.eventbus.EventBus
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class SelectAreaActivity : NewBaseActivity() {
    companion object {
        val CHOOSE_COUNTRY_CODE = 111
        val COUNTRYCODE = "countryCode"
        val AREA = "area"
    }


    val mList: ArrayList<CountryInfo> = ArrayList()
    lateinit var v_search: MaterialSearchView
    lateinit var ly_search: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_area)

        tv_cancel.setOnClickListener { finish() }
        getAreaData()
        et_search?.hint = LanguageUtil.getString(this,"filter_fold_country")
        tv_cancel?.text = LanguageUtil.getString(this,"common_text_btnCancel")

    }

    private fun getAreaData() {
        val stream: InputStream = assets.open("area.json")
        val size = stream.available()
        val byteArray = ByteArray(size)
        stream.read(byteArray)
        stream.close()
        val json: String = String(byteArray, Charset.defaultCharset())
        val jsonObject = JsonParser().parse(json).asJsonObject
        handleData(jsonObject)
        Log.d(TAG, "------" + json)


    }


    private fun handleData(data: JsonObject) {
        area_side_bar.visibility = View.VISIBLE
        var allCountry = arrayListOf<CountryInfo>()
        var limtCountry = PublicInfoDataService.getInstance().getLimitCountryList(null)
        if (data.get("countryList").isJsonArray) {
            val countryData = JsonUtils.jsonToList(data.get("countryList").toString(), CountryInfo::class.java)
            Log.d(TAG, "-----正常的----" + countryData.size)
            countryData.forEach {
                if (!TextUtils.isEmpty(it.dialingCode)) {
                    allCountry.add(it)
                }
            }
            for (bean in limtCountry) {
                for (country in allCountry) {
                    if (country.numberCode == bean) {
                        allCountry.remove(country)
                        break
                    }
                }
            }
            mList.clear()
            mList.addAll(allCountry)


            if (Locale.getDefault().language.contentEquals("zh")) {
                val sortedByPinyinList = mList.sortedBy { Pinyin.toPinyin(it.cnName, "") }
                sortedByPinyinList.forEach(::println)
                initAdapter(ArrayList(sortedByPinyinList))
            } else {
                initAdapter(mList)
            }

        }

    }


    var countryList = arrayListOf<CountryInfo>()
    var countryListNormal = arrayListOf<CountryInfo>()

    private fun initAdapter(mList: ArrayList<CountryInfo>) {
        countryList.clear()
        countryListNormal.clear()
        countryList.addAll(mList)
        countryListNormal.addAll(mList)
        area_side_bar.visibility = View.VISIBLE
        val areaAdapter = AreaAdapter(countryList, this)
        rv_area?.layoutManager = LinearLayoutManager(this)
        rv_area?.adapter = areaAdapter


        area_side_bar?.setOnTouchingLetterChangedListener { s ->
            for (i in 0 until countryList.size) {
                //对比字符串，忽略大小写
                if (countryList[i].getStickItem().equals(s, true)) {
                    rv_area.scrollToPosition(i)
                    break
                }
            }
        }


        /**
         * 给 RecyclerView添加 ItemDecoration
         */
        rv_area?.addItemDecoration(SectionDecoration(this, object : SectionDecoration.DecorationCallback {
            override fun getGroupFirstLine(position: Int): String {
                Log.d(TAG, "---位置---" + position)
                Log.d(TAG, "-------" + countryList[position].getStickItem())
                return countryList[position].getStickItem().substring(0, 1).toLowerCase()
            }

            override fun getGroupId(position: Int): Long {
                Log.d(TAG, "----pos--" + countryList[position].getStickItem()[0]).toLong()
                return Character.toUpperCase(countryList[position].getStickItem()[0]).toLong()

            }

        }))

        /**
         * Adapter的点击事件
         */
        areaAdapter.setOnItemClickListener { adapter, view, position ->
            intent.putExtra(COUNTRYCODE, countryList[position].dialingCode)
            if (Locale.getDefault().language.contentEquals("zh")) {
                intent.putExtra(AREA, countryList[position].cnName)
            } else {
                intent.putExtra(AREA, countryList[position].enName)
            }

            setResult(Activity.RESULT_OK, intent)
            EventBus.getDefault().post(countryList[position])
            finish()
        }
        areaAdapter.listener = object : AreaAdapter.FilterListener {
            override fun getFilterData(list: java.util.ArrayList<CountryInfo>) {
                countryList.clear()
                countryList.addAll(list)
                areaAdapter.setList(countryList)
            }

        }

        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                areaAdapter.filter.filter(s)
            }

        })
    }


}


