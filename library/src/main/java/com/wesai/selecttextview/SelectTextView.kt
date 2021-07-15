package com.wesai.selecttextview

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.library.R

open class SelectTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {
    var mSelected = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.selectTextView)
        mSelected = a.getBoolean(R.styleable.selectTextView_selected, false)
        a.recycle()
    }
}