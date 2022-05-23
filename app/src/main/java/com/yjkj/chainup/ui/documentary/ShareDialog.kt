package com.yjkj.chainup.ui.documentary

import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogDocumentShareBinding
import com.yjkj.chainup.ui.documentary.vm.ShareViewModel


class ShareDialog : BaseDialogMVFragment<ShareViewModel?, DialogDocumentShareBinding?>() {



    override fun setContentView() = R.layout.dialog_document_share
    override fun initView() {
        mViewModel?.bean?.value= arguments?.getSerializable("bean") as CpContractPositionBean?

    }






}