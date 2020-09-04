package com.imyyq.mvvm.base

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import com.kingja.loadsir.callback.Callback

/**
 * 如果你的界面足够简单，不需要 ViewModel，那么可以继承自此类。
 * 此类不可以使用 [mViewModel] 变量，不可使用 vm 相关的方法，
 * 不可使用 LoadingDialog，不可使用 LoadSir
 */
abstract class NoViewModelBaseActivity<V : ViewBinding> :
    ViewBindingBaseActivity<V, BaseViewModel<BaseModel>>() {

    @SuppressLint("MissingSuperCall")
    final override fun initViewAndViewModel() {
        setContentView(mBinding.root)
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