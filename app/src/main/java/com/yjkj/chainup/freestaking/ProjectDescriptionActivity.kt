package com.yjkj.chainup.freestaking

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import kotlinx.android.synthetic.main.activity_project_description.*

/**
 * 查看公告
 */
@Route(path = RoutePath.ProjectDescriptionActivity)
class ProjectDescriptionActivity : NewBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_description)
        initView()

    }

    @SuppressLint("NewApi")
    private fun initView() {
        toolbar_detail.title = ""
        setSupportActionBar(toolbar_detail)
        toolbar_detail.setNavigationOnClickListener { finish() }
        var projectName = intent.getStringExtra(PROJECT_NAME)
        var projectInfo = intent.getStringExtra(PROJECT_INFO)
        tv_projectName.text = projectName
        tv_introduction.text = projectInfo
//        tv_introduction.setTextSize(14)
//        tv_introduction.setTextColor(getColor(R.color.text_color))
//        tv_introduction.setStrList(arrayListOf(projectInfo))
    }
}
