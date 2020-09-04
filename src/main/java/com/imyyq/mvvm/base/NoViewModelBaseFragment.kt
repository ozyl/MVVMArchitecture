package com.imyyq.mvvm.base

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import com.kingja.loadsir.callback.Callback

/**
 * 详见 [NoViewModelBaseActivity] 的注释，一样的
 */
abstract class NoViewModelBaseFragment<V : ViewBinding> :
    ViewBindingBaseFragment<V, BaseViewModel<BaseModel>>() {

    @SuppressLint("MissingSuperCall")
    final override fun initViewAndViewModel() {
    }

    final override fun isViewModelNeedStartAndFinish() = false

    final override fun isViewModelNeedStartForResult() = false

    final override fun isNeedLoadingDialog() = false

    @SuppressLint("MissingSuperCall")
    final override fun initLoadSir() {
    }

    final override fun getLoadSirTarget() = null


    @SuppressLint("MissingSuperCall")
    final override fun initUiChangeLiveData() {
    }
    final override fun initViewModel(viewModelStoreOwner: ViewModelStoreOwner): BaseViewModel<BaseModel> {
        return super.initViewModel(viewModelStoreOwner)
    }

    final override fun onLoadSirShowed(it: Class<out Callback>) {
    }

    final override fun onLoadSirSuccess() {
    }

    final override fun onLoadSirReload() {
    }
}