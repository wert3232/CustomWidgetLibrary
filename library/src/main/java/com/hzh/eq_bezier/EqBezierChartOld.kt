/*
package com.hzh.eq_bezier

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import androidx.core.graphics.xor
import androidx.core.net.toUri

open class EqBezierChart : View{
    var maxY = 100f
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.BLUE
        style = Paint.Style.STROKE
    }
    private val mPaintA = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.RED
        style = Paint.Style.STROKE

    }
    private val mPaintB = Paint().apply {
        isAntiAlias = true
        strokeWidth = 4f
        color = 0x5500FF00.toInt()
        style = Paint.Style.FILL
    }
    private val mPaintC = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = 0x55000000.toInt()
        style = Paint.Style.STROKE
    }
    private var canvas: Canvas ?= null
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)
    init {
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas
        maxY = (measuredHeight / 2) .toFloat()
*/
/*
        putEqPoint(2 * maxY,maxY,0.5f, 0xAAFF0000.toInt())
        putEqPoint((2 * 1.75 * maxY).toFloat(),maxY -25f,1f,  0xAA00FF00.toInt())
        putEqPoint((2 * 2.5 * maxY).toFloat(),maxY - 10f,0.5f,  0xAA0000FF.toInt())
        putEqPoint((2 * 3.25 * maxY).toFloat(), - maxY,0.5f)
*//*

        var p1 = createEqPath(2 * maxY,maxY,1f)
        var p2 = createEqPath((2 * 1.5 * maxY).toFloat(),maxY,1f)
        var p3 = createEqPath((2 * 2.0 * maxY).toFloat(),maxY,1f)
        var p4 = createEqPath((2 * 2.5 * maxY).toFloat(), - maxY,1f)
        canvas?.apply {
            canvas.translate(0f,(measuredHeight / 2) .toFloat())
            val zeroLineY = (measuredHeight / 2) .toFloat()
            var baseLine = Path().apply {
                moveTo(0f, 0f)
                lineTo(measuredWidth.toFloat(), 0f)
            }
            drawPath(p1 + p2 + p3 + p4, mPaintB)
            drawPath(baseLine, mPaint)
            */
/*drawPath(create3TBezier(2 * maxY,maxY,1f), mPaintA)
            drawPath(create3TBezier((2 * 1.5 * maxY).toFloat(),maxY,1f), mPaintA)
            drawPath(create3TBezier((2 * 2.0 * maxY).toFloat(),maxY,1f), mPaintA)
            drawPath(create3TBezier((2 * 2.5 * maxY).toFloat(),1f), mPaintA)*//*

            var eq1 = EqObj(2 * maxY.toInt(),maxY,1f, measuredWidth, height)
            var eq2 = EqObj((2 * 1.5 * maxY).toInt(),10f,1f, measuredWidth, height)
            var eq3 = EqObj((2 * 2.0 * maxY).toInt(), maxY,1f, measuredWidth, height)
            var eq4 = EqObj((2 * 2.5 * maxY).toInt(),- maxY,1f, measuredWidth, height)
            drawPath(eq1.getPath(),mPaintC)
            drawPath(eq2.getPath(),mPaintC)
            drawPath(eq3.getPath(),mPaintC)
            drawPath(eq4.getPath(),mPaintC)
            val p = EqObj.buildAndPath(arrayOf(eq1,eq2,eq3,eq4), measuredWidth, maxY)
            drawPath(p,mPaintA)
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
    fun createEqPath(px: Float, py: Float, q: Float = 1f) : Path{
        val zeroLineY = (measuredHeight / 2) .toFloat()
        var x = px
        var y = zeroLineY + ( - py) //因为canvas往下为正轴,往上为负轴,所以让py反过来
        val y2Max = maxY
        val d = q * y2Max / 2
        val p0 = EqPoint(x,y)
        val p1 = EqPoint(x - d, y)
        val p2 = EqPoint(x - d, zeroLineY)
        val p3 = EqPoint(x - 2 * d, zeroLineY)
        val p11 = EqPoint(x + d, y)
        val p22 = EqPoint(x + d, zeroLineY)
        val p33 = EqPoint(x + 2 * d, zeroLineY)
        return Path().apply {
            moveTo(p3.x,p3.y)
            cubicTo(p2.x, p2.y, p1.x, p1.y, p0.x, p0.y)
//            lineTo(p0.x,p0.y)
            cubicTo(p11.x, p11.y, p22.x, p22.y, p33.x, p33.y)
        }
    }


    fun create3TBezier(px: Float, py: Float, q: Float = 1f): Path{
        val zeroLineY = (measuredHeight / 2) .toFloat()
        var x = px
        var y = zeroLineY + ( - py) //因为canvas往下为正轴,往上为负轴,所以让py反过来
        val y2Max = maxY
        val d = q * y2Max / 2
        val p0 = EqPoint(x,y)
        val p1 = EqPoint((x - d), y)
        val p2 = EqPoint((x - d), zeroLineY)
        val p3 = EqPoint((x - 2 * d), zeroLineY)
        val p11 = EqPoint((x + d), y)
        val p22 = EqPoint((x + d), zeroLineY)
        val p33 = EqPoint((x + 2 * d), zeroLineY)
        val points = draw3TBezier(p3,p2,p1,p0).apply {
            addAll(draw3TBezier(p0,p11,p22,p33))
        }

        return Path().apply {
            moveTo(0f,zeroLineY)
            points.forEach {
                lineTo(it.x.toFloat(),it.y.toFloat())
            }
        }
    }
    fun draw3TBezier(p0: EqPoint, p1: EqPoint, p2: EqPoint, p3: EqPoint) : ArrayList<EqPoint> {
        var start = System.currentTimeMillis()
        var points = arrayListOf(p0,p1,p2,p3)
        var n = points.size
        var xs = arrayOfNulls<Float>(n - 1)
        var ys = arrayOfNulls<Float>(n - 1)
        var rPoints = ArrayList<EqPoint>()
        for(t in 0..20){
            var vt = t.toFloat() / 20
            for(i in 1 until n){
                for(j in 0 until n - i){
                    if(i == 1){//xarray[j] = arrayCoordinate.GetAt(j).x * (1 - t) + arrayCoordinate[j + 1].x * t;
                        xs[j] = points[j].x * (1 - vt) + points[j + 1].x * vt
                        ys[j] = points[j].y * (1 - vt) + points[j + 1].y * vt
                    }else{
                        xs[j] = xs[j]!! * (1 - vt) + xs[j + 1]!! * vt
                        ys[j] = ys[j]!! * (1 - vt) + ys[j + 1]!! * vt
                    }
                }
            }
            var x = xs[0]!!.toInt().toFloat()
            var y = ys[0]!!
            rPoints.add(EqPoint(x,y))
        }
        return rPoints
    }
}
data class EqPoint(var x: Float,var y: Float)*/
