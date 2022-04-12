package com.yjkj.chainup.base



import androidx.lifecycle.*
import com.yjkj.chainup.common.binding.command.BindingFunction
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


abstract class BaseViewModel : ViewModel(), LifecycleObserver {

    var finish = MutableLiveData<Void?>()
    var refreshUI = MutableLiveData(false)
    private var mCompositeDisposable: CompositeDisposable? = null
     var apiService: ApiService = HttpClient.instance.createApi()


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected open fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onDestroy() {
        detachView()
    }

    fun finish() {
        finish.value = null
    }


    fun refresh() {
        refreshUI.value = !refreshUI.value!!
    }


    /**
     * 启动网络任务
     */
    fun <D> startTask(single: Observable<D>, onNext: Consumer<in D>, onError: Consumer<in Throwable>) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        mCompositeDisposable!!.add(
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
        )
    }


    private fun detachView() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable!!.dispose()
            mCompositeDisposable!!.clear()
            mCompositeDisposable = null
        }
    }




}