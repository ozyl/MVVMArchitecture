package com.imyyq.mvvm.data

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.ObservableField

data class ToolbarConfig (
    var title:ObservableField<String>?=null,
    var backClick: ObservableField<View.OnClickListener>?=null
)