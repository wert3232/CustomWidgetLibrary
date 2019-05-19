package com.baby.viewtools

import android.content.Context
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.library.R

open class OrnamentImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {
    var inverseBindingListener: InverseBindingListener? = null
    var viewIndex = -2
    var selectIndex = -1
        set(value) {
            this@OrnamentImageView.visibility = if(value == viewIndex){
                View.VISIBLE
            }
            else {
                View.GONE
            }
            field = value
        }
    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.commonAttr)
        isClickable = false
        viewIndex =  a.getInt(R.styleable.commonAttr_viewIndex,-2)
        selectIndex = a.getInt(R.styleable.commonAttr_selectIndex,-1)
        a.recycle()
    }
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
    companion object {
        @InverseBindingAdapter(attribute = "selectIndex", event = "indexAttrChanged")
        @JvmStatic fun getSelectIndex(view: OrnamentImageView): Int {
            return view.selectIndex
        }

        @BindingAdapter(value = arrayOf("selectIndex"))
        @JvmStatic	fun setSelectIndex(view: OrnamentImageView, selectIndex: Int) {
            if (view.selectIndex != selectIndex) {
                view.selectIndex = selectIndex
            }
        }

        @BindingAdapter(value = arrayOf("selectIndexAttrChanged"), requireAll = false)
        fun setIndexAttrChanged(view: OrnamentImageView, inverseBindingListener: InverseBindingListener?) {
            if (inverseBindingListener == null) {
                view.inverseBindingListener = null
            } else {
                view.inverseBindingListener = inverseBindingListener
            }
        }
    }
}