package com.chainup.contract.ui.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.chainup.contract.R;
import com.chainup.contract.adapter.CpPageAdapter;
import com.chainup.contract.base.CpNBaseDialogFragment;
import com.chainup.contract.eventbus.CpEventBusUtil;
import com.chainup.contract.eventbus.CpMessageEvent;
import com.chainup.contract.eventbus.CpNLiveDataUtil;
import com.chainup.contract.utils.ChainUpLogUtil;
import com.chainup.contract.utils.CpJsonUtils;
import com.chainup.contract.utils.CpSoftKeyboardUtil;
import com.chainup.contract.ws.CpWsContractAgentManager;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.yjkj.chainup.manager.CpLanguageUtil;
import com.yjkj.chainup.new_contract.fragment.CpCoinSearchItemFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 合约币种搜索对话框
 */
public class CpContractCoinSearchDialog extends CpNBaseDialogFragment implements CpWsContractAgentManager.WsResultCallback {

    private static final String TAG = "ContractCoinSearchDialog";
    private EditText etSearch;
    private JSONArray mContractList;
    private String contractListJson;
    private ArrayList<String> ContractCodeList = new ArrayList<>();
    private  ArrayList<String> showTitles = new ArrayList<String>();
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String content = "";
            if (null != s) {
                content = s.toString();
            }
            ChainUpLogUtil.d("et_search", "afterTextChanged==content is " + content);
            CpMessageEvent msgEvent = new CpMessageEvent(CpMessageEvent.coinSearchType);
            msgEvent.setMsg_content(content);
            CpNLiveDataUtil.postValue(msgEvent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftin_rightout_DialogFg_style);
    }
    @Override
    public void onStart() {
        super.onStart();
        Window window = this.getDialog().getWindow();
        //去掉dialog默认的padding
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.LEFT;
        //设置dialog的动画
        lp.windowAnimations = R.style.leftin_rightout_DialogFg_animstyle_buff;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());

    }

    @Override
    protected int setContentView() {
        return R.layout.cp_dialog_contract_coin_search;
    }

    @Override
    protected void initView() {

        initTab();
        showSearch();
        findViewById(R.id.ll_dissmiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
        initAutoTextView();

    }

    private void initAutoTextView() {
        etSearch.setHint(CpLanguageUtil.getString(getContext(), "common_action_searchCoinPair"));
    }

    /*
     * 满足杠杆需求的分组
     */
    private int type;

    @Override
    protected void loadData() {
        CpWsContractAgentManager.Companion.getInstance().addWsCallback(this);
        Bundle bundle = getArguments();
        if (null != bundle) {
            contractListJson = bundle.getString("contractList");
            try {
                mContractList = new JSONArray(contractListJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(CpMessageEvent event) {
        if (CpMessageEvent.sl_contract_left_coin_type == event.getMsg_type()) {
            dismissDialog();
        }
    }


    private void showSearch() {
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(searchTextWatcher);
    }


    private void initTab() {
        showTitles.clear();
        fragments.clear();
        boolean isHasU = false; //正向合约
        boolean isHasB = false;//币本位
        boolean isHasH = false;//混合合约
        boolean isHasM = false;//模拟合约
        String[] arrays = new String[mContractList.length()];
        ContractCodeList.clear();
        if (mContractList.length() == 0) {
            return;
        }
        for (int i = 0; i < mContractList.length(); i++) {
            String contractType="E";
            try {
                ChainUpLogUtil.e("合约币对", mContractList.getJSONObject(i).get("symbol").toString());
                // (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
                ChainUpLogUtil.e("合约方向", mContractList.getJSONObject(i).get("contractSide").toString());
                int contractSide = mContractList.getJSONObject(i).getInt("contractSide");
                 contractType = mContractList.getJSONObject(i).getString("contractType");
                //E,USDT合约 2,币本位合约 H,混合合约 S,模拟合约
                switch (contractType) {
                    case "E":
                        isHasU = true;
                        break;
                    case "H":
                        isHasM = true;
                        break;
                    case "S":
                        isHasH = true;
                        break;
                }
//                if (contractSide == 1 && contractType.equals("E")) {
//                    isHasU = true;
//                } else if (contractSide == 0 && contractType.equals("E")) {
//                    isHasB = true;
//                } else if (contractType.equals("S")) {
//                    isHasM = true;
//                } else {
//                    isHasH = true;
//                }
                JSONObject obj = (JSONObject) mContractList.get(i);
                String currentSymbolBuff = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase();
                ContractCodeList.add(currentSymbolBuff);
                arrays[i] = currentSymbolBuff;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, Object> rmap = new HashMap<>();
        rmap.put("bind", true);
        rmap.put("symbols", CpJsonUtils.gson.toJson(arrays));
        CpWsContractAgentManager.Companion.getInstance().sendMessage(rmap, this);
//        WsContractAgentManager.instance.sendMessage(hashMapOf("bind" to true, "symbols" to JsonUtils.gson.toJson(arrays)), this)

        //USDT
        if (isHasU) {
            showTitles.add(CpLanguageUtil.getString(getContext(), "cp_contract_data_text13"));
            fragments.add(CpCoinSearchItemFragment.newInstance(1, contractListJson));
        }
        //币本位
        if (isHasB) {
            showTitles.add(CpLanguageUtil.getString(getContext(), "cp_contract_data_text10"));
            fragments.add(CpCoinSearchItemFragment.newInstance(0, contractListJson));
        }
        //混合
        if (isHasH) {
            showTitles.add(CpLanguageUtil.getString(getContext(), "cp_contract_data_text12"));
            fragments.add(CpCoinSearchItemFragment.newInstance(2, contractListJson));
        }
        //模拟
        if (isHasM) {
            showTitles.add(CpLanguageUtil.getString(getContext(), "cp_contract_data_text11"));
            fragments.add(CpCoinSearchItemFragment.newInstance(3, contractListJson));
        }



        ViewPager vp_market_aa = findViewById(R.id.vp_market_aa);
        CpPageAdapter marketPageAdapter = new CpPageAdapter(getChildFragmentManager(), showTitles, fragments);
        vp_market_aa.setAdapter(marketPageAdapter);
        vp_market_aa.setOffscreenPageLimit(fragments.size());

        SlidingTabLayout tl_market_aa = findViewById(R.id.tl_market_aa);

        String[] showTitlesArray = new String[showTitles.size()];
        for (int j = 0; j < showTitles.size(); j++) {
            showTitlesArray[j] = showTitles.get(j);
        }
        tl_market_aa.setViewPager(vp_market_aa, showTitlesArray);


        tl_market_aa.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                ChainUpLogUtil.e(TAG,"点击的position"+position);
                if (showTitles.get(position).equals(CpLanguageUtil.getString(getContext(), "cl_market_text7"))){
                    CpEventBusUtil.post(new CpMessageEvent(CpMessageEvent.sl_contract_receive_coupon));
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }
    final Handler handler = new Handler();

    @Override
    protected void dismissDialog() {
        if (etSearch != null) {
            etSearch.setText("");
            etSearch.removeTextChangedListener(searchTextWatcher);
        }
        CpSoftKeyboardUtil.hideSoftKeyboard(etSearch);
        super.dismissDialog();
    }

    @Override
    public void showDialog(FragmentManager manager, String tag) {
        super.showDialog(manager, tag);
    }

    @Override
    public void onWsMessage(@NotNull String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            CpMessageEvent messageEvent = new CpMessageEvent(CpMessageEvent.sl_contract_sidebar_market_event);
            messageEvent.setMsg_content(jsonObject);
            CpEventBusUtil.post(messageEvent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String[] arrays = new String[ContractCodeList.size()];
        ContractCodeList.toArray(arrays);
        HashMap<String, Object> rmap = new HashMap<>();
        rmap.put("bind", false);
        rmap.put("symbols", CpJsonUtils.gson.toJson(arrays));
//        WsContractAgentManager.Companion.getInstance().sendMessage(rmap, this);
        dismissDialog();

    }
}
