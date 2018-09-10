package com.baby.viewtools

import android.content.Context
import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.library.R

open class RadioText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {
    var inverseBindingListener: InverseBindingListener? = null
    var viewIndex = -2
    var selectIndex = -1
        set(value) {
            //Log.e(javaClass.simpleName,"selectIndex: ${value}  viewIndex${viewIndex}")
            this@RadioText.isSelected = value == viewIndex
            field = value
        }
    init{
        val a = context.obtainStyledAttributes(attrs, R.styleable.commonAttr)
        viewIndex =  a.getInt(R.styleable.commonAttr_viewIndex,-2)
        selectIndex = a.getInt(R.styleable.commonAttr_selectIndex,-1)
        a.recycle()
    }
    companion object {
        @InverseBindingAdapter(attribute = "selectIndex", event = "indexAttrChanged")
        @JvmStatic fun getSelectIndex(radioText: RadioText): Int {
            return radioText.selectIndex
        }

        @BindingAdapter(value = arrayOf("selectIndex"))
        @JvmStatic	fun setSelectIndex(radioText: RadioText, selectIndex: Int) {
            if (radioText.selectIndex != selectIndex) {
                radioText.selectIndex = selectIndex
            }
        }

        @BindingAdapter(value = arrayOf("selectIndexAttrChanged"), requireAll = false)
        fun setIndexAttrChanged(radioText: RadioText, inverseBindingListener: InverseBindingListener?) {
            if (inverseBindingListener == null) {
                radioText.inverseBindingListener = null
            } else {
                radioText.inverseBindingListener = inverseBindingListener
            }
        }
    }
}