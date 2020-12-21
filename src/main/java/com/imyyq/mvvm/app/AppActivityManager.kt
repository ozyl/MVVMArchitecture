package com.imyyq.mvvm.app

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

/**
 * App 全局 Activity 管理器，采用 registerActivityLifecycleCallbacks 监听所有的 Activity 的创建和销毁。
 * 可通过 [GlobalConfig.gIsNeedActivityManager] 关闭这个功能
 */
object AppActivityManager {
    private val mActivityList = mutableListOf<Activity>()

    fun add(activity: Activity) = mActivityList.add(activity)

    fun remove(activity: Activity) = mActivityList.remove(activity)

    fun isEmpty(): Boolean {
        checkEnabled()
        return mActivityList.isEmpty()
    }

    fun get(clazz: Class<out Activity>): Activity? {
        checkEnabled()
        return mActivityList.find { it.javaClass == clazz }
    }

    fun current(): Activity? {
        checkEnabled()
        if (mActivityList.isNotEmpty()) {
            return mActivityList.last()
        }
        return null
    }

    fun currentAppCompatActivity(): AppCompatActivity? {
        return current() as? AppCompatActivity
    }

    fun finishCurrentActivity() {
        checkEnabled()
        current()?.finish()
    }

    private fun finishActivity(activity: Activity) {
        checkEnabled()
        mActivityList.forEach {
            if (it == activity) {
                remove(activity)
                it.finish()
                return
            }
        }
    }

    fun finishActivity(vararg clazz: Class<Activity>) {
        checkEnabled()
        if (clazz.isEmpty()) return
        clazz.forEach {
            val activity  = get(it)?:return
            finishActivity(activity)
        }
    }

    fun finishAllActivity() {
        checkEnabled()
        mActivityList.forEach {
            it.finish()
        }
        mActivityList.clear()
    }

    fun finishAllActivity(vararg excludeClazz: Class<Activity>) {
        checkEnabled()
        mActivityList.forEachIndexed { index, activity ->
            excludeClazz.forEach {
                if (get(it)==activity){
                    return@forEachIndexed
                }
            }
            activity.finish()
            mActivityList.removeAt(index)
        }
    }

    private fun checkEnabled() {
        if (!GlobalConfig.gIsNeedActivityManager) {
            throw RuntimeException("GlobalConfig.mIsNeedActivityManager 开关没有打开，不能使用 AppActivityManager 类")
        }
    }
}