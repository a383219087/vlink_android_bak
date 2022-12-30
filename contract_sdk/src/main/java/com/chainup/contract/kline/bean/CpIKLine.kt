package  com.chainup.contract.kline.bean


import com.yjkj.chainup.new_version.kline.bean.vice.CpIKDJ
import com.yjkj.chainup.new_version.kline.bean.vice.CpIMACD
import com.yjkj.chainup.new_version.kline.bean.vice.CpIRSI
import com.yjkj.chainup.new_version.kline.bean.vice.CpIWR


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:55 AM
 * @Description: K线实体
 */
interface CpIKLine : CpCandleBean, CpVolumeBean, CpIKDJ, CpIMACD, CpIRSI, CpIWR