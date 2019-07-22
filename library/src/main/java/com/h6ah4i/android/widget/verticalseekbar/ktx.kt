package com.h6ah4i.android.widget.verticalseekbar
import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.library.R

@BindingAdapter(value = ["verticalSeekBar_index"])
fun VerticalSeekBar.setVerticalSeekBarIndex(verticalSeekBar_index: Int){
    if (this.index != verticalSeekBar_index) {
        this.index = verticalSeekBar_index
    }
}
@InverseBindingAdapter(attribute = "verticalSeekBar_index",event = "verticalSeekBarIndexAttrChanged")
fun VerticalSeekBar.getVerticalSeekBarIndex() : Int {
    return this.index
}
@BindingAdapter(value = arrayOf("verticalSeekBarIndexAttrChanged"), requireAll = false)
fun VerticalSeekBar.setVerticalSeekBarIndexAttrChanged(inverseBindingListener: InverseBindingListener?) {
    if (inverseBindingListener != null) {
        setOnIndexChangeCallBack {
            inverseBindingListener.onChange()
        }
    }
}
