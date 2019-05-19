package com.wesail.balanceview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.library.R
import android.graphics.Bitmap.Config
import com.common.toBitmap

open class AxisView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var xPercent = 0f
    private var yPercent = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private val paintFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
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
        /*fun _A(){
            offsetX = event.x
            offsetY = event.y
            invalidate()
        }*/
        fun _A(){
            offsetX = event.x - measuredWidth / 2
            offsetY = event.y - measuredHeight / 2
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
        /*
        offsetX = (xPercent + 1) / 2f * measuredWidth.toFloat()
        offsetY = (yPercent + 1) / 2f * measuredHeight.toFloat()
        */
        poi?.also {point ->
            offsetX = xPercent * (measuredWidth - width) / 2
            offsetY = yPercent * (measuredHeight - height) / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        poi?.apply {
            val xLength = measuredWidth - width
            val yLength = measuredHeight  - height
            if (pointPositionByPrecentMap["isEnable"] == true) {
                val xp = pointPositionByPrecentMap["xp"] as Float
                val yp = pointPositionByPrecentMap["yp"] as Float
                offsetX = xp * (measuredWidth - width) / 2
                offsetY = yp * (measuredHeight - height) / 2
            }
            when {
                offsetX < -(measuredWidth - width) / 2 -> {
                    offsetX = -(measuredWidth - width).toFloat() / 2
                }
                offsetX > (measuredWidth - width) / 2 -> {
                    offsetX = (measuredWidth - width).toFloat() / 2
                }
            }
            when {
                offsetY < -(measuredHeight - height) / 2 -> {
                    offsetY = -(measuredHeight - height).toFloat() / 2
                }
                offsetY > (measuredHeight - height) / 2 -> {
                    offsetY = (measuredHeight - height).toFloat() / 2
                }
            }
            val left = offsetX  - width / 2
            val top = offsetY - height / 2

            val axisX = offsetX / xLength * axisLength
            val axisY = offsetY / yLength * axisLength

            setAxis(axisX.toInt(), axisY.toInt())

            canvas.translate(measuredWidth.toFloat() / 2,measuredHeight.toFloat() / 2)
            canvas.drawFilter = paintFilter
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
            onValueChangeListener?.onChange(axisX,axisY,axisLength / 2)
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
fun Drawable.toBitmap(wrapWidth: Int, wrapHigh: Int, ratio: Float = 1f, deg: Float = 0f) : Bitmap {
    val config = if (this.opacity != PixelFormat.OPAQUE)
        Bitmap.Config.ARGB_8888
    else
        Bitmap.Config.RGB_565
    val bitmap1: Bitmap = this.toBitmap(config = config)

    val bitmap2 = bitmap1.let {
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
