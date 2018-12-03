package com.github.shchurov.horizontalwheelview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.databinding.InverseBindingListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

import java.lang.Math.PI
import com.library.R
import java.util.*

class AntiHorizontalWheelView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    val a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView)
    val commonAttr = context.obtainStyledAttributes(attrs, R.styleable.commonAttr)
    private var startIndex = a.getInt(R.styleable.HorizontalWheelView_startIndex, 0)
    private var endIndex = a.getInt(R.styleable.HorizontalWheelView_endIndex, 10) + 1
    private var isAnti = commonAttr.getBoolean(R.styleable.commonAttr_isAnti, false)
    private val touchHandler = AntiTouchHandler(this)
    private var angle: Double = 0.toDouble()
        set(value) {
            if (field == value) {
            } else {
                field = value
            }
        }
    var onlyPositiveValues: Boolean = a.getBoolean(R.styleable.HorizontalWheelView_onlyPositiveValues, DEFAULT_ONLY_POSITIVE_VALUES)
    var endLock: Boolean = a.getBoolean(R.styleable.HorizontalWheelView_endLock, DEFAULT_END_LOCK)
    private var listener: onChangeListener? = null
    //值越大越慢
    private var scaleSpeedUnit = a.getInt(R.styleable.HorizontalWheelView_scaleSpeedUnit, 20)
    var inverseBindingListener: InverseBindingListener? = null
    var viewIndex = a.getInt(R.styleable.HorizontalWheelView_index, startIndex)
        set(value) {
            if (field == value) {

            } else {
                field = value
                angle = if(isAnti){
                        -(value * (2 * PI) / (endIndex - startIndex))
                    }else{
                        value * (2 * PI) / (endIndex - startIndex)
                    }
                invalidate()
                //Log.e("Test","radians:$angle      $value")
                listener?.onRotationChanged(this.angle, value)
                inverseBindingListener?.onChange()
            }
        }
    var radiansAngle: Double
        get() = angle
        set(radians) {
            if (!checkEndLock(radians)) {
                angle = radians % (2 * PI)
            }
            if(isAnti){
                if (onlyPositiveValues && angle > 0) {
                    angle -= 2 * PI
                }
                viewIndex = -(this.angle * (endIndex - startIndex).toFloat() / (2 * PI)).toInt()
            }else{
                if (onlyPositiveValues && angle < 0) {
                    angle += 2 * PI
                }
                viewIndex = (this.angle * (endIndex - startIndex).toFloat() / (2 * PI)).toInt()
            }
        }

    var degreesAngle: Double
        get() = radiansAngle * 180 / PI
        set(degrees) {
            val radians = degrees * PI / 180
            radiansAngle = radians
        }

    var completeTurnFraction: Double
        get() = radiansAngle / (2 * PI)
        set(fraction) {
            val radians = fraction * 2.0 * PI
            radiansAngle = radians
        }
    var scaleAction: (Int) -> Unit = {}
    private var isZeroShow = true
    private var isCursorShow = a.getBoolean(R.styleable.HorizontalWheelView_isCursorShow, true)
    private var normalMarkLengthRatio = a.getFloat(R.styleable.HorizontalWheelView_normalMarkLengthRatio, 0.6f)
    private var zeroMarkLengthRatio = a.getFloat(R.styleable.HorizontalWheelView_zeroMarkLengthRatio, 0.8f)
    private var zeroMarkColor = a.getColor(R.styleable.HorizontalWheelView_zeroMarkColor, -1)
    private var lineSpaceRatio = a.getFloat(R.styleable.HorizontalWheelView_lineSpaceRatio, 0f)

    private var isCursorSpace = a.getBoolean(R.styleable.HorizontalWheelView_isCursorSpace, true)
    private var isZeroSpace = a.getBoolean(R.styleable.HorizontalWheelView_isZeroSpace, true)

    var normalColor: Int = a.getColor(R.styleable.HorizontalWheelView_normalColor, DEFAULT_NORMAL_COLOR)
        set(value) {
            field = value
            invalidate()
        }
    var activeColor = a.getColor(R.styleable.HorizontalWheelView_activeColor, DEFAULT_ACTIVE_COLOR)
        set(value) {
            field = value
            invalidate()
        }
    var marksCount: Int = DEFAULT_MARKS_COUNT
        set(marksCount) {
            field = marksCount
            maxVisibleMarksCount = marksCount / 2 + 1
            gaps = FloatArray(maxVisibleMarksCount)
            shades = FloatArray(maxVisibleMarksCount)
            scales = FloatArray(maxVisibleMarksCount)
            invalidate()
        }
    private var maxVisibleMarksCount = (marksCount / 2) + 1
    private var gaps = FloatArray(maxVisibleMarksCount)
    private var shades = FloatArray(maxVisibleMarksCount)
    private var scales = FloatArray(maxVisibleMarksCount)
    private var showActiveRange = a.getBoolean(R.styleable.HorizontalWheelView_showActiveRange, DEFAULT_SHOW_ACTIVE_RANGE)
    private var normalMarkWidth: Int = convertToPx(a.getInt(R.styleable.HorizontalWheelView_normalMarkWidth, DP_NORMAL_MARK_WIDTH))
    private var zeroMarkWidth = convertToPx(DP_ZERO_MARK_WIDTH)
    private var cursorCornersRadius = convertToPx(DP_CURSOR_CORNERS_RADIUS)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var viewportHeight:Int = 0
    private var normalMarkHeight :Int = 0
    private var zeroMarkHeight :Int = 0
    private val cursorRect = RectF()
    private var cursorRect_top: RectF = RectF()
    private var cursorRect_bottom: RectF = RectF()
    private val colorSwitches = intArrayOf(-1, -1, -1)
    init {
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet) {
        marksCount = a.getInt(R.styleable.HorizontalWheelView_marksCount, DEFAULT_MARKS_COUNT)
        val snapToMarks = a.getBoolean(R.styleable.HorizontalWheelView_snapToMarks, DEFAULT_SNAP_TO_MARKS)
        touchHandler.setSnapToMarks(snapToMarks)
        a.recycle()
        commonAttr.recycle()
    }

    fun setChangeListener(listener: onChangeListener) {
        this.listener = listener
        touchHandler.setChangeListener(listener)
    }

    fun setChangeListener(onRotationChanged: (radians: Double, index: Int) -> Unit,
                          onScrollStateChanged: (state: Int) -> Unit = {},
                          onTouch: () -> Unit = {}
    ) {
        setChangeListener(
                object : onChangeListener {
                    override fun onRotationChanged(radians: Double, index: Int) {
                        onRotationChanged(radians, index)
                    }

                    override fun onScrollStateChanged(state: Int) {
                        onScrollStateChanged(state)
                    }

                    override fun onTouch() {
                        onTouch()
                    }
                }
        )
    }


    private fun checkEndLock(radians: Double): Boolean {
        if (!endLock) {
            return false
        }
        var hit = false
        if (radians >= 2 * PI) {
            angle = Math.nextAfter(2 * PI, java.lang.Double.NEGATIVE_INFINITY)
            hit = true
        } else if (onlyPositiveValues && !isAnti && radians < 0) {
            angle = 0.0
            hit = true
        } else if (onlyPositiveValues && isAnti && radians > 0) {
            angle = 0.0
            hit = true
        } else if (radians <= -2 * PI) {
            angle = Math.nextAfter(-2 * PI, java.lang.Double.POSITIVE_INFINITY)
            hit = true
        }
        if (hit) {
            touchHandler.cancelFling()
        }
        return hit
    }



    fun setSnapToMarks(snapToMarks: Boolean) {
        touchHandler.setSnapToMarks(snapToMarks)
    }

    private var scale = 0f
    fun onDistanceChange(distanceX: Float, distanceY: Float) {
        if (Math.abs(distanceX) < scaleSpeedUnit) {
            scale += distanceX
            if (Math.abs(scale) > scaleSpeedUnit) {
                scaleAction((scale / scaleSpeedUnit).toInt())
                scale = 0f
            }
        } else {
            scaleAction((distanceX / scaleSpeedUnit).toInt())
            scale = 0f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            listener?.onTouch()
        }
        return touchHandler.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewportHeight = height - paddingTop - paddingBottom
        normalMarkHeight = (viewportHeight * normalMarkLengthRatio).toInt()
        zeroMarkHeight = (viewportHeight * zeroMarkLengthRatio).toInt()
        setupCursorRect()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val resolvedWidthSpec = resolveMeasureSpec(widthMeasureSpec, DP_DEFAULT_WIDTH)
        val resolvedHeightSpec = resolveMeasureSpec(heightMeasureSpec, DP_DEFAULT_HEIGHT)
        super.onMeasure(resolvedWidthSpec, resolvedHeightSpec)
    }

    private fun resolveMeasureSpec(measureSpec: Int, dpDefault: Int): Int {
        val mode = View.MeasureSpec.getMode(measureSpec)
        if (mode == View.MeasureSpec.EXACTLY) {
            return measureSpec
        }
        var defaultSize = Utils.convertToPx(dpDefault, resources)
        if (mode == View.MeasureSpec.AT_MOST) {
            defaultSize = Math.min(defaultSize, View.MeasureSpec.getSize(measureSpec))
        }
        return View.MeasureSpec.makeMeasureSpec(defaultSize, View.MeasureSpec.EXACTLY)
    }

    override fun onDraw(canvas: Canvas) {
        isZeroShow = isCursorShow
        val step = 2 * PI / marksCount
        var offset = (PI / 2 - radiansAngle) % step
        if (offset < 0) {
            offset += step
        }
        setupGaps(step, offset)
        setupShadesAndScales(step, offset)
        val zeroIndex = calcZeroIndex(step)
        setupColorSwitches(step, offset, zeroIndex)
        drawMarks(canvas, zeroIndex)
        if (isCursorShow) {
            drawCursor(canvas)
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.angle = angle
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        angle = ss.angle
        invalidate()
    }

    //draw

    private fun convertToPx(dp: Int): Int {
        return Utils.convertToPx(dp, resources)
    }

    private fun setupCursorRect() {
        val cursorHeight = (viewportHeight * CURSOR_RELATIVE_HEIGHT).toInt()
        val cursorWidth = convertToPx(DP_CURSOR_WIDTH)
        val top = (paddingTop + (viewportHeight - cursorHeight) / 2).toFloat()
        val bottom = top + cursorHeight
        val left = ((width - cursorWidth) / 2).toFloat()
        val right = left + cursorWidth
        cursorRect.top = top
        cursorRect.bottom = bottom
        cursorRect.left = left
        cursorRect.right = right
        if (isCursorSpace && lineSpaceRatio > 0 && lineSpaceRatio <= 1) {
            val spaceLength = viewportHeight * lineSpaceRatio

            cursorRect_top.left = left
            cursorRect_top.right = right
            cursorRect_top.top = top
            cursorRect_top.bottom = (viewportHeight - spaceLength) / 2

            cursorRect_bottom.left = left
            cursorRect_bottom.right = right
            cursorRect_bottom.top = (viewportHeight + spaceLength) / 2
            cursorRect_bottom.bottom = bottom
        }
    }

    private fun setupGaps(step: Double, offset: Double) {
        gaps[0] = Math.sin(offset / 2).toFloat()
        var sum = gaps[0]
        var angle = offset
        var n = 1
        while (angle + step <= PI) {
            gaps[n] = Math.sin(angle + step / 2).toFloat()
            sum += gaps[n]
            angle += step
            n++
        }
        val lastGap = Math.sin((PI + angle) / 2).toFloat()
        sum += lastGap
        if (n != gaps.size) {
            gaps[gaps.size - 1] = -1f
        }
        val k = width / sum
        for (i in gaps.indices) {
            if (gaps[i] != -1f) {
                gaps[i] *= k
            }
        }
    }

    private fun setupShadesAndScales(step: Double, offset: Double) {
        var angle = offset
        for (i in 0 until maxVisibleMarksCount) {
            val sin = Math.sin(angle)
            shades[i] = (1 - SHADE_RANGE * (1 - sin)).toFloat()
            scales[i] = (1 - SCALE_RANGE * (1 - sin)).toFloat()
            angle += step
        }
    }

    private fun calcZeroIndex(step: Double): Int {
        val twoPi = 2 * PI
        val normalizedAngle = (radiansAngle + PI / 2 + twoPi) % twoPi
        return if (normalizedAngle > PI) {
            -1
        } else ((PI - normalizedAngle) / step).toInt()
    }

    private fun setupColorSwitches(step: Double, offset: Double, zeroIndex: Int) {
        if (!showActiveRange) {
            Arrays.fill(colorSwitches, -1)
            return
        }
        val angle = radiansAngle
        var afterMiddleIndex = 0
        if (offset < PI / 2) {
            afterMiddleIndex = ((PI / 2 - offset) / step).toInt() + 1
        }
        if (angle > 3 * PI / 2) {
            colorSwitches[0] = 0
            colorSwitches[1] = afterMiddleIndex
            colorSwitches[2] = zeroIndex
        } else if (angle >= 0) {
            colorSwitches[0] = Math.max(0, zeroIndex)
            colorSwitches[1] = afterMiddleIndex
            colorSwitches[2] = -1
        } else if (angle < -3 * PI / 2) {
            colorSwitches[0] = 0
            colorSwitches[1] = zeroIndex
            colorSwitches[2] = afterMiddleIndex
        } else if (angle < 0) {
            colorSwitches[0] = afterMiddleIndex
            colorSwitches[1] = zeroIndex
            colorSwitches[2] = -1
        }
    }

    private fun drawMarks(canvas: Canvas, zeroIndex: Int) {
        var x = paddingLeft.toFloat()
        var color = normalColor
        var colorPointer = 0
        for (i in gaps.indices) {
            if (gaps[i] == -1f) {
                break
            }
            x += gaps[i]
            while (colorPointer < 3 && i == colorSwitches[colorPointer]) {
                color = if (color == normalColor) activeColor else normalColor
                colorPointer++
            }
            if (i != zeroIndex) {
                drawNormalMark(canvas, x, scales[i], shades[i], color)
            } else if (!isZeroShow) {
                drawNormalMark(canvas, x, scales[i], shades[i], color)
            } else {
                drawZeroMark(canvas, x, scales[i], shades[i])
            }
        }
    }

    private fun drawNormalMark(canvas: Canvas, x: Float, scale: Float, shade: Float, color: Int) {
        val height = normalMarkHeight * scale
        val top = paddingTop + (viewportHeight - height) / 2
        val bottom = top + height
        paint.setStrokeWidth(normalMarkWidth.toFloat())
        paint.setColor(applyShade(color, shade))
        if (lineSpaceRatio > 0 && lineSpaceRatio <= 1) {
            val spaceLength = viewportHeight * lineSpaceRatio
            canvas.drawLine(x, top, x, (viewportHeight - spaceLength) / 2, paint)
            canvas.drawLine(x, (viewportHeight + spaceLength) / 2, x, bottom, paint)
        } else {
            canvas.drawLine(x, top, x, bottom, paint)
        }
    }

    private fun drawZeroMark(canvas: Canvas, x: Float, scale: Float, shade: Float) {
        val height = zeroMarkHeight * scale
        val top = paddingTop + (viewportHeight - height) / 2
        val bottom = top + height
        paint.setStrokeWidth(zeroMarkWidth.toFloat())
        paint.setColor(applyShade(if (zeroMarkColor == -1) activeColor else zeroMarkColor, shade))
        if (isZeroSpace && lineSpaceRatio > 0 && lineSpaceRatio <= 1) {
            val spaceLength = viewportHeight * lineSpaceRatio
            canvas.drawLine(x, top, x, (viewportHeight - spaceLength) / 2, paint)
            canvas.drawLine(x, (viewportHeight + spaceLength) / 2, x, bottom, paint)
        } else {
            canvas.drawLine(x, top, x, bottom, paint)
        }
    }

    private fun drawCursor(canvas: Canvas) {
        paint.setStrokeWidth(0f)
        paint.setColor(if (zeroMarkColor == -1) activeColor else zeroMarkColor)
        if (isCursorSpace && lineSpaceRatio > 0 && lineSpaceRatio <= 1) {
            canvas.drawRoundRect(cursorRect_top, cursorCornersRadius.toFloat(), cursorCornersRadius.toFloat(), paint)
            canvas.drawRoundRect(cursorRect_bottom, cursorCornersRadius.toFloat(), cursorCornersRadius.toFloat(), paint)
        } else {
            canvas.drawRoundRect(cursorRect, cursorCornersRadius.toFloat(), cursorCornersRadius.toFloat(), paint)
        }
    }

    private fun applyShade(color: Int, shade: Float): Int {
        val r = (Color.red(color) * shade).toInt()
        val g = (Color.green(color) * shade).toInt()
        val b = (Color.blue(color) * shade).toInt()
        return Color.rgb(r, g, b)
    }

    companion object {
        private val DP_CURSOR_CORNERS_RADIUS = 1
        private val DP_NORMAL_MARK_WIDTH = 1
        private val DP_ZERO_MARK_WIDTH = 2
        private val DP_CURSOR_WIDTH = 3
        private val CURSOR_RELATIVE_HEIGHT = 1f
        private val SHADE_RANGE = 0.7f
        private val SCALE_RANGE = 0.1f

        private val DP_DEFAULT_WIDTH = 200
        private val DP_DEFAULT_HEIGHT = 32
        private val DEFAULT_MARKS_COUNT = 40
        private val DEFAULT_NORMAL_COLOR = -0x1
        private val DEFAULT_ACTIVE_COLOR = -0xab5310
        private val DEFAULT_SHOW_ACTIVE_RANGE = true
        private val DEFAULT_SNAP_TO_MARKS = false
        private val DEFAULT_END_LOCK = false
        private val DEFAULT_ONLY_POSITIVE_VALUES = false

        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SETTLING = 2
    }
}

