package com.imyyq.mvvm.app

import androidx.lifecycle.*
import com.apkfuns.logutils.LogUtils

/**
 * App 状态监听器，可用于判断应用是在后台还是在前台
 */
object AppStateTracker {
    private var mIsTract = false
    private var mChangeListener: MutableList<AppStateChangeListener> = mutableListOf()
    const val STATE_FOREGROUND = 0
    const val STATE_BACKGROUND = 1
    var currentState = STATE_BACKGROUND
        get() {
            if (!mIsTract) {
                throw RuntimeException("必须先调用 track 方法")
            }
            return field
        }

    fun track(appStateChangeListener: AppStateChangeListener) {
        if (!mIsTract) {
            mIsTract = true
            ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleChecker())
        }
        mChangeListener.add(appStateChangeListener)
    }

    fun track(lifecycleOwner: LifecycleOwner,appStateChangeListener: AppStateChangeListener) {
        track(appStateChangeListener)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                LogUtils.d("生命周期->移除前后台监听")
                mChangeListener.remove(appStateChangeListener)
            }
        })
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackground()
    }

    class LifecycleChecker : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            currentState = STATE_FOREGROUND
            mChangeListener.forEach {
                it.appTurnIntoForeground()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            currentState = STATE_BACKGROUND
            mChangeListener.forEach {
                it.appTurnIntoBackground()
            }
        }
    }
}