package com.baby.viewtools

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.library.R

import java.util.Arrays

/**
 * 波浪侧边栏
 */
class WaveSideBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {
    private var lettersOnChangeListener = { letter: String ->}

    // 渲染字母表
    private var mLetters: List<String>? = null

    // 当前选中的位置
    private var mChoosePosition = -1

    private var mOldPosition: Int = 0

    private var mNewPosition: Int = 0

    // 字母列表画笔
    private val mLettersPaint = Paint()

    // 提示字母画笔
    private val mTextPaint = Paint()
    // 波浪画笔
    private var mWavePaint = Paint()

    private var mTextSize: Int = 0
    private var mHintTextSize: Int = 0
    private var mTextColor: Int = 0
    private var mWaveColor: Int = 0
    private var plateColor: Int = 0
    private var mTextColorChoose: Int = 0
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mItemHeight: Int = 0
    private var mPadding: Int = 0

    // 波浪路径
    private val mWavePath = Path()

    // 圆形路径
    private val mCirclePath = Path()

    // 手指滑动的Y点作为中心点
    private var mCenterY: Int = 0 //中心点Y

    // 贝塞尔曲线的分布半径
    private var mRadius: Int = 0

    // 圆形半径
    private var mCircleRadius: Int = 0
    // 用于过渡效果计算
    private var mRatioAnimator: ValueAnimator? = null

    // 用于绘制贝塞尔曲线的比率
    private var mRatio: Float = 0.toFloat()

    // 选中字体的坐标
    private var mPointX: Float = 0.toFloat()
    private var mPointY: Float = 0.toFloat()

    // 圆形中心点X
    private var mCircleCenterX: Float = 0.toFloat()

