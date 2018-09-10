package com.baby.viewtools
import android.content.Context
import android.content.res.ColorStateList
import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.library.R
import android.graphics.Paint.FontMetricsInt
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup.LayoutParams.*
import android.widget.TextView

open class LabelView2 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    val textPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = this@LabelView2.textSize.toFloat()
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
    }
    var inverseBindingListener: InverseBindingListener? = null
    var selectIndex = -1
        set(value) {
            if(value != field) {
                field = value
                invalidate()
            }
        }

    var textSize = 40
        set(value) {
            if(value != field){
                textPaint.textSize = value.toFloat()
                field = value
            }
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
    var appContent: String = ""
        set(value) {
            if(field != value) {
                measureTxt(value)
                if(field.length != value.length){
                    requestLayout()
                }
                field = value
                invalidate()
            }
        }
    private var txtWidth = 20
        set(value) {
            if(value < 20){
                field = 20
            }else{
                field = value
            }
        }
    private var txtHeight = 20
        set(value) {
            if(value < 20){
                field = 20
            }else{
                field = value
            }
        }
    init{
        val a = context.obtainStyledAttributes(attrs, R.styleable.commonAttr)
        textSize = a.getDimensionPixelSize(R.styleable.commonAttr_appTextSize, 40)
        /*textColor = a.getColorStateList(R.styleable.commonAttr_appTextColor)*/
        textColor = a.getColor(R.styleable.commonAttr_appTextColor, Color.parseColor("#FFFFFF"))
        selectIndex = a.getInt(R.styleable.commonAttr_selectIndex,-1)
        appContent = a.getString(R.styleable.commonAttr_appContent) ?: ""
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
       //Log.e(javaClass.simpleName,"onMeasure")
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var w = widthSpecSize   //定义测量宽，高(不包含测量模式),并设置默认值，查看View#getDefaultSize可知
        var h = heightSpecSize
        if(widthMode ==  MeasureSpec.AT_MOST && heightMode ==  MeasureSpec.AT_MOST
                && layoutParams.width == WRAP_CONTENT && layoutParams.height == WRAP_CONTENT
        ){
            w = txtWidth
            h = txtHeight
        }else if(widthMode ==  MeasureSpec.AT_MOST && layoutParams.width == WRAP_CONTENT){
            w = txtWidth
            h =  getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        }else if(heightMode ==  MeasureSpec.AT_MOST && layoutParams.height == WRAP_CONTENT){
            w =  getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
            h = txtHeight
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        setMeasuredDimension(w,h)
    }
    private fun measureTxt(txt: String){
        val rect = Rect()
        textPaint.getTextBounds(txt,0,txt.length,rect)
        rect.apply {
            txtWidth = this.width() + 10
            txtHeight = this.height()  + 10
        }
    }
    override fun onDraw(canvas: Canvas) {
        val targetRect = Rect(0, 0, canvas.width, canvas.height)
        val fontMetrics = textPaint.fontMetricsInt
        val baseline = (targetRect.bottom.toFloat() + targetRect.top.toFloat() - fontMetrics.bottom.toFloat() - fontMetrics.top.toFloat()) / 2
        canvas.drawText(appContent, targetRect.centerX().toFloat(), baseline, textPaint)
    }
    companion object {
        @InverseBindingAdapter(attribute = "selectIndex", event = "indexAttrChanged")
        @JvmStatic fun getSelectIndex(view: LabelView): Int {
            return view.selectIndex
        }

        @BindingAdapter(value = arrayOf("selectIndex"))
        @JvmStatic	fun setSelectIndex(view: LabelView, selectIndex: Int) {
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
        @JvmStatic	fun setAppContent(view: LabelView, appContent: String) {
            if (view.appContent != appContent) {
                view.appContent = appContent
            }
        }
    }
}