package com.github.shchurov.horizontalwheelview

import android.content.Context
import android.content.res.TypedArray
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

import java.lang.Math.PI
import com.library.R

class HorizontalWheelView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    val a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalWheelView)
    private var startIndex = a.getInt(R.styleable.HorizontalWheelView_horizontalWheelView_start_index, 0)
    private var endIndex = a.getInt(R.styleable.HorizontalWheelView_horizontalWheelView_end_index, 10) + 1
    private var isAnti = a.getBoolean(R.styleable.HorizontalWheelView_horizontalWheelView_isAnti, false)
    private val drawer: Drawer = Drawer(this)
    private val touchHandler: TouchHandler
    private var angle: Double = 0.toDouble()
        set(value) {
            if (field == value) {
            } else {
                field = value
            }
        }
    var onlyPositiveValues: Boolean = false
    var endLock: Boolean = false
    private var listener: onChangeListener? = null
    //值越大越慢
    private var scaleSpeedUnit = a.getInt(R.styleable.HorizontalWheelView_scaleSpeedUnit, 20)
    var inverseBindingListener: InverseBindingListener? = null
    var viewIndex = a.getInt(R.styleable.HorizontalWheelView_horizontalWheelView_index, startIndex)
        set(value) {
            if (field != value) {
                field = value
                val current = value - startIndex
                val progress = endIndex - startIndex
                angle = if (isAnti) {
                    -(current * (2 * PI) / progress)
                } else {
                    current * (2 * PI) / progress
                }
                invalidate()
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
            val progress = endIndex - startIndex
            if (isAnti) {
                if (onlyPositiveValues && angle > 0) {
                    angle -= 2 * PI
                }
                viewIndex = -(this.angle * progress.toFloat() / (2 * PI)).toInt() + startIndex
            } else {
                if (onlyPositiveValues && angle < 0) {
                    angle += 2 * PI
                }
                viewIndex = (this.angle * progress.toFloat() / (2 * PI)).toInt() + startIndex
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

    var marksCount: Int
        get() = drawer.marksCount
        set(marksCount) {
            drawer.marksCount = marksCount
            invalidate()
        }
    var isCursorShow = true
        set(value) {
            drawer.isCursorShow = value
            field = value
        }
    var scaleAction: (Int) -> Unit = {}
    var normalMarkLengthRatio = 0.6f
        set(value) {
            drawer.normalMarkHeightRatio = value
            field = value
        }
    var zeroMarkHeightRatio = 0.8f
        set(value) {
            drawer.zeroMarkHeightRatio = value
            field = value
        }
    var zeroMarkColor = -1
        set(value) {
            drawer.zeroMarkColor = value
            field = value
        }
    private var lineSpaceRatio = 0f
        set(value) {
            drawer.setLineSpaceRatio(value)
            field = value
        }
    private var isCursorSpace = true
        set(value) {
            drawer.setCursorSpace(value)
            field = value
        }
    private var isZeroSpace = true
        set(value) {
            drawer.setZeroSpace(value)
            field = value
        }

    init {
        touchHandler = TouchHandler(this)
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet) {
        val marksCount = a.getInt(R.styleable.HorizontalWheelView_marksCount, DEFAULT_MARKS_COUNT)
        drawer.marksCount = marksCount
        val normalColor = a.getColor(R.styleable.HorizontalWheelView_normalColor, DEFAULT_NORMAL_COLOR)
        drawer.setNormalColor(normalColor)
        val activeColor = a.getColor(R.styleable.HorizontalWheelView_activeColor, DEFAULT_ACTIVE_COLOR)
        drawer.setActiveColor(activeColor)
        zeroMarkColor = a.getColor(R.styleable.HorizontalWheelView_zeroMarkColor, -1)
        val showActiveRange = a.getBoolean(R.styleable.HorizontalWheelView_showActiveRange,
                DEFAULT_SHOW_ACTIVE_RANGE)
        drawer.setShowActiveRange(showActiveRange)
        val snapToMarks = a.getBoolean(R.styleable.HorizontalWheelView_snapToMarks, DEFAULT_SNAP_TO_MARKS)
        touchHandler.setSnapToMarks(snapToMarks)
        endLock = a.getBoolean(R.styleable.HorizontalWheelView_endLock, DEFAULT_END_LOCK)
        onlyPositiveValues = a.getBoolean(R.styleable.HorizontalWheelView_onlyPositiveValues,
                DEFAULT_ONLY_POSITIVE_VALUES)
        isCursorShow = a.getBoolean(R.styleable.HorizontalWheelView_isCursorShow, true)
        normalMarkLengthRatio = a.getFloat(R.styleable.HorizontalWheelView_normalMarkLengthRatio, 0.6f)
        zeroMarkHeightRatio = a.getFloat(R.styleable.HorizontalWheelView_zeroMarkLengthRatio, 0.8f)
        lineSpaceRatio = a.getFloat(R.styleable.HorizontalWheelView_lineSpaceRatio, 0f)
        isCursorSpace = a.getBoolean(R.styleable.HorizontalWheelView_isCursorSpace, true)
        isZeroSpace = a.getBoolean(R.styleable.HorizontalWheelView_isZeroSpace, true)
        drawer.setNormalMarkWidth(a.getInt(R.styleable.HorizontalWheelView_normalMarkWidth, 1))
        a.recycle()
    }

    fun setChangeListener(listener: onChangeListener) {
        this.listener = listener
        touchHandler.setChangeListener(listener)
    }

    fun setChangeListener(
        onRotationChanged: (radians: Double, index: Int) -> Unit,
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


    fun setShowActiveRange(show: Boolean) {
        drawer.setShowActiveRange(show)
        invalidate()
    }

    fun setNormaColor(color: Int) {
        drawer.setNormalColor(color)
        invalidate()
    }

    fun setActiveColor(color: Int) {
        drawer.setActiveColor(color)
        invalidate()
    }

    fun setSnapToMarks(snapToMarks: Boolean) {
        touchHandler.setSnapToMarks(snapToMarks)
    }

    private var scale = 0f
    fun onDistanceChange(distanceX: Float, distanceY: Float) {
        if (isAnti) {
            if (Math.abs(distanceX) < scaleSpeedUnit) {
                scale += distanceX
                if (Math.abs(scale) > scaleSpeedUnit) {
                    scaleAction(-(scale / scaleSpeedUnit).toInt())
                    scale = 0f
                }
            } else {
                scaleAction(-(distanceX / scaleSpeedUnit).toInt())
                scale = 0f
            }
        } else {
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            listener?.onTouch()
        }
        return touchHandler.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        drawer.onSizeChanged()
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
        drawer.onDraw(canvas)
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

    companion object {

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

interface onChangeListener {
    fun onRotationChanged(radians: Double, index: Int)
    fun onScrollStateChanged(state: Int)
    fun onTouch()
}
