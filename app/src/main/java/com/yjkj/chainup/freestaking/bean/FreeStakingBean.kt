package com.yjkj.chainup.freestaking.bean

class FreeStakingBean {
    /**
     * banner : http://chaindown-oss.oss-cn-hongkong.aliyuncs.com/upload/20190923113240890.png
     * url : http://www.biki.com
     * tipMine : 我的Pos记录
     * typeConfig : [{"langType":"zh_CN","typeName":"协议Pos","typeSn":"1569209560881_1"},{"langType":"zh_CN","typeName":"持仓Pos","typeSn":"1569209560881_3"},{"langType":"zh_CN","typeName":"主流币Pos","typeSn":"1569209560881_4"},{"langType":"zh_CN","typeName":"平台币Pos","typeSn":"1569209560881_5"}]
     * footBanner : http://chaindown-oss.oss-cn-hongkong.aliyuncs.com/upload/20190923113240890.png
     * detail :
     *
     *中文理财介绍
     * footTitle : 理财首页介绍标题
     * faqUrl : faq链接
     * contact : 理财联系方式
     */

    var banner: String? = null
    var url: String? = null
    var tipMine: String? = null
    var footBanner: String? = null
    var detail: String? = null
    var footTitle: String? = null
    var faqUrl: String? = null
    var contact: String? = null
    var typeConfig: List<TypeConfigBean>? = null

    class TypeConfigBean {
        /**
         * langType : zh_CN
         * typeName : 协议Pos
         * typeSn : 1569209560881_1
         */

        var langType: String? = null
        var typeName: String? = null
        var typeSn: String? = null
    }
}
