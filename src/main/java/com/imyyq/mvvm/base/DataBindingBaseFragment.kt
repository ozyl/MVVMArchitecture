package com.imyyq.mvvm.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class DataBindingBaseFragment<V : ViewDataBinding, VM : BaseViewModel<out BaseModel>>(
    @LayoutRes private val layoutId: Int,
    private val varViewModelId: Int? = null,
    sharedViewModel: Boolean = false
) : ViewBindingBaseFragment<V, VM>(sharedViewModel) {

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): V =
        DataBindingUtil.inflate(inflater, layoutId, container, attachToParent)

    open val attachToParent = false

    final override fun initViewAndViewModel() {
        super.initViewAndViewModel()
        // 绑定 v 和 vm
        if (varViewModelId != null) {
            mBinding.setVariable(varViewModelId, mViewModel)
        }

        // 让 LiveData 和 xml 可以双向绑定
        mBinding.lifecycleOwner = this
    }

    override fun onDestroyView() {
        if (bindingIsInit){
            mBinding.unbind()
        }
        super.onDestroyView()
    }
}