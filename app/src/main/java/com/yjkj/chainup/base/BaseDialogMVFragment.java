package com.yjkj.chainup.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.yjkj.chainup.BR;
import com.yjkj.chainup.R;

import java.lang.reflect.ParameterizedType;


/**
 * Created by 奔跑的狗子
 * on 2021/04/03.
 */

public abstract class BaseDialogMVFragment<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends NBaseDialogFragment{


    protected VM mViewModel;
    protected VDB mBinding;
    protected View mView;

    @Override
    protected void loadData() {

    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater,setContentView(),null,false);
        mView = mBinding.getRoot();
        mBinding.setLifecycleOwner(this);
        mViewModel = new ViewModelProvider(this).get(getTClass());
        mBinding.setVariable(BR._all,mViewModel);
        mViewModel.getRefreshUI().observe(getViewLifecycleOwner() , o -> {
            if (mViewModel != null) {
                mBinding.setVariable(BR._all, mViewModel);
            }
        });
        mViewModel.getFinish().observe(getViewLifecycleOwner(), o -> dismissDialog());

        mBinding.executePendingBindings();
        getLifecycle().addObserver(mViewModel);
        return mView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog=super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialogBottomAnim);
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    /**
     * 获取泛型对相应的Class对象
     */
    private Class<VM> getTClass() {
        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        //返回表示此类型实际类型参数的 Type 对象的数组()，想要获取第二个泛型的Class，所以索引写1
        return (Class<VM>) type.getActualTypeArguments()[0];//<T>
    }
    @Override
    public void showDialog(FragmentManager manager, String tag) {
        super.showDialog(manager, tag);
    }
}

