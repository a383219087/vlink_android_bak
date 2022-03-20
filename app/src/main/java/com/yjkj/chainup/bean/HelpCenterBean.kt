package com.yjkj.chainup.bean

import com.google.gson.annotations.SerializedName

data class HelpCenterBean(
        @SerializedName("articleTypeName") var articleTypeName: String? = "",
        @SerializedName("cmsArticleList") var cmsArticleList: List<CmsArticle>? = listOf(),
        @SerializedName("id") var id: Int? = 0
)

data class CmsArticle(
        @SerializedName("articleTypeId") var articleTypeId: Int? = 0,
        @SerializedName("articleTypeName") var articleTypeName: String? = "",
        @SerializedName("content") var content: String? = "",
        @SerializedName("ctime") var ctime: Long? = 0,
        @SerializedName("fileName") var fileName: String? = "",
        @SerializedName("footerFlag") var footerFlag: Int? = 0,
        @SerializedName("id") var id: Int? = 0,
        @SerializedName("lang") var lang: String? = "",
        @SerializedName("mtime") var mtime: Any? = Any(),
        @SerializedName("publisher") var publisher: Any? = Any(),
        @SerializedName("publisherId") var publisherId: Int? = 0,
        @SerializedName("sort") var sort: Int? = 0,
        @SerializedName("title") var title: String? = ""
)