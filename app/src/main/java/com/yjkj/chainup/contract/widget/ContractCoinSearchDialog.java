package com.yjkj.chainup.contract.widget;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.contract.sdk.ContractPublicDataAgent;
import com.contract.sdk.ContractSDKAgent;
import com.contract.sdk.data.Contract;
import com.contract.sdk.data.ContractGroup;
import com.contract.sdk.data.ContractTicker;
import com.contract.sdk.data.GlobalConfig;
import com.flyco.tablayout.SlidingTabLayout;
import com.yjkj.chainup.R;
import com.yjkj.chainup.base.NBaseDialogFragment;
import com.yjkj.chainup.contract.fragment.SlCoinSearchItemFragment;
import com.yjkj.chainup.db.constant.ParamConstant;
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil;
import com.yjkj.chainup.extra_service.eventbus.MessageEvent;
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil;
import com.yjkj.chainup.manager.LanguageUtil;
import com.yjkj.chainup.new_version.adapter.PageAdapter;
import com.yjkj.chainup.util.LogUtil;
import com.yjkj.chainup.util.SystemUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 合约币种搜索对话框
 */
public class ContractCoinSearchDialog extends NBaseDialogFragment {

    private static final String TAG = "ContractCoinSearchDialog";
    private EditText etSearch;
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
            LogUtil.d("et_search", "afterTextChanged==content is " + content);
            MessageEvent msgEvent = new MessageEvent(MessageEvent.coinSearchType);
            msgEvent.setMsg_content(content);
            NLiveDataUtil.postValue(msgEvent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftin_rightout_DialogFg_style);
    }

    @Override
    protected int setContentView() {
        return R.layout.sl_dialog_contract_coin_search;
    }

    @Override
    protected void initView() {

        initTab();
        showSearch();
        findViewById(R.id.alpha_ll).setOnClickListener(this);

        initAutoTextView();
    }

    private void initAutoTextView() {
        etSearch.setHint(LanguageUtil.getString(getContext(), "common_action_searchCoinPair"));
    }

    /*
     * 满足杠杆需求的分组
     */
    private int type;

    @Override
    protected void loadData() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            type = bundle.getInt(ParamConstant.TYPE);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(MessageEvent event) {
        if (MessageEvent.sl_contract_left_coin_type == event.getMsg_type()) {
            dismissDialog();
        }
    }


    private void showSearch() {
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(searchTextWatcher);
    }


    private void initTab() {
        ArrayList<String> showTitles = new ArrayList<String>();
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        GlobalConfig globalConfig = ContractSDKAgent.INSTANCE.getGlobalConfig();
        List<ContractGroup> contractGroup = null;
        if (globalConfig != null) {
            contractGroup = globalConfig.getContract_group();
        }
        if (contractGroup != null && !contractGroup.isEmpty()) {
            String language = SystemUtils.getSystemLanguage();
            for (int i = 0; i < contractGroup.size(); i++) {
                ContractGroup group = contractGroup.get(i);
                String groupName = group.getGroupName(language);
                showTitles.add(groupName);
                fragments.add(SlCoinSearchItemFragment.newInstance(i));
            }
        } else {
            List<ContractTicker> contractTickers = ContractPublicDataAgent.INSTANCE.getContractTickers();
            boolean isHasU = false;
            boolean isHasB = false;
            boolean isHasM = false;
            for (ContractTicker ticker : contractTickers) {
                if (ticker.getBlock() == Contract.CONTRACT_BLOCK_USDT) {
                    isHasU = true;
                } else if (ticker.getBlock() == Contract.CONTRACT_BLOCK_MAIN || ticker.getBlock() == Contract.CONTRACT_BLOCK_INNOVATION) {
                    isHasB = true;
                } else if (ticker.getBlock() == Contract.CONTRACT_BLOCK_SIMULATION) {
                    isHasM = true;
                }
            }
            ///USDT
            if (isHasU) {
                showTitles.add("USDT");
                fragments.add(SlCoinSearchItemFragment.newInstance(0));
            }
            ///币本位
            if (isHasB) {
                showTitles.add(LanguageUtil.getString(getContext(), "sl_str_inverse"));
                fragments.add(SlCoinSearchItemFragment.newInstance(1));
            }
            ///模拟
            if (isHasM) {
                showTitles.add(LanguageUtil.getString(getContext(), "sl_str_simulation"));
                fragments.add(SlCoinSearchItemFragment.newInstance(2));
            }
        }

        ViewPager vp_market_aa = findViewById(R.id.vp_market_aa);
        PageAdapter marketPageAdapter = new PageAdapter(getChildFragmentManager(), showTitles, fragments);
        vp_market_aa.setAdapter(marketPageAdapter);
        vp_market_aa.setOffscreenPageLimit(fragments.size());
        SlidingTabLayout tl_market_aa = findViewById(R.id.tl_market_aa);
        String[] showTitlesArray = new String[showTitles.size()];
        for (int j = 0; j < showTitles.size(); j++) {
            showTitlesArray[j] = showTitles.get(j);
        }
        tl_market_aa.setViewPager(vp_market_aa, showTitlesArray);
    }


    @Override
    protected void dismissDialog() {
        super.dismissDialog();
        LogUtil.d(TAG, "dismissDialog==");
        if (etSearch != null) {
            etSearch.removeTextChangedListener(searchTextWatcher);
        }

    }

    @Override
    public void showDialog(FragmentManager manager, String tag) {
        super.showDialog(manager, tag);
    }

}
