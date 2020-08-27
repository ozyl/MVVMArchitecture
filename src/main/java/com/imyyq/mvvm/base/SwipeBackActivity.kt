package com.imyyq.mvvm.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper
import com.imyyq.mvvm.app.GlobalConfig


open class SwipeBackActivity :AppCompatActivity(), BGASwipeBackHelper.Delegate {
    protected var mSwipeBackHelper: BGASwipeBackHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        initSwipeBackFinish();
        super.onCreate(savedInstanceState)
    }
    /**
     * 滑动返回执行完毕，销毁当前 Activity
     */
    override fun onSwipeBackLayoutExecuted() {
        mSwipeBackHelper?.swipeBackward()
    }

    /**
     * 正在滑动返回
     *
     * @param slideOffset 从 0 到 1
     */

    override fun onSwipeBackLayoutSlide(slideOffset: Float) {
    }
    /**
     * 没达到滑动返回的阈值，取消滑动返回动作，回到默认状态
     */
    override fun onSwipeBackLayoutCancel() {

    }

    override fun onBackPressed() {
        // 正在滑动返回的时候取消返回按钮事件
        mSwipeBackHelper?.run {
            if (this.isSliding) {
                return;
            }
            mSwipeBackHelper?.backward();
        }
    }

    open override fun isSupportSwipeBack(): Boolean = GlobalConfig.gIsSupportSwipe
    /**
     * 初始化滑动返回。在 super.onCreate(savedInstanceState) 之前调用该方法
     */
    private fun initSwipeBackFinish() {
        mSwipeBackHelper = BGASwipeBackHelper(this, this)
    }

}