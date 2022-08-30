package com.chainup.contract.view.roundLayout.abs


interface IRoundView {
    fun setCornerRadius(cornerRadius: Float)
    fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int)
}