    var letters: List<String>?
        get() = mLetters
        set(letters) {
            this.mLetters = letters
            invalidate()
        }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mLetters = Arrays.asList(*context.resources.getStringArray(R.array.sideBarLetters))
        mPadding = context.resources.getDimensionPixelSize(R.dimen.padding)
        @SuppressLint("CustomViewStyleable")
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.commonAttr)
        mTextColor = a.getColor(R.styleable.commonAttr_appTextColor, Color.parseColor("#969696"))
        mTextColorChoose = a.getColor(R.styleable.commonAttr_appChooseTextColor, ContextCompat.getColor(context, android.R.color.white))
        mTextSize = a.getDimensionPixelSize(R.styleable.commonAttr_appTextSize, context.resources.getDimensionPixelSize(R.dimen.textSize))
        mHintTextSize = a.getDimensionPixelSize(R.styleable.commonAttr_appHintTextSize, context.resources.getDimensionPixelSize(R.dimen.hintTextSize))
        mWaveColor = a.getColor(R.styleable.commonAttr_appBackgroundColor, Color.parseColor("#bef9b81b"))
        plateColor = a.getColor(R.styleable.commonAttr_appPlateColor, Color.parseColor("#F9F9F9"))
        mRadius = a.getDimensionPixelSize(R.styleable.commonAttr_appRadius, context.resources.getDimensionPixelSize(R.dimen.radius))
        mCircleRadius = a.getDimensionPixelSize(R.styleable.commonAttr_appCircleRadius, context.resources.getDimensionPixelSize(R.dimen.circleRadius))
        a.recycle()

        mWavePaint = Paint()
        mWavePaint.isAntiAlias = true
        mWavePaint.style = Paint.Style.FILL
        mWavePaint.color = mWaveColor

        mTextPaint.isAntiAlias = true
        mTextPaint.color = mTextColorChoose
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = mHintTextSize.toFloat()
        mTextPaint.textAlign = Paint.Align.CENTER
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        val x = event.x
        mOldPosition = mChoosePosition
        mNewPosition = (y / mHeight * mLetters!!.size).toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //限定触摸范围
                if (x < mWidth - 1.5 * mRadius) {
                    return false
                }
                mCenterY = y.toInt()
                startAnimator(1.0f)
            }
            MotionEvent.ACTION_MOVE -> {

                mCenterY = y.toInt()
                if (mOldPosition != mNewPosition) {
                    if (mNewPosition >= 0 && mNewPosition < mLetters!!.size) {
                        mChoosePosition = mNewPosition
                        lettersOnChangeListener.invoke(mLetters!![mNewPosition])
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {

                startAnimator(0f)
                mChoosePosition = -1
            }
            else -> {
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        mWidth = measuredWidth
        mItemHeight = (mHeight - mPadding) / mLetters!!.size
        mPointX = mWidth - 1.6f * mTextSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制字母列表
        drawLetters(canvas)

        //绘制波浪
        drawWavePath(canvas)

        //绘制圆
        drawCirclePath(canvas)

        //绘制选中的字体
        drawChooseText(canvas)

    }

    /**
     * 绘制字母列表
     *
     * @param canvas
     */
    private fun drawLetters(canvas: Canvas) {

        val rectF = RectF()
        rectF.left = mPointX - mTextSize
        rectF.right = mPointX + mTextSize
        rectF.top = (mTextSize / 2).toFloat()
        rectF.bottom = (mHeight - mTextSize / 2).toFloat()

        mLettersPaint.reset()
        mLettersPaint.style = Paint.Style.FILL
        mLettersPaint.color = plateColor
        mLettersPaint.isAntiAlias = true
        canvas.drawRoundRect(rectF, mTextSize.toFloat(), mTextSize.toFloat(), mLettersPaint)

        mLettersPaint.reset()
        mLettersPaint.style = Paint.Style.STROKE
        mLettersPaint.color = mTextColor
        mLettersPaint.isAntiAlias = true
        canvas.drawRoundRect(rectF, mTextSize.toFloat(), mTextSize.toFloat(), mLettersPaint)

        for (i in mLetters!!.indices) {
            mLettersPaint.reset()
            mLettersPaint.color = mTextColor
            mLettersPaint.isAntiAlias = true
            mLettersPaint.textSize = mTextSize.toFloat()
            mLettersPaint.textAlign = Paint.Align.CENTER

            val fontMetrics = mLettersPaint.fontMetrics
            val baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top)

            val pointY = (mItemHeight * i).toFloat() + baseline / 2 + mPadding.toFloat()

            if (i == mChoosePosition) {
                mPointY = pointY
            } else {
                canvas.drawText(mLetters!![i], mPointX, pointY, mLettersPaint)
            }
        }

    }

    /**
     * 绘制选中的字母
     *
     * @param canvas
     */
    private fun drawChooseText(canvas: Canvas) {
        if (mChoosePosition != -1) {
            // 绘制右侧选中字符
            mLettersPaint.reset()
            mLettersPaint.color = mTextColorChoose
            mLettersPaint.textSize = mTextSize.toFloat()
            mLettersPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(mLetters!![mChoosePosition], mPointX, mPointY, mLettersPaint)

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                val target = mLetters!![mChoosePosition]
                val fontMetrics = mTextPaint.fontMetrics
                val baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top)
                val x = mCircleCenterX
                val y = mCenterY + baseline / 2
                canvas.drawText(target, x, y, mTextPaint)
            }
        }
    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private fun drawWavePath(canvas: Canvas) {
        mWavePath.reset()
        // 移动到起始点
        mWavePath.moveTo(mWidth.toFloat(), (mCenterY - 3 * mRadius).toFloat())
        //计算上部控制点的Y轴位置
        val controlTopY = mCenterY - 2 * mRadius

        //计算上部结束点的坐标
        val endTopX = (mWidth - mRadius.toDouble() * Math.cos(ANGLE) * mRatio.toDouble()).toInt()
        val endTopY = (controlTopY + mRadius * Math.sin(ANGLE)).toInt()
        mWavePath.quadTo(mWidth.toFloat(), controlTopY.toFloat(), endTopX.toFloat(), endTopY.toFloat())

        //计算中心控制点的坐标
        val controlCenterX = (mWidth - 1.8 * mRadius.toDouble() * Math.sin(ANGLE_R) * mRatio.toDouble()).toInt()
        val controlCenterY = mCenterY
        //计算下部结束点的坐标
        val controlBottomY = mCenterY + 2 * mRadius
        val endBottomY = (controlBottomY - mRadius * Math.cos(ANGLE)).toInt()
        mWavePath.quadTo(controlCenterX.toFloat(), controlCenterY.toFloat(), endTopX.toFloat(), endBottomY.toFloat())

        mWavePath.quadTo(mWidth.toFloat(), controlBottomY.toFloat(), mWidth.toFloat(), (controlBottomY + mRadius).toFloat())

        mWavePath.close()
        canvas.drawPath(mWavePath, mWavePaint)
    }

    /**
     * 绘制左边提示的圆
     *
     * @param canvas
     */
    private fun drawCirclePath(canvas: Canvas) {
        //x轴的移动路径
        mCircleCenterX = mWidth + mCircleRadius - (2.0f * mRadius + 2.0f * mCircleRadius) * mRatio

        mCirclePath.reset()
        mCirclePath.addCircle(mCircleCenterX, mCenterY.toFloat(), mCircleRadius.toFloat(), Path.Direction.CW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mCirclePath.op(mWavePath, Path.Op.DIFFERENCE)
        }

        mCirclePath.close()
        canvas.drawPath(mCirclePath, mWavePaint)

    }


    private fun startAnimator(value: Float) {
        if (mRatioAnimator == null) {
            mRatioAnimator = ValueAnimator()
        }
        mRatioAnimator!!.cancel()
        mRatioAnimator!!.setFloatValues(value)
        mRatioAnimator!!.addUpdateListener { value ->
            mRatio = value.animatedValue as Float
            //球弹到位的时候，并且点击的位置变了，即点击的时候显示当前选择位置
            if (mRatio == 1f && mOldPosition != mNewPosition) {
                if (mNewPosition >= 0 && mNewPosition < mLetters!!.size) {
                    mChoosePosition = mNewPosition
                    lettersOnChangeListener.invoke(mLetters!![mNewPosition])
                }
            }
            invalidate()
        }
        mRatioAnimator!!.start()
    }


    fun setOnTouchLetterChangeListener(lettersOnChange: (String) -> Unit) {
        this.lettersOnChangeListener = lettersOnChange
    }


    companion object {

        private val TAG = "WaveSideBar"

        // 计算波浪贝塞尔曲线的角弧长值
        private val ANGLE = Math.PI * 45 / 180
        private val ANGLE_R = Math.PI * 90 / 180
    }
}