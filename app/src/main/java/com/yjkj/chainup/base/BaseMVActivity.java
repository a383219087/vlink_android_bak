package com.yjkj.chainup.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import com.yjkj.chainup.BR;


import java.lang.reflect.ParameterizedType;



public abstract class BaseMVActivity<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends NBaseActivity {


    protected BaseMVActivity() {
    }


    protected VM mViewModel;
    protected VDB mBinding;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public void initView() {
        mBinding = DataBindingUtil.setContentView(this, setContentView());
        mBinding.setLifecycleOwner(this);
        mViewModel = new ViewModelProvider(this).get(getTClass());
        mViewModel.getFinish().observe(this, o -> finish());
        mViewModel.getRefreshUI().observe(this, o -> {
            if (mViewModel != null) {
                mBinding.setVariable(BR._all, mViewModel);
                mBinding.executePendingBindings();//立即更新UI
            }
        });

        mBinding.setVariable(BR._all, mViewModel);
        mBinding.executePendingBindings();//立即更新UI
        getLifecycle().addObserver(mViewModel);

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

}

