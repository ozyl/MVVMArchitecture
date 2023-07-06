package com.imyyq.mvvm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.apkfuns.logutils.LogUtils
import com.fenxiangbuy.dialog.MsgDialog
import com.fenxiangbuy.dialog.WaitDialog
import com.imyyq.mvvm.bus.LiveDataBus
import com.imyyq.mvvm.utils.DialogUtil
import com.imyyq.mvvm.utils.Utils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.kingja.loadsir.core.Transport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

abstract class ViewBindingBaseFragment<V : ViewBinding, VM : BaseViewModel<out BaseModel>>(
    private val sharedViewModel: Boolean = false
) : BaseVisibilityFragment(),
    IView<V, VM>, ILoadingDialog, ILoading, IActivityResult {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    private lateinit var mStartActivityForResult: ActivityResultLauncher<Intent>

    var waitDialog = WeakReference<WaitDialog>(null)
        get() {
            if (field.get() == null) field = WeakReference(WaitDialog())
            return field
        }


    var msgDialog = WeakReference<MsgDialog>(null)
        get() {
            if (field.get() == null) field = WeakReference(MsgDialog())
            return field
        }

    private lateinit var mLoadService: LoadService<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initStartActivityForResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = initBinding(inflater, container)
        if (getLoadSirTarget() == this) {
            mLoadService = getLoadSir().register(
                contentView()
            ) { onLoadSirReload() }
            return mLoadService.loadLayout
        }
        return contentView()
    }

    open fun getLoadSir() = LoadSir.getDefault()


    open fun contentView() = mBinding.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewAndViewModel()
        initParam()
        initUiChangeLiveData()
        initViewObservable()
        initLoadSir()
        initData()
    }

    @CallSuper
    override fun initViewAndViewModel() {
        mViewModel = if (sharedViewModel) {
            initViewModel(requireActivity())
        } else {
            initViewModel(this)
        }

        if (readyCompleteListener != null) {
            mViewModel.mUiChangeLiveData.initRepositoryReadyCompleteEvent()
            mViewModel.mUiChangeLiveData.repositoryReadyCompleteEvent?.observe(viewLifecycleOwner, Observer {
                if (it == true) readyCompleteListener?.invoke()
            })
        }
        // 让 vm 可以感知 v 的生命周期
        lifecycle.addObserver(mViewModel)
    }

    @CallSuper
    override fun initUiChangeLiveData() {
        initUiChangeLiveData(mViewModel)
    }

    fun initUiChangeLiveData(viewModel: BaseViewModel<out BaseModel>) {
        if (isViewModelNeedStartAndFinish()) {
            viewModel.mUiChangeLiveData.initStartAndFinishEvent()

            // vm 可以结束界面
            LiveDataBus.observe(
                this,
                viewModel.mUiChangeLiveData.finishEvent!!,
                Observer { activity?.finish() },
                true
            )
            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                viewModel.mUiChangeLiveData.startActivityEvent!!,
                Observer {
                    startActivity(it)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(
                this,
                viewModel.mUiChangeLiveData.startActivityWithMapEvent!!,
                Observer {
                    startActivity(it?.first, it?.second)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(
                this,
                viewModel.mUiChangeLiveData.startActivityEventWithBundle!!,
                Observer {
                    startActivity(it?.first, bundle = it?.second)
                },
                true
            )
        }

        if (isViewModelNeedStartForResult()) {
            viewModel.mUiChangeLiveData.initStartActivityForResultEvent()

            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                viewModel.mUiChangeLiveData.startActivityForResultEvent!!,
                Observer {
                    startActivityForResult(it)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(
                this,
                viewModel.mUiChangeLiveData.startActivityForResultEventWithBundle!!,
                Observer {
                    startActivityForResult(it?.first, bundle = it?.second)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(
                this,
                viewModel.mUiChangeLiveData.startActivityForResultEventWithMap!!,
                Observer {
                    startActivityForResult(it?.first, it?.second)
                },
                true
            )
        }

        if (isNeedLoadingDialog()) {
            viewModel.mUiChangeLiveData.initLoadingDialogEvent()

            // 显示waitLoading
            viewModel.mUiChangeLiveData.UIEvent?.observe(viewLifecycleOwner, Observer {
                it ?: return@Observer
                if (it.isExtendsMsgDialog) {
                    extUiEvent(it)
                    return@Observer
                }
                when (it.type) {
                    UIEventType.DIALOG_WAIT -> {
                        waitDialog.get()?.run {
                            DialogUtil.initWaitDialog(this, it)
                            this.show(this@ViewBindingBaseFragment)
                        }
                    }
                    UIEventType.DIALOG_DISMISS -> {
                        waitDialog.get()?.dismiss()
                        msgDialog.get()?.dismiss()
                    }
                    UIEventType.DIALOG_DISMISS_WAIT -> {
                        waitDialog.get()?.dismiss()
                    }
                    UIEventType.DIALOG_DISMISS_MSG -> {
                        msgDialog.get()?.dismiss()
                    }
                    UIEventType.DIALOG_MSG -> {
                        val dialog =
                            if (it.tag != null) {
                                MsgDialog()
                            } else {
                                msgDialog.get()
                            }
                        dialog?.run {
                            initMsgDialog(this, it)
                            this.show(this@ViewBindingBaseFragment)
                        }
                    }
                }
            })
        }
    }

    open fun extUiEvent(it: UIEvent) {

    }


    open fun initMsgDialog(msgDialog: MsgDialog, it: UIEvent) {
        DialogUtil.initMsgDialog(msgDialog, it)
    }

    @CallSuper
    override fun initLoadSir() {
        // 只有目标不为空的情况才有实例化的必要
        if (getLoadSirTarget() != null) {
            mLoadService = getLoadSir().register(
                getLoadSirTarget()
            ) { onLoadSirReload() }
            mLoadService.showSuccess()
            mViewModel.mUiChangeLiveData.initLoadSirEvent()
            mViewModel.mUiChangeLiveData.loadSirEvent?.observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    showStatusSuccess()
                    onLoadSirSuccess()
                } else {
                    showStatusCallback(it)
                    onLoadSirShowed(it)
                }
            })
        }
    }

    fun startActivity(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        startActivity(Utils.getIntentByMapOrBundle(activity, clz, map, bundle))
    }

    fun startActivityForResult(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        mStartActivityForResult.launch(Utils.getIntentByMapOrBundle(activity, clz, map, bundle))
    }

    open val readyCompleteListener: (() -> Unit)? = null



    fun showStatusCallback(it: Class<out Callback>?) {
        try {
            if (this::mLoadService.isInitialized)
                mLoadService.showCallback(it)
        } catch (e: Exception) {
            LogUtils.e(e)
        }
    }


    fun showStatusSuccess() {
        try {
            if (this::mLoadService.isInitialized)
                mLoadService.showSuccess()
        } catch (e: Exception) {
            LogUtils.e(e)
        }
    }


    fun setCallBack(callback: Class<out Callback?>?, transport: Transport?) {
        mLoadService.setCallBack(callback, transport)
    }

    private fun initStartActivityForResult() {
        if (!this::mStartActivityForResult.isInitialized && isRegisterActivityResult()) {
            mStartActivityForResult =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val data = it.data ?: Intent()
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            onActivityResultOk(data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResultOk(data)
                            }
                        }
                        Activity.RESULT_CANCELED -> {
                            onActivityResultCanceled(data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResultCanceled(data)
                            }
                        }
                        else -> {
                            onActivityResult(it.resultCode, data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResult(it.resultCode, data)
                            }
                        }
                    }
                }
        }
    }

    /**
     * 通过 setArguments 传递 bundle，在这里可以获取
     */
    override fun getBundle(): Bundle? {
        return arguments
    }

    /**
     * <pre>
     *     // 一开始我们这么写
     *     mViewModel.liveData.observe(this, Observer { })
     *
     *     // 用这个方法可以这么写
     *     observe(mViewModel.liveData) { }
     *
     *     // 或者这么写
     *     observe(mViewModel.liveData, this::onChanged)
     *     private fun onChanged(s: String) { }
     * </pre>
     */
    fun <T> observe(liveData: LiveData<T>, onChanged: ((t: T) -> Unit)) =
        liveData.observe(this, Observer { onChanged(it) })

    override fun onDestroyView() {
        super.onDestroyView()

        // 通过反射，解决内存泄露问题
        GlobalScope.launch {
            var clz: Class<*>? = this@ViewBindingBaseFragment.javaClass
            while (clz != null) {
                // 找到 mBinding 所在的类
                if (clz == ViewBindingBaseFragment::class.java) {
                    try {
                        val field = clz.getDeclaredField("mBinding")
                        field.isAccessible = true
                        field.set(this@ViewBindingBaseFragment, null)
                    } catch (ignore: Exception) {
                    }
                }
                clz = clz.superclass
            }
        }
    }

    val bindingIsInit: Boolean
        get() = this::mBinding.isInitialized

    val modelIsInit: Boolean
        get() = this::mViewModel.isInitialized

    override fun onDestroy() {
        super.onDestroy()
        // 界面销毁时移除 vm 的生命周期感知
        if (this::mViewModel.isInitialized) {
            lifecycle.removeObserver(mViewModel)
        }
        removeLiveDataBus(this)
    }
}