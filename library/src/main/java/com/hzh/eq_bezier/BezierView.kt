package com.hzh.eq_bezier

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View

import java.util.ArrayList

/**
 * Created by an
 */

class BezierView : View {
    private val mContext: Context
    private val lineSmoothness = 0.2f
    private val mPoints = ArrayList<Point>()
    private var mPath: Path? = null
    private var mAssistPath: Path? = null
    private val drawScale = 1f
    private var mPathMeasure: PathMeasure? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var offsetX: Int = 0
    private var offsetY: Int = 0
    private val XY = Point(0, 0)
    private var mLineEnd: Int = 0
    private var mLineStart: Int = 0
    private val mControls = ArrayList<Point>()
    private val currentPoint = Point(0, 0)
    private val lines = ArrayList<Line>()
    private val whichPoint = -1
    private val isDownOn = false
    private var mHasInit = false
    private val mPointsCount = 18

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        initPointList()
    }

    private fun initPointList() {
        if (mHasInit) return
        mHasInit = true


        initXY()

        //Log.i(TAG, "initPointList: getTop=$top")//0
        //Log.i(TAG, "initPointList: mHeight=$mHeight")//123
        //Log.i(TAG, "initPointList: offsetY=$offsetY")//0
        //Log.i(TAG, "initPointList: offsetX=$offsetX")//15

        //初始化点
        for (i in 0 until mPointsCount) {
            //            Point point = new Point((int) (1.0f * mWidth / (mPointsCount - 1) * i) + offsetX + XY.x, mHeight + XY.y - (int) ((1.0f * mHeight - offsetY) / (mPointsCount - 1) * i));
            val point = Point((1.0f * mWidth / (mPointsCount - 1) * i).toInt() + offsetX + XY.x, (mHeight * 0.5 + top.toDouble() + offsetY.toDouble()).toInt())
            mPoints.add(point)
        }

        mLineStart = mPoints[0].y
        mLineEnd = mPoints[mPoints.size - 1].y

        measurePath()
    }

    private fun initXY() {
        //        offsetX = getMeasuredWidth() / 17;
        //        offsetY = getMeasuredHeight() / 9;
        offsetY = 0
        offsetX = 15
        mWidth = measuredWidth - 2 * offsetX
        mHeight = 123//曲线的可滑动的高度的范围

//        Log.i(TAG, "initXY: offsetX=$offsetX")
//        Log.i(TAG, "initXY: offsetY=$offsetY")
//
//        Log.i(TAG, "initXY: mWidth=$mWidth")
//        Log.i(TAG, "initXY: mHeight=$mHeight")
    }


    override fun onDraw(canvas: Canvas) {
        if (mPoints == null)
            return
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE

        paint.isAntiAlias = true
        paint.isDither = true

        paint.color = Color.BLUE
        val dst = Path()
        dst.rLineTo(0f, 0f)
        val distance = mPathMeasure!!.length * drawScale
        if (mPathMeasure!!.getSegment(0f, distance, dst, true)) {
            val pos = FloatArray(2)
            mPathMeasure!!.getPosTan(distance, pos, null)
            //绘制点
            drawPoint(canvas, pos)
            //绘制线
            canvas.drawPath(dst, paint)
        }
    }

    /**
     * 绘制点
     *
     * @param canvas
     * @param pos
     */
    private fun drawPoint(canvas: Canvas, pos: FloatArray) {
        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 1f
        redPaint.style = Paint.Style.FILL
        for (i in mPoints.indices) {
            val point = mPoints[i]
            if (i == 0 || i == mPoints.size - 1) {
                continue
            }
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 0f, redPaint)//8:表示控制点的大小
        }
    }

    /**
     * 根据x坐标获取单个Y坐标
     */

    //    private int getSingle(int x) {
    //        int y = 0;
    //        double t = 0;
    //        int p1 = 0, p2 = 0, p3 = 0, p4 = 0;
    //        double tem1 = 0, tem2 = 2;
    //        for (int i = 0; i < lines.size(); i++) {
    //            if (x >= mPoints.get(i).x && x < mPoints.get(i + 1).x) {
    //                p1 = lines.get(i).start.y;
    //                p2 = lines.get(i).control1.y;
    //                p3 = lines.get(i).control2.y;
    //                p4 = lines.get(i).end.y;
    //                tem1 = (x - mPoints.get(i).x);
    //                tem2 = (mPoints.get(i + 1).x - mPoints.get(i).x);
    //            }
    //        }
    //
    //        t = tem1 / tem2;
    //        y = (int) (p1 * Math.pow(1 - t, 3) + 3 * p2 * t * Math.pow(1 - t, 2) + 3 * p3 * Math.pow(t, 2) * (1 - t) + p4 * Math.pow(t, 3));
    //        if (y > mPoints.get(0).y) {
    //            y = mPoints.get(0).y;
    //        } else if (y < mPoints.get(mPoints.size() - 1).y) {
    //            y = mPoints.get(mPoints.size() - 1).y;
    //        }
    //        return y;
    //    }

    /**
     * 路经测量
     */
    private fun measurePath() {
        lines.clear()
        mControls.clear()
        mPath = Path()
        mAssistPath = Path()
        var prePreviousPointX = java.lang.Float.NaN
        var prePreviousPointY = java.lang.Float.NaN
        var previousPointX = java.lang.Float.NaN
        var previousPointY = java.lang.Float.NaN
        var currentPointX = java.lang.Float.NaN
        var currentPointY = java.lang.Float.NaN
        var nextPointX: Float
        var nextPointY: Float

        val lineSize = mPoints.size
        for (valueIndex in 0 until lineSize) {
            if (java.lang.Float.isNaN(currentPointX)) {
                val point = mPoints[valueIndex]
                currentPointX = point.x.toFloat()
                currentPointY = point.y.toFloat()
            }
            if (java.lang.Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    val point = mPoints[valueIndex - 1]
                    previousPointX = point.x.toFloat()
                    previousPointY = point.y.toFloat()
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX
                    previousPointY = currentPointY
                }
            }

            if (java.lang.Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    val point = mPoints[valueIndex - 2]
                    prePreviousPointX = point.x.toFloat()
                    prePreviousPointY = point.y.toFloat()
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX
                    prePreviousPointY = previousPointY
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                val point = mPoints[valueIndex + 1]
                nextPointX = point.x.toFloat()
                nextPointY = point.y.toFloat()
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX
                nextPointY = currentPointY
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                mPath!!.moveTo(currentPointX, currentPointY)//起点
                mAssistPath!!.moveTo(currentPointX, currentPointY)
            } else {
                // 求出控制点坐标
                val firstDiffX = currentPointX - prePreviousPointX
                val firstDiffY = currentPointY - prePreviousPointY
                val secondDiffX = nextPointX - previousPointX
                val secondDiffY = nextPointY - previousPointY
                val firstControlPointX = previousPointX + lineSmoothness * firstDiffX
                val firstControlPointY = previousPointY + lineSmoothness * firstDiffY
                val secondControlPointX = currentPointX - lineSmoothness * secondDiffX
                val secondControlPointY = currentPointY - lineSmoothness * secondDiffY
                mPath!!.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY)
                lines.add(Line(
                        Point(previousPointX.toInt(),
                        previousPointY.toInt()),
                        Point(firstControlPointX.toInt(), firstControlPointY.toInt()),
                        Point(secondControlPointX.toInt(), secondControlPointY.toInt()),
                        Point(currentPointX.toInt(), currentPointY.toInt())
                ))
                //将控制点保存到辅助路径上
                mAssistPath!!.lineTo(firstControlPointX, firstControlPointY)
                mAssistPath!!.lineTo(secondControlPointX, secondControlPointY)
                mAssistPath!!.lineTo(currentPointX, currentPointY)
            }

            // 更新值,
            prePreviousPointX = previousPointX
            prePreviousPointY = previousPointY
            previousPointX = currentPointX
            previousPointY = currentPointY
            currentPointX = nextPointX
            currentPointY = nextPointY
        }
        mPathMeasure = PathMeasure(mPath, false)
    }
    //根据seekbar设置
    fun setPoints(index: Int, percent: Float) {

        //Log.d(TAG, "setPoints: index $index  percent   $percent")

        val currentPoint = mPoints[index]
        //        float y = getTop() + offsetY + (mHeight * (1 - percent));
        var y = offsetY + mHeight * (1 - percent)

       /* Log.i(TAG, "setPoints: getTop=$top")//6
        Log.i(TAG, "setPoints: offsetY=$offsetY")//0
        Log.i(TAG, "setPoints: mHeight=$mHeight")//123*/
        //        y = y > mHeight + getTop() ? mHeight + getTop() : y < offsetY + getTop() ? offsetY + getTop() : y;
       if (y > mHeight + top) {
            y = (mHeight + top).toFloat()
        } else if (y < offsetY + top) {
            y = (offsetY + top).toFloat()
        } else {
           y = y //曲线可滑动有效范围
       }
        currentPoint.set(currentPoint.x, y.toInt())
        invalidate()
        measurePath()
    }
    class Line constructor(private val start: Point, private val control1: Point, private val control2: Point, private val end: Point)

    companion object {
        private val TAG = BezierView::class.java.simpleName
    }

}