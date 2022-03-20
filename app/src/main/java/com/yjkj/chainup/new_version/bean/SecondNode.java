package com.yjkj.chainup.new_version.bean;


import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import java.util.List;

public  class SecondNode extends BaseExpandNode {

    private String title;
    private String fileName;

    public SecondNode( String title,String fileName) {
        this.title = title;
        this.fileName = fileName;

        setExpanded(false);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return null;
    }
}
