package  com.chainup.contract.kline1.bean


import com.chainup.contract.kline1.bean.vice.CpIKDJ
import com.chainup.contract.kline1.bean.vice.CpIMACD
import com.chainup.contract.kline1.bean.vice.CpIRSI
import com.chainup.contract.kline1.bean.vice.CpIWR


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:55 AM
 * @Description: K线实体
 */
interface CpIKLine : CpCandleBean, CpVolumeBean, CpIKDJ, CpIMACD, CpIRSI, CpIWR