package com.yjkj.chainup.base;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseeFragment extends com.trello.rxlifecycle2.components.support.RxFragment {



    public BaseeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(setLayoutId(), container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public abstract int setLayoutId();


    public void showToast(String msg) {
        if (!TextUtils.isEmpty(msg))
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private ProgressDialog mProgressDialog;

    public void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
        }
        if (TextUtils.isEmpty(msg)) {
            msg = "正在加载数据...";
        }
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    public void cancelProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }
    }

    public void jumpToActivity(Class activity) {
        Intent intent = new Intent(getContext(), activity);
        startActivity(intent);
    }

    public void jumpToActivity(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
