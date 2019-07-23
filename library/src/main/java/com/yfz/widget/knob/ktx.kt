package com.yfz.widget.knob

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.library.R

@BindingAdapter(value = ["knob_index"])
fun Knob.setKnobIndex(knob_index: Int) {
    if (this.index != knob_index) {
        this.bindingIndex(knob_index)
    }
}

@InverseBindingAdapter(attribute = "knob_index", event = "knobIndexAttrChanged")
fun Knob.getKnobIndex(): Int {
    return this.index
}

@BindingAdapter(value = arrayOf("knobIndexAttrChanged"), requireAll = false)
fun Knob.setKnobIndexAttrChanged(inverseBindingListener: InverseBindingListener?) {
    if (inverseBindingListener == null) {
        val action = getTag(R.string.inversebindinglistener) as? (Knob.(index: Int) -> Unit)
        this.removeKnobChangeListener(onIndexChange = action)
        this.setTag(R.string.inversebindinglistener, null)
    } else {
        val action: (Knob.(index: Int) -> Unit) = {
            inverseBindingListener.onChange()
        }
        this.addKnobChangeListener(onIndexChange = action)
        this.setTag(R.string.inversebindinglistener, action)
    }
}