package com.yjkj.chainup.ui.documentary

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryDetailBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.documentary.vm.DocumentaryDetailViewModel


@Route(path = RoutePath.DocumentaryDetailActivity)
class DocumentaryDetailActivity : BaseMVActivity<DocumentaryDetailViewModel?, ActivityDocumentaryDetailBinding?>(){
    @Autowired(name = "bean")
    @JvmField
    var item : CpContractPositionBean?=null
    @Autowired(name = "type")
    @JvmField
    var type : Int?=null
    @Autowired(name = "status")
    @JvmField
    var status : Int?=null

    override fun setContentView() = R.layout.activity_documentary_detail
    override fun initData() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.bean?.value=item
        mViewModel?.type?.value=type
        mViewModel?.status?.value=status
        mViewModel?.getData()

    }




}