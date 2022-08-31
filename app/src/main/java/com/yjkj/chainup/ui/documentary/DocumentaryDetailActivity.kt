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

    @Autowired(name = "id")
    @JvmField
    var id : String?=null
    @Autowired(name = "contractId")
    @JvmField
    var contractId : Int=0



    override fun setContentView() = R.layout.activity_documentary_detail
    override fun initData() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.id?.value=id
        mViewModel?.contractId?.value=contractId
        mViewModel?.getData()


    }


}