internal class AntiTouchHandler(private val view: AntiHorizontalWheelView) : GestureDetector.SimpleOnGestureListener() {
    private var listener: onChangeListener? = null
    private val gestureDetector: GestureDetector
    private var settlingAnimator: ValueAnimator? = null
    private var snapToMarks: Boolean = false
    private var scrollState = AntiHorizontalWheelView.SCROLL_STATE_IDLE

    private val flingAnimatorListener = ValueAnimator.AnimatorUpdateListener { animation ->
        view.radiansAngle = (animation.animatedValue as Float).toDouble()
    }

    private val animatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            updateScrollStateIfRequired(AntiHorizontalWheelView.SCROLL_STATE_IDLE)
        }
    }

    init {
        gestureDetector = GestureDetector(view.context, this)
    }

    fun setChangeListener(listener: onChangeListener) {
        this.listener = listener
    }

    fun setSnapToMarks(snapToMarks: Boolean) {
        this.snapToMarks = snapToMarks
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        val action = event.actionMasked
        if (scrollState != AntiHorizontalWheelView.SCROLL_STATE_SETTLING && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            if (snapToMarks) {
                playSettlingAnimation(findNearestMarkAngle(view.radiansAngle))
            } else {
                updateScrollStateIfRequired(AntiHorizontalWheelView.SCROLL_STATE_IDLE)
            }
        }
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        cancelFling()
        return true
    }

    fun cancelFling() {
        if (scrollState == AntiHorizontalWheelView.SCROLL_STATE_SETTLING) {
            settlingAnimator!!.cancel()
        }
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        //Log.e("test","distanceX:" + distanceX + "  distanceY:" + distanceY);
        val newAngle = view.radiansAngle + distanceX * SCROLL_ANGLE_MULTIPLIER
        view.radiansAngle = newAngle
        view.onDistanceChange(distanceX, distanceY)
        updateScrollStateIfRequired(AntiHorizontalWheelView.SCROLL_STATE_DRAGGING)
        return true
    }

    private fun updateScrollStateIfRequired(newState: Int) {
        if (listener != null && scrollState != newState) {
            scrollState = newState
            listener!!.onScrollStateChanged(newState)
        }
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        var endAngle = view.radiansAngle - velocityX * FLING_ANGLE_MULTIPLIER
        if (snapToMarks) {
            endAngle = findNearestMarkAngle(endAngle).toFloat().toDouble()
        }
        playSettlingAnimation(endAngle)
        return true
    }

    private fun findNearestMarkAngle(angle: Double): Double {
        val step = 2 * PI / view.marksCount
        return Math.round(angle / step) * step
    }

    private fun playSettlingAnimation(endAngle: Double) {
        updateScrollStateIfRequired(AntiHorizontalWheelView.SCROLL_STATE_SETTLING)
        val startAngle = view.radiansAngle
        val duration = (Math.abs(startAngle - endAngle) * SETTLING_DURATION_MULTIPLIER).toInt()
        settlingAnimator = ValueAnimator.ofFloat(startAngle.toFloat(), endAngle.toFloat())
                .setDuration(duration.toLong())
        settlingAnimator!!.interpolator = INTERPOLATOR
        settlingAnimator!!.addUpdateListener(flingAnimatorListener)
        settlingAnimator!!.addListener(animatorListener)
        settlingAnimator!!.start()
    }

    companion object {

        private val SCROLL_ANGLE_MULTIPLIER = 0.002f
        private val FLING_ANGLE_MULTIPLIER = 0.0002f
        private val SETTLING_DURATION_MULTIPLIER = 1000
        private val INTERPOLATOR = DecelerateInterpolator(2.5f)
    }
}
