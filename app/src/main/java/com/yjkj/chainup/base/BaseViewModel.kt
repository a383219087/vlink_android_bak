package com.yjkj.chainup.base



import androidx.lifecycle.*


abstract class BaseViewModel : ViewModel(), LifecycleObserver {

    var finish = MutableLiveData<Void?>()
    var refreshUI = MutableLiveData(false)


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
    }

    fun finish() {
        finish.value = null
    }


    fun refresh() {
        refreshUI.value = !refreshUI.value!!
    }






}