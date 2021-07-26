 package com.yfz.widget.knob

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.library.R
import kotlin.math.*


open class Knob @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {
    private val a = context.obtainStyledAttributes(attrs, R.styleable.Knob)
    private var backDrawable: Drawable = a.getDrawable(R.styleable.Knob_knob_back_circle_drawable)
        ?: ContextCompat.getDrawable(context, R.drawable.knob_back)!!
    private var mainDrawable: Drawable = a.getDrawable(R.styleable.Knob_knob_main_circle_drawable)
        ?: ContextCompat.getDrawable(context, R.drawable.knob_controller)!!
    private var progressPrimaryDrawable: Drawable? = a.getDrawable(R.styleable.Knob_knob_progress_primary_drawable)?.apply {
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE,null)
    }
    private var progressSecondDrawable: Drawable? = a.getDrawable(R.styleable.Knob_knob_progress_second_drawable)
    private var isProgressDrawable: Boolean = a.getBoolean(R.styleable.Knob_knob_is_progress_drawable,false)
    private var mainCircleRadius: Float = a.getFloat(R.styleable.Knob_knob_main_circle_radius, 0.85f)
    private var backgroundCircleRadius: Float = a.getFloat(R.styleable.Knob_knob_back_circle_radius, 1f)
    private val progressPrimaryStrokeWidth = a.getFloat(R.styleable.Knob_knob_progress_primary_stroke_width, 0.05f)
    private val progressPrimaryPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Knob_knob_progress_primary_color, Color.parseColor("#FFA036"))
        strokeWidth = progressPrimaryStrokeWidth
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint().apply {
        color =  Color.parseColor("#FFFFFF")
        textSize = 20f
    }
    private val progressSecondStrokeWidth = a.getFloat(R.styleable.Knob_knob_progress_secondary_stroke_width, 0.05f)
    private val progressSecondPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Knob_knob_progress_secondary_color, Color.parseColor("#FF363636"))
        strokeWidth = progressSecondStrokeWidth
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val dstOut = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    private val startIndex = a.getInt(R.styleable.Knob_knob_start_index, 0)
    private val endIndex = a.getInt(R.styleable.Knob_knob_end_index, 100)

    var index = a.getInt(R.styleable.Knob_knob_index, 0)
        private set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    private val startOffset = a.getInt(R.styleable.Knob_knob_start_offset, 30)
    private var sweepAngle = a.getInt(R.styleable.Knob_knob_sweep_angle, 300)
    private var progressRadius = a.getFloat(R.styleable.Knob_knob_progress_radius, 0.9f)
    private val reviseDegree = a.getInt(R.styleable.Knob_knob_main_circle_drawable_revise_degree, 0)
    private val oval = RectF()
    private val totalIndex get() = endIndex - startIndex
    private var progressPercent = (index - startIndex).toFloat() / totalIndex
        set(value) {
            field = when {
                value < 0 -> 0f
                value > 1 -> 1f
                else -> value
            }
        }
    private val crollerMatrix = Matrix()
    private val backgroundMatrix = Matrix()
    private val gestureDetector by lazy {
        GestureDetector(this.context, this)
    }
    private val onIndexChangeListeners by lazy {
        mutableListOf<Knob.(index: Int) -> Unit>()
    }
    private val onStartTrackingListeners by lazy {
        mutableListOf<Knob.() -> Unit>()
    }
    private val onStopTrackingListeners by lazy {
        mutableListOf<Knob.() -> Unit>()
    }

    private var primarySweepGradientColors: Pair<IntArray, FloatArray>? = null
    private var secondSweepGradientColors: Pair<IntArray, FloatArray>? = null
    private var setStyleTools: (() -> Unit)? = null

    init {
        a.recycle()
    }

    fun bindingIndex(index: Int) {
        progressPercent = (index - startIndex).toFloat() / totalIndex
        this.index = index
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun makeStyleTools(width: Float, height: Float) = run {
        val midX = width / 2
        val midY = height / 2
        val minLength = min(width, height)
        return@run {
            progressPrimaryPaint.apply {
                strokeWidth = when {
                    progressPrimaryStrokeWidth in 0.001f..0.4f -> {
                        val r = minLength * progressPrimaryStrokeWidth
                        if (r < 1) 10f else r
                    }
                    progressPrimaryStrokeWidth >= 1 -> {
                        progressPrimaryStrokeWidth
                    }
                    else -> {
                        10f
                    }
                }
                primarySweepGradientColors?.also {
                    shader = SweepGradient(midX, midY, it.first, it.second)
                    strokeCap = Paint.Cap.BUTT
                }
            }

            progressSecondPaint.apply {
                strokeWidth = when {
                    progressSecondStrokeWidth in 0.001f..0.4f -> {
                        val r = minLength * progressSecondStrokeWidth
                        if (r < 1) 10f else r
                    }
                    progressSecondStrokeWidth >= 1 -> {
                        progressSecondStrokeWidth
                    }
                    else -> {
                        10f
                    }
                }
                secondSweepGradientColors?.also {
                    shader = SweepGradient(midX, midY, it.first, it.second)
                    strokeCap = Paint.Cap.BUTT
                }
            }
            Unit
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setStyleTools = makeStyleTools(w.toFloat(), h.toFloat())
        setStyleTools?.invoke()
    }

    fun setPrimarySweepGradient(@ColorInt colors: IntArray, positions: FloatArray) {
        primarySweepGradientColors = colors to positions
        setStyleTools?.also {
            it()
            invalidate()
        }
    }

    fun setSecondarySweepGradient(@ColorInt colors: IntArray, positions: FloatArray) {
        secondSweepGradientColors = colors to positions
        setStyleTools?.also {
            it()
            invalidate()
        }
    }

    private val matrix1 = Matrix()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val minLength = min(width, height)
        val midX = width / 2
        val midY = height / 2
        if (isProgressDrawable and (progressPrimaryDrawable != null)){
            val progressPrimaryDrawable = progressPrimaryDrawable!!
            if (progressSecondDrawable != null){
                val saveCount = canvas.save()
                canvas.translate(midX - minLength / 2, midY - minLength / 2)
                progressSecondDrawable!!.setBounds(0, 0, minLength.toInt(), minLength.toInt())
                progressSecondDrawable!!.draw(canvas)
                clipPaint.xfermode = dstOut
                matrix1.reset()
                matrix1.preRotate(90 + startOffset.toFloat(), minLength / 2, minLength / 2)
                canvas.concat(matrix1)
                oval.set(-0.42f * minLength, -0.42f * minLength,  minLength + 0.42f * minLength,  minLength + 0.42f * minLength)
                canvas.drawArc(oval, 0f, sweepAngle.toFloat() * progressPercent, true, clipPaint)
                canvas.restoreToCount(saveCount)
            }

            kotlin.run {
                val saveCount = canvas.save()
                canvas.translate(midX - minLength / 2, midY - minLength / 2)
                backgroundMatrix.reset()
                backgroundMatrix.preScale(backgroundCircleRadius, backgroundCircleRadius, minLength / 2, minLength / 2)
                canvas.concat(backgroundMatrix)
                backDrawable.setBounds(0, 0, minLength.toInt(), minLength.toInt())
                backDrawable.draw(canvas)
                canvas.restoreToCount(saveCount)
            }

            kotlin.run {
                val saveCount = canvas.saveLayer(midX - minLength / 2 , midY - minLength / 2 , midX + minLength / 2 ,midY + minLength / 2, null)
                canvas.translate(midX - minLength / 2, midY - minLength / 2)
                progressPrimaryDrawable.setBounds(0, 0, minLength.toInt(), minLength.toInt())
                progressPrimaryDrawable.draw(canvas)
                clipPaint.xfermode = dstOut

                matrix1.reset()
                matrix1.preRotate(90 + startOffset.toFloat(), minLength / 2, minLength / 2)
                canvas.concat(matrix1)
                oval.set(-0.42f * minLength, -0.42f * minLength,  minLength + 0.42f * minLength,  minLength + 0.42f * minLength)
                canvas.drawArc(oval, sweepAngle.toFloat() * progressPercent, 360 - (sweepAngle.toFloat() * progressPercent), true, clipPaint)
                canvas.restoreToCount(saveCount)
            }
        }
        else{
            kotlin.run {
                val saveCount = canvas.save()
                canvas.translate(midX - minLength / 2, midY - minLength / 2)
                backgroundMatrix.reset()
                backgroundMatrix.preScale(backgroundCircleRadius, backgroundCircleRadius, minLength / 2, minLength / 2)
                canvas.concat(backgroundMatrix)
                backDrawable.setBounds(0, 0, minLength.toInt(), minLength.toInt())
                backDrawable.draw(canvas)
                canvas.restoreToCount(saveCount)
            }
            kotlin.run {
                val saveCount = canvas.save()
                matrix1.reset()
                matrix1.preRotate(90 + startOffset.toFloat(), midX, midY)
                canvas.concat(matrix1)
                val radius = if (progressRadius in 0f..1f) progressRadius * minLength else progressRadius
                oval.set(midX - radius / 2, midY - radius / 2, midX + radius / 2, midY + radius / 2)
                canvas.drawArc(oval, 0f, sweepAngle.toFloat(), false, progressSecondPaint)
                canvas.restoreToCount(saveCount)
            }
            kotlin.run {
                val saveCount = canvas.save()
                matrix1.reset()
                matrix1.preRotate(90 + startOffset.toFloat(), midX, midY)
                canvas.concat(matrix1)
                val radius = if (progressRadius in 0f..1f) progressRadius * minLength else progressRadius
                oval.set(midX - radius / 2, midY - radius / 2, midX + radius / 2, midY + radius / 2)
                canvas.drawArc(oval, 0f, sweepAngle.toFloat() * progressPercent, false, progressPrimaryPaint)
                canvas.restoreToCount(saveCount)
            }
        }
        kotlin.run {
            val saveCount = canvas.save()
            canvas.translate(midX - minLength / 2, midY - minLength / 2)
            crollerMatrix.reset()
            crollerMatrix.preScale(mainCircleRadius, mainCircleRadius, minLength / 2, minLength / 2)
            val degree = (0f + reviseDegree + startOffset) + sweepAngle * progressPercent
            crollerMatrix.preRotate(degree, minLength / 2, minLength / 2)
            canvas.concat(crollerMatrix)
            mainDrawable.setBounds(0, 0, minLength.toInt(), minLength.toInt())
            mainDrawable.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    var downTouchDeg = 0f
    override fun onShowPress(e: MotionEvent) {
        val viewCenterX = measuredWidth / 2
        val viewCenterY = measuredHeight / 2
        val dx = e.x - viewCenterX
        val dy = e.y - viewCenterY
        downTouchDeg = ((atan2(dy, dx) * 180) / PI).toFloat()
        onStartTrackingListeners.forEach { action ->
            this@Knob.action()
        }
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onStopTrackingListeners.forEach { action ->
            this@Knob.action()
        }
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val viewCenterX = measuredWidth.toFloat() / 2
        val viewCenterY = measuredHeight.toFloat() / 2
        val dx = e2.x - viewCenterX
        val dy = e2.y - viewCenterY
        val d = sqrt(dx * dx + dy * dy)
        //滑动系数,越靠近中心变得越变化越小
        val radius = sqrt(viewCenterX * viewCenterX + viewCenterY * viewCenterY)
        val ceo = kotlin.run {
            val c = d / (radius * 0.8f)
            when {
                c > 1f -> 1f
                c < 0.5f -> 0.5f
                else -> c
            }
        }
        val currentTouchDeg = ((atan2(dy, dx) * 180) / PI).toFloat()
        val deg = (currentTouchDeg - downTouchDeg)
        if (abs(deg) < 90) {
            progressPercent += (deg / sweepAngle) * ceo
            val oldIndex = index
            val newIndex = (totalIndex * progressPercent).toInt() + startIndex
            if (oldIndex != newIndex) {
                index = newIndex
                onIndexChangeListeners.forEach { action ->
                    this@Knob.action(index)
                }
            }
        }
        downTouchDeg = currentTouchDeg
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    fun addKnobChangeListener(
        onStartTracking: (Knob.() -> Unit)? = null,
        onStopTracking: (Knob.() -> Unit)? = null,
        onIndexChange: (Knob.(index: Int) -> Unit)? = null
    ) {
        if (onStartTracking != null) {
            onStartTrackingListeners.add(onStartTracking)
        }
        if (onStopTracking != null) {
            onStopTrackingListeners.add(onStopTracking)
        }
        if (onIndexChange != null) {
            onIndexChangeListeners.add(onIndexChange)
        }
    }

    fun removeKnobChangeListener(
        onStartTracking: (Knob.() -> Unit)? = null,
        onStopTracking: (Knob.() -> Unit)? = null,
        onIndexChange: (Knob.(index: Int) -> Unit)? = null
    ) {
        if (onStartTracking != null) {
            onStartTrackingListeners.remove(onStartTracking)
        }
        if (onStopTracking != null) {
            onStopTrackingListeners.remove(onStopTracking)
        }
        if (onIndexChange != null) {
            onIndexChangeListeners.remove(onIndexChange)
        }
    }
}
