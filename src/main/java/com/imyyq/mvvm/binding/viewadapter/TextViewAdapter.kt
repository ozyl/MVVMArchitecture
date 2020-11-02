package com.imyyq.mvvm.binding.viewadapter

import android.graphics.Typeface
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("isBold")
fun setBold(view: TextView, isBold: Boolean?) {
    isBold?.run {
        if (this) {
            view.setTypeface(null, Typeface.BOLD)
        } else {
            view.setTypeface(null, Typeface.NORMAL)
        }
    }
}
