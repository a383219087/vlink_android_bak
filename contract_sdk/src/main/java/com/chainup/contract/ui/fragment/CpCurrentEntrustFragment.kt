package com.chainup.contract.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import kotlinx.android.synthetic.main.fragment_cp_current_entrust.*
import java.nio.Buffer

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CpCurrentEntrustFragment : CpNBaseFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mDataList: ArrayList<String>
    private lateinit var mBufferAdapter: BufferAdapter


    override fun initView() {
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mDataList= ArrayList()
        mDataList.add("")
        mDataList.add("")
        mDataList.add("")
        mBufferAdapter= BufferAdapter(R.layout.cp_item_position,mDataList)
        rv_order.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mBufferAdapter
        }
    }

    override fun setContentView(): Int {
      return R.layout.fragment_cp_current_entrust
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CpCurrentEntrustFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


    class BufferAdapter(layoutResId: Int, data: MutableList<String>) :
            BaseQuickAdapter<String, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: String) {

        }
    }
}