package com.imyyq.mvvm.binding.viewadapter.edittext

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.imyyq.mvvm.binding.command.BindingConsumer


/**
 * EditText输入文字改变的监听
 */
@BindingAdapter(value = ["textChanged","afterTextChanged"], requireAll = false)
fun addTextChangedListener(
    editText: EditText,
    textChanged: BindingConsumer<String>?,
    afterTextChanged: BindingConsumer<String>?,
) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            textChanged?.call(s.toString())
        }

        override fun afterTextChanged(editable: Editable) {
            afterTextChanged?.call(editable.toString())
        }
    })
}

@BindingAdapter(value = ["focusChanged"], requireAll = false)
fun addFocusChangedListener(
    editText: EditText,
    focusChanged: BindingConsumer<Boolean>?
) {
    focusChanged?.run {
        editText.setOnFocusChangeListener { view, b -> this.call(b) }
    }
}
