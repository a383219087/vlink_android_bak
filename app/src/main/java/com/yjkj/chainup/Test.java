package com.yjkj.chainup;

import android.os.Bundle;

import com.yjkj.chainup.base.BaseActivity;
import com.yjkj.chainup.new_version.adapter.MarketContractDropAdapter;

import java.util.ArrayList;

public class Test extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MarketContractDropAdapter(new ArrayList<>());
    }
}
