package com.yjkj.chainup.contract.utils

import android.text.TextUtils
import com.common.sdk.utlis.MathHelper
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.extra.Contract.ContractCalculate
import com.yjkj.chainup.manager.RateManager.Companion.getRatesByCoinName

object ContractUtils {
    /**
     * 计算合约总资产
     * @param coinType 当前资产展示单位 如：BTC USDT ETH等
     * @return
     */
    fun calculateTotalBalance(coinType: String?): Double {
        val contractAccount = ContractUserDataAgent.getContractAccounts() ?: return 0.0
        var total_balance = 0.0
        for (i in contractAccount.indices) {
            val account = contractAccount[i]

            if (account == null || ContractPublicDataAgent.isVirtualCoin(account.coin_code)) {
                continue
            }
            val freeze_vol = MathHelper.round(account.freeze_vol)
            val available_vol = MathHelper.round(account.available_vol)
            var longProfitAmount = 0.0 //多仓位的未实现盈亏
            var shortProfitAmount = 0.0 //空仓位的未实现盈亏
            var position_margin = 0.0
            val contractPositions = ContractUserDataAgent.getCoinPositions(account.coin_code)
            if (contractPositions != null && contractPositions.size > 0) {
                for (j in contractPositions.indices) {
                    val contractPosition = contractPositions[j]
                    val positionContract = ContractPublicDataAgent.getContract(contractPosition.instrument_id)
                    val contractTicker = ContractPublicDataAgent.getContractTicker(contractPosition.instrument_id)
                    if (positionContract == null || contractTicker == null) {
                        continue
                    }
                    position_margin += MathHelper.round(contractPosition.im)
                    if (contractPosition.side == 1) { //开多

                        longProfitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                                contractPosition.cur_qty,
                                contractPosition.avg_cost_px,
                                contractTicker.fair_px,
                                positionContract.face_value,
                                positionContract.isReserve)
                    } else if (contractPosition.side == 2) { //开空
                        shortProfitAmount += ContractCalculate.CalculateCloseShortProfitAmount(
                                contractPosition.cur_qty,
                                contractPosition.avg_cost_px,
                                contractTicker.fair_px,
                                positionContract.face_value,
                                positionContract.isReserve)
                    }
                }
            }
            val balance = MathHelper.add(freeze_vol, available_vol)
            val total = balance + position_margin + longProfitAmount + shortProfitAmount
            total_balance += if (TextUtils.equals(coinType, account.coin_code)) {
                total
            } else {
                val btcRate = java.lang.Double.valueOf(getRatesByCoinName(coinType))
                val coinRate = java.lang.Double.valueOf(getRatesByCoinName(account.coin_code))
                MathHelper.div(MathHelper.mul(total, coinRate), btcRate)
            }
        }
        return total_balance
    }



}