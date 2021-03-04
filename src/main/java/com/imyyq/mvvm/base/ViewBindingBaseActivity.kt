package com.imyyq.mvvm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.fenxiangbuy.dialog.MsgDialog
import com.fenxiangbuy.dialog.WaitDialog
import com.github.anzewei.parallaxbacklayout.ParallaxBack
import com.imyyq.mvvm.bus.LiveDataBus
import com.imyyq.mvvm.utils.DialogUtil
import com.imyyq.mvvm.utils.Utils
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.kingja.loadsir.core.Transport


/**
 * 通过构造函数和泛型，完成 view 的初始化和 vm 的初始化，并且将它们绑定，
 */
@ParallaxBack(edge = ParallaxBack.Edge.LEFT, layout = ParallaxBack.Layout.PARALLAX)
abstract class ViewBindingBaseActivity<V : ViewBinding, VM : BaseViewModel<out BaseModel>> :
    AppCompatActivity(),
    IView<V, VM>, ILoadingDialog, ILoading, IActivityResult, IArgumentsFromIntent {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    val waitDialog by lazy {
        WaitDialog()
    }

    val msgDialog by lazy {
        MsgDialog()
    }

    private lateinit var mStartActivityForResult: ActivityResultLauncher<Intent>


    private lateinit var mLoadService: LoadService<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (superOnCreateAfterIsContinue()) return
        initViewAndViewModel()
        initParam()
        initUiChangeLiveData()
        initViewObservable()
        initLoadSir()
        initData()
    }

    open fun superOnCreateAfterIsContinue(): Boolean = false

    abstract override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): V

    @CallSuper
    override fun initViewAndViewModel() {
        mBinding = initBinding(layoutInflater, null)
        setContentView(contentView())
        mViewModel = initViewModel(this)
        mViewModel.mIntent = getArgumentsIntent()

        if (readyCompleteListener != null) {
            mViewModel.mUiChangeLiveData.initRepositoryReadyCompleteEvent()
            mViewModel.mUiChangeLiveData.repositoryReadyCompleteEvent?.observe(this, Observer {
                if (it == true) readyCompleteListener?.invoke()
            })
        }
        // 让 vm 可以感知 v 的生命周期
        lifecycle.addObserver(mViewModel)
    }


    open fun contentView() = mBinding.root


    val bindingIsInit: Boolean
        get() = this::mBinding.isInitialized


    val modelIsInit: Boolean
        get() = this::mViewModel.isInitialized

    @CallSuper
    override fun initUiChangeLiveData() {
        if (isViewModelNeedStartAndFinish()) {
            mViewModel.mUiChangeLiveData.initStartAndFinishEvent()

            fun setResult(pair: Pair<Int?, Intent?>) {
                pair.first?.let { resultCode ->
                    val intent = pair.second
                    if (intent == null) {
                        setResult(resultCode)
                    } else {
                        setResult(resultCode, intent)
                    }
                }
            }

            // vm 可以结束界面
            LiveDataBus.observe<Pair<Int?, Intent?>>(
                this,
                mViewModel.mUiChangeLiveData.finishEvent!!,
                Observer {
                    setResult(it)
                    finish()
                },
                true
            )
            // vm 设置切换动画
            LiveDataBus.observe<Pair<Int?, Int?>>(
                this,
                mViewModel.mUiChangeLiveData.overridePendingTransitionEvent!!,
                Observer {
                    overridePendingTransition(it.first ?: 0, it.second ?: 0)
                },
                true
            )
            LiveDataBus.observe<Pair<Int?, Intent?>>(
                this,
                mViewModel.mUiChangeLiveData.setResultEvent!!,
                Observer { setResult(it) },
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
                        waitDialog.isCancelable = it.isCancelable
                        waitDialog.show(this)
                    }
                    UIEventType.DIALOG_DISMISS -> {
                        waitDialog.dismiss()
                        msgDialog.dismiss()
                    }
                    UIEventType.DIALOG_MSG -> {
                        initMsgDialog(msgDialog, it)
                        msgDialog.show(this)
                    }
                }
            })
        }
    }

    open val readyCompleteListener: (() -> Unit)? = null

    open fun extUiEvent(it: UIEvent) {

    }

    open fun initMsgDialog(msgDialog: MsgDialog, it: UIEvent) {
        DialogUtil.initMsgDialog(msgDialog,it)
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
            mViewModel.mUiChangeLiveData.loadSirEvent?.observe(this, Observer {
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

    open fun getLoadSir() = LoadSir.getDefault()


    fun showStatusCallback(it: Class<out Callback>?) {
        mLoadService.showCallback(it)
    }


    fun setCallBack(callback: Class<out Callback?>?, transport: Transport?) {
        mLoadService.setCallBack(callback, transport)
    }


    fun showStatusSuccess() {
        mLoadService.showSuccess()
    }

    fun startActivity(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        startActivity(Utils.getIntentByMapOrBundle(this, clz, map, bundle))
    }

    fun startActivityForResult(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        initStartActivityForResult()
        mStartActivityForResult.launch(Utils.getIntentByMapOrBundle(this, clz, map, bundle))
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
     * 通过 [BaseViewModel.startActivity] 传递 bundle，在这里可以获取
     */
    final override fun getBundle(): Bundle? {
        return intent.extras
    }

    final override fun getArgumentsIntent(): Intent? {
        return intent
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


    override fun onDestroy() {
        super.onDestroy()

        // 界面销毁时移除 vm 的生命周期感知
        if (this::mViewModel.isInitialized) {
            lifecycle.removeObserver(mViewModel)
        }
        removeLiveDataBus(this)
    }
}