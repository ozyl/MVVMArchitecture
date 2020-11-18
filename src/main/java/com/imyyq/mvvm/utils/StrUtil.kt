package com.imyyq.mvvm.utils


import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.imyyq.mvvm.app.BaseApp
import okio.JvmStatic
import java.util.*


object StrUtil {
    /**
     * Return whether the string is null or 0-length.
     *
     * @param s The string.
     * @return `true`: yes<br></br> `false`: no
     */
    @JvmStatic
    fun isEmpty(s: CharSequence?): Boolean {
        return s.isNullOrBlank()
    }

}