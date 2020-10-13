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
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.fenxiangbuy.dialog.MsgDialog
import com.fenxiangbuy.dialog.WaitDialog
import com.fenxiangbuy.dialog.data.model.BtnConfig
import com.imyyq.mvvm.bus.LiveDataBus
import com.imyyq.mvvm.utils.Utils
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class ViewBindingBaseFragment<V : ViewBinding, VM : BaseViewModel<out BaseModel>>(
    private val sharedViewModel: Boolean = false
) : Fragment(),
    IView<V, VM>, ILoadingDialog, ILoading, IActivityResult {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    private lateinit var mStartActivityForResult: ActivityResultLauncher<Intent>

    val waitDialog by lazy {
        WaitDialog()
    }

    val msgDialog by lazy {
        MsgDialog()
    }

    private lateinit var mLoadService: LoadService<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = initBinding(inflater, container)
        if (getLoadSirTarget() == this) {
            mLoadService = LoadSir.getDefault().register(
                contentView()
            ) { onLoadSirReload() }
            return mLoadService.loadLayout
        }
        return contentView()
    }

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
        // 让 vm 可以感知 v 的生命周期
        lifecycle.addObserver(mViewModel)
    }

    @CallSuper
    override fun initUiChangeLiveData() {
        if (isViewModelNeedStartAndFinish()) {
            mViewModel.mUiChangeLiveData.initStartAndFinishEvent()

            // vm 可以结束界面
            LiveDataBus.observe(
                this,
                mViewModel.mUiChangeLiveData.finishEvent!!,
                Observer { activity?.finish() },
                true
            )
            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityEvent!!,
                Observer {
                    startActivity(it)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityWithMapEvent!!,
                Observer {
                    startActivity(it?.first, it?.second)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityEventWithBundle!!,
                Observer {
                    startActivity(it?.first, bundle = it?.second)
                },
                true
            )
        }
        if (isViewModelNeedStartForResult()) {
            mViewModel.mUiChangeLiveData.initStartActivityForResultEvent()

            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEvent!!,
                Observer {
                    startActivityForResult(it)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEventWithBundle!!,
                Observer {
                    startActivityForResult(it?.first, bundle = it?.second)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEventWithMap!!,
                Observer {
                    startActivityForResult(it?.first, it?.second)
                },
                true
            )
        }

        if (isNeedLoadingDialog()) {
            mViewModel.mUiChangeLiveData.initLoadingDialogEvent()

            // 显示waitLoading
            mViewModel.mUiChangeLiveData.UIEvent?.observe(this, Observer {
                it ?: return@Observer
                if (it.isExtendsMsgDialog) {
                    extUiEvent(it)
                    return@Observer
                }
                when (it.type) {
                    UIEventType.DIALOG_WAIT -> {
                        waitDialog.hintMsg = it.msg
                        (activity as? AppCompatActivity?)?.run {
                            waitDialog.show(this)
                        }
                    }
                    UIEventType.DIALOG_DISMISS -> {
                        waitDialog.dismiss()
                        msgDialog.dismiss()
                    }
                    UIEventType.DL_TIP_SUCCESS -> {
                    }
                    UIEventType.DL_TIP_FAIL -> {
                    }
                    UIEventType.DL_TIP_WARNING -> {
                    }
                    UIEventType.DIALOG_MSG -> {
                        initMsgDialog(msgDialog, it)
                        (activity as? AppCompatActivity?)?.run {
                            waitDialog.show(this)
                        }
                    }
                }
            })
        }
    }


    open fun extUiEvent(it: UIEvent) {

    }


    open fun initMsgDialog(msgDialog: MsgDialog, it: UIEvent) {
        msgDialog.content = it.msg ?: ""
        msgDialog.title = it.title ?: "温馨提示"

        if (it.autoConfirm || it.confirmVoidCallback != null) msgDialog.confirm = BtnConfig(
            click = it.confirmVoidCallback,
            text = it.confirmText
        )
        if (it.autoCancel || it.cancelVoidCallback != null) msgDialog.cancel =
            BtnConfig(
                click = it.cancelVoidCallback,
                text = it.cancelText
            )
    }

    @CallSuper
    override fun initLoadSir() {
        // 只有目标不为空的情况才有实例化的必要
        if (getLoadSirTarget() != null) {
            if (!this::mLoadService.isInitialized) {
                mLoadService = LoadSir.getDefault().register(
                    getLoadSirTarget()
                ) { onLoadSirReload() }
            }
            mViewModel.mUiChangeLiveData.initLoadSirEvent()
            mViewModel.mUiChangeLiveData.loadSirEvent?.observe(this, Observer {
                if (it == null) {
                    mLoadService.showSuccess()
                    onLoadSirSuccess()
                } else {
                    mLoadService.showCallback(it)
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
        initStartActivityForResult()
        mStartActivityForResult.launch(Utils.getIntentByMapOrBundle(activity, clz, map, bundle))
    }

    private fun initStartActivityForResult() {
        if (!this::mStartActivityForResult.isInitialized) {
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

    override fun onDestroy() {
        super.onDestroy()

        // 界面销毁时移除 vm 的生命周期感知
        if (this::mViewModel.isInitialized) {
            lifecycle.removeObserver(mViewModel)
        }
        removeLiveDataBus(this)
    }
}