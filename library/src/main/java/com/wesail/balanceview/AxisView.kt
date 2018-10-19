package com.wesail.balanceview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.library.R
open class AxisView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var xPercent = 0f
    private var yPercent = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private val pointPositionByPrecentMap = mutableMapOf<String,Any>(
            "isEnable" to false,
            "xp" to 0f,
            "yp" to 0f
    )
    var axisLength = 100
    var axisX = 0
    var axisY = 0
    var point: Drawable? = null
    var poi: Bitmap ?= null
    var poiSizePrecent = 0.2f
    private var onValueChangeListener: OnValueChangeListener? = null
    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.axisView)
        axisLength = a.getInt(R.styleable.axisView_axisLength, 100)
        axisX = a.getInt(R.styleable.axisView_axisX,0)
        axisY = a.getInt(R.styleable.axisView_axisY, 0)
        poiSizePrecent = a.getFloat(R.styleable.axisView_poiSizePrecent, 0.2f)
        point = a.getDrawable(R.styleable.axisView_pointDrawable) ?: ContextCompat.getDrawable(context,R.drawable.sound_point)
        a.recycle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        fun _A(){
            offsetX = event.x
            offsetY = event.y
            invalidate()
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                _A()
                onValueChangeListener?.onTouch()
            }
            MotionEvent.ACTION_MOVE -> {
                pointPositionByPrecentMap["isEnable"] = false
                _A()
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        /*offsetX = measuredWidth.toFloat() / 2
        offsetY = measuredHeight.toFloat() / 2*/
        offsetX = (xPercent + 1) / 2f * measuredWidth.toFloat()
        offsetY = (yPercent + 1) / 2f * measuredHeight.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        poi?.apply {
            if(pointPositionByPrecentMap["isEnable"] == true){
                val xp = pointPositionByPrecentMap["xp"] as Float
                val yp = pointPositionByPrecentMap["yp"] as Float
                offsetX = (xp + 1) / 2f * measuredWidth.toFloat()
                offsetY = (yp + 1) / 2f * measuredHeight.toFloat()
            }

            val xLength = canvas.width - width
            val yLength = canvas.height - height
            var left = (offsetX -  this.width.toFloat() / 2)
            if(left < 0){
                left = 0f
            }else if (left > xLength){
                left = xLength.toFloat()
            }
            var top = (offsetY -  this.height.toFloat() / 2)
            if(top < 0){
                top = 0f
            }else if (top > yLength){
                top = yLength.toFloat()
            }
            fun owo(){
                var unitX = (canvas.width - width).toFloat() / (axisLength * 2)
                var unitY = (canvas.height - height).toFloat() / (axisLength * 2)
                val axisX = (left / unitX).toInt() - axisLength
                val axisY = (top / unitY).toInt() - axisLength
                setAxis(axisX,axisY)
            }
            owo()
            canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(this, left , top,null)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        point?.apply {
            poi = toBitmap(measuredWidth,measuredHeight,poiSizePrecent)
        }
    }
    private fun setAxis(axisX: Int,axisY: Int){
        if(this.axisX != axisX || this.axisY != axisY){
            this.axisX = axisX
            this.axisY = axisY
            onValueChangeListener?.onChange(axisX,axisY,axisLength)
            xPercent = axisX.toFloat() / axisLength
            yPercent = axisY.toFloat() / axisLength
//            Log.e(javaClass.simpleName, "axisX: ${axisX}  axisY:${axisY}")
        }
    }
    // -1.0f ~ 1.0f, -1.0f ~ 1.0f
    fun setPointPosition(xp: Float = 0f , yp: Float = 0f,condition:() -> Boolean){
        if(condition.invoke()){
            pointPositionByPrecentMap["isEnable"] = true
            pointPositionByPrecentMap["xp"] = xp
            pointPositionByPrecentMap["yp"] = yp
            invalidate()
        }else{
            pointPositionByPrecentMap["isEnable"] = false
        }
    }
    fun setPointPosition(xp: Float = 0f , yp: Float = 0f){
        xPercent = xp
        yPercent = yp
        setPointPosition(xp,yp){
            true
        }
    }
    fun setValueChangeListener(onValueChangeListener: OnValueChangeListener){
        this.onValueChangeListener = onValueChangeListener
    }

    interface OnValueChangeListener{
        fun onChange(x:Int, y: Int,axisLength: Int) : Unit
        fun onTouch(){

        }
    }
}


//父宽度、父高度、缩放大小(以父长宽为基准) 、旋转角度
fun Drawable.toBitmap(wrapWidth: Int, wrapHigh: Int, ratio: Float = 1f, @Suppress("UNUSED_PARAMETER") deg: Float = 0f) : Bitmap {
    val config = if (this.opacity != PixelFormat.OPAQUE)
        Bitmap.Config.ARGB_8888
    else
        Bitmap.Config.RGB_565
    var bitmap1: Bitmap = this.toBitmap(config = config)

    var bitmap2 = bitmap1.let {
        if(wrapHigh<= 0 || wrapWidth <= 0 || it.width <= 0 || it.height <= 0) {
            return it
        }
        val datumLengh = if(wrapWidth > wrapHigh) wrapHigh.toFloat() else wrapWidth.toFloat()
        val datumRatio = if(it.width > it.height){
            datumLengh / it.width.toFloat()
        }else{
            datumLengh / it.height.toFloat()
        }
        val matrix = Matrix().apply {
            preScale(datumRatio * ratio, datumRatio * ratio)
        }
        return@let Bitmap.createBitmap(it,0,0,it.width, it.height, matrix,false)
    }
    return bitmap2
}