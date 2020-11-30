package com.imyyq.mvvm.utils


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