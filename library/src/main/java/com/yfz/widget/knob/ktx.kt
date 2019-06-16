package com.yfz.widget.knob

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.library.R

@BindingAdapter("knob_index")
fun Knob.setKnobIndex(newValue: Int){
    if (this.index != newValue) {
        this.index = newValue
    }
}
@InverseBindingAdapter(attribute = "knob_index",event = "knobIndexAttrChanged")
fun Knob.getKnobIndex() : Int {
    return this.index
}
@BindingAdapter(value = arrayOf("knobIndexAttrChanged"), requireAll = false)
fun Knob.setKnobIndexAttrChanged(inverseBindingListener: InverseBindingListener?) {
    if (inverseBindingListener == null) {
        val action = getTag(R.string.inversebindinglistener) as? (Knob.(index: Int) -> Unit)
        this.removeKnobChangeListener (onIndexChange = action)
        this.setTag(R.string.inversebindinglistener,null)
    } else {
        val action: (Knob.(index: Int) -> Unit) = {
            inverseBindingListener.onChange()
        }
        this.addKnobChangeListener(onIndexChange = action)
        this.setTag(R.string.inversebindinglistener, action)
    }
}