package com.github.shchurov.horizontalwheelview

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener


@BindingAdapter(value = ["horizontalWheelView_index"])
fun HorizontalWheelView.setHorizontalWheelViewIndex(horizontalWheelView_index: Int) {
    if (this.viewIndex != horizontalWheelView_index) {
        this.viewIndex = horizontalWheelView_index
    }
}

@InverseBindingAdapter(attribute = "horizontalWheelView_index", event = "horizontalWheelViewIndexAttrChanged")
fun HorizontalWheelView.getHorizontalWheelViewIndex(): Int {
    return this.viewIndex
}

@BindingAdapter(value = arrayOf("horizontalWheelViewIndexAttrChanged"), requireAll = false)
fun HorizontalWheelView.setHorizontalWheelViewIndexAttrChanged(inverseBindingListener: InverseBindingListener?) {
    if (inverseBindingListener == null) {
        this.inverseBindingListener = null
    } else {
        this.inverseBindingListener = inverseBindingListener
    }
}
