package com.baby.viewtools

import android.content.Context
import android.content.res.ColorStateList
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.library.R
import android.graphics.Paint.FontMetricsInt


open class LabelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    val textPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = textSize
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
    }
    var inverseBindingListener: InverseBindingListener? = null
    var selectIndex = -1
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }

    var textSize = 15
        set(value) {
            textPaint.textSize = value.toFloat()
            field = value
        }

    /*var textColor: ColorStateList = ColorStateList.valueOf(-0x1000000)
        set(value) {
            if(value!=null && field != value){
                field = value
            }
        }*/
    var textColor = Color.parseColor("#FFFFFF")
        set(value) {
            textPaint.color = value
            field = value
        }
    var appContent = "1"
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.commonAttr)
        textSize = a.getDimensionPixelSize(R.styleable.commonAttr_appTextSize, 15)
        /*textColor = a.getColorStateList(R.styleable.commonAttr_appTextColor)*/
        textColor = a.getColor(R.styleable.commonAttr_appTextColor, Color.parseColor("#FFFFFF"))
        selectIndex = a.getInt(R.styleable.commonAttr_selectIndex, -1)
        appContent = a.getString(R.styleable.commonAttr_appContent) ?: ""
        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val targetRect = Rect(0, 0, canvas.width, canvas.height)
        val fontMetrics = textPaint.fontMetricsInt
        val baseline = (targetRect.bottom.toFloat() + targetRect.top.toFloat() - fontMetrics.bottom.toFloat() - fontMetrics.top.toFloat()) / 2
        canvas.drawText(appContent, targetRect.centerX().toFloat(), baseline, textPaint)
    }

    companion object {
        @InverseBindingAdapter(attribute = "selectIndex", event = "indexAttrChanged")
        @JvmStatic
        fun getSelectIndex(view: LabelView): Int {
            return view.selectIndex
        }

        @BindingAdapter(value = arrayOf("selectIndex"))
        @JvmStatic
        fun setSelectIndex(view: LabelView, selectIndex: Int) {
            if (view.selectIndex != selectIndex) {
                view.selectIndex = selectIndex
            }
        }

        @BindingAdapter(value = arrayOf("selectIndexAttrChanged"), requireAll = false)
        fun setIndexAttrChanged(view: LabelView, inverseBindingListener: InverseBindingListener?) {
            if (inverseBindingListener == null) {
                view.inverseBindingListener = null
            } else {
                view.inverseBindingListener = inverseBindingListener
            }
        }

        @BindingAdapter(value = arrayOf("appContent"))
        @JvmStatic
        fun setAppContent(view: LabelView, appContent: String) {
            if (view.appContent != appContent) {
                view.appContent = appContent
            }
        }
    }
}