package com.yfz.widget.knob

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DrawableUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.library.R
open class Knob @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val a = context.obtainStyledAttributes(attrs, R.styleable.Croller)
    private val valLabelPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Croller_val_label_color, Color.TRANSPARENT)
        style = Paint.Style.FILL
        textSize = a.getDimension(R.styleable.Croller_val_label_color, 16f)
        isFakeBoldText = false
        textAlign = Paint.Align.CENTER
    }
    private var textLabelPaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Croller_label_color, Color.TRANSPARENT)
        style = Paint.Style.FILL
        textSize = a.getInt(R.styleable.Croller_label_size, 40).toFloat()
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Croller_progress_secondary_color, Color.parseColor("#111111"))
        strokeWidth = a.getDimension(R.styleable.Croller_progress_secondary_stroke_width, 10f)
        style = Paint.Style.FILL
    }
    private var circlePaint2 = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Croller_progress_primary_color, Color.parseColor("#FFA036"))
        strokeWidth = a.getDimension(R.styleable.Croller_progress_primary_stroke_width, 10f)
        style = Paint.Style.FILL
    }
    private var linePaint = Paint().apply {
        isAntiAlias = true
        color = a.getColor(R.styleable.Croller_indicator_color, Color.parseColor("#FFA036"));
        strokeWidth = a.getFloat(R.styleable.Croller_indicator_width, 7f);
    }

    private val SWEEP_GRADIENT_COLORS = intArrayOf(Color.YELLOW, Color.RED, Color.RED, Color.WHITE, Color.WHITE, Color.GREEN, Color.GREEN, Color.YELLOW, Color.YELLOW)
    private val SWEEP_GRADIENT_POSITION = floatArrayOf(0f, 30f / 360f, 60f / 360f, 120f / 360f, 150f / 360f, 180f / 360f, 240f / 360f, 300f / 360f, 360f / 360f)

    private var midx = 0f
    private var midy = 0f

    private var max = a.getInt(R.styleable.Croller_max,25)
    private var min = a.getInt(R.styleable.Croller_min,1)

    private var currdeg = 0f
    private var deg = a.getFloat(R.styleable.Croller_progress, 1f) + 2f
    private var downdeg = 0f

    var isAntiClockwise = a.getBoolean(R.styleable.Croller_anticlockwise, false)
            set(value) {
                field = value
                invalidate()
            }

    private var startEventSent = false
    private var mProgressChangeListener: ProgressChangedListener? = null
    private var mKnobChangeListener: KnobChangeListener? = null

    private var mBackDrawable: Drawable = a.getDrawable(R.styleable.Croller_back_circle_drawable) ?: ContextCompat.getDrawable(context,R.drawable.knob_back)!!
    private var mMainDrawable: Drawable = a.getDrawable(R.styleable.Croller_main_circle_drawable) ?: ContextCompat.getDrawable(context,R.drawable.knob_controler)!!

    init {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        midx =  measuredWidth.toFloat();
        midy =  measuredHeight.toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN ->{
                isPressed = true
                val dx = event.getX() - midx
                val dy = event.getY() - midy
                downdeg = (Math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                downdeg -= 90f
                if (downdeg < 0) {
                    downdeg += 360f
                }
                downdeg = Math.floor((downdeg / 360 * (max + 5)).toDouble()).toFloat()

                mKnobChangeListener?.apply {
                    onStartTrackingTouch(this@Knob)
                    startEventSent = true
                }
                return true
            }
            MotionEvent.ACTION_UP ->{
                isPressed = true
                val dx = event.getX() - midx
                val dy = event.getY() - midy
                currdeg = (Math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                currdeg -= 90f
                if (currdeg < 0) {
                    currdeg += 360f
                }
                currdeg = Math.floor((currdeg / 360 * (max + 5)).toDouble()).toFloat()

                if (currdeg / (max + 4) > 0.75f && (downdeg - 0) / (max + 4) < 0.25f) {
                    if (isAntiClockwise) {
                        deg++
                        if (deg > max + 2) {
                            deg = (max + 2).toFloat()
                        }
                    } else {
                        deg--
                        if (deg < min + 2) {
                            deg = (min + 2).toFloat()
                        }
                    }
                } else if (downdeg / (max + 4) > 0.75f && (currdeg - 0) / (max + 4) < 0.25f) {
                    if (isAntiClockwise) {
                        deg--
                        if (deg < min + 2) {
                            deg = (min + 2).toFloat()
                        }
                    } else {
                        deg++
                        if (deg > max + 2) {
                            deg = (max + 2).toFloat()
                        }
                    }
                } else {
                    if (isAntiClockwise) {
                        deg -= currdeg - downdeg
                    } else {
                        deg += currdeg - downdeg
                    }
                    if (deg > max + 2) {
                        deg = (max + 2).toFloat()
                    }
                    if (deg < min + 2) {
                        deg = (min + 2).toFloat()
                    }
                }

                downdeg = currdeg

                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE ->{
                isPressed = false
                mKnobChangeListener?.apply {
                    onStopTrackingTouch(this@Knob)
                    startEventSent = false
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mProgressChangeListener?.onProgressChanged((deg - 2).toInt())
        mKnobChangeListener?.onProgressChanged(this, (deg - 2).toInt())
        /*var backBitmap = DrawableUtil.drawableToBitmap(mBackDrawable)
        var mMainBitmap = DrawableUtil.drawableToBitmap(mMainDrawable)
        canvas.drawBitmap(backBitmap,canvas.width.toFloat() / 2 ,canvas.height.toFloat() / 2, null)
        canvas.drawBitmap(mMainBitmap,(mMainBitmap.width - canvas.width).toFloat() / 2,(mMainBitmap.height - canvas.height).toFloat() / 2, null)*/
        val back  = mBackDrawable.toBitmap(canvas.width,canvas.height,0.6f)
        val main = mMainDrawable.toBitmap(canvas.width,canvas.height,0.5f)
        canvas.drawBitmap(back,(canvas.width - back.width).toFloat() / 2,(canvas.height - back.height).toFloat() / 2,null)
        canvas.drawBitmap(main,(canvas.width - main.width).toFloat() / 2,(canvas.height - main.height).toFloat() / 2,null)
    }

    fun setProgress(x: Int) {
        deg = (x + 2).toFloat()
        invalidate()
    }
    fun getProgress(): Int {
        return (deg - 2).toInt()
    }
    fun Drawable.toBitmap(wrapWidth: Int, wrapHigh: Int, ratio: Float = 1f, deg: Float = 0f) : Bitmap {
        val config = if (this.opacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888
        else
            Bitmap.Config.RGB_565
        var bitmap1: Bitmap = this.toBitmap(config = config)

        /*var bitmap2 = */bitmap1.let {
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
            return Bitmap.createBitmap(it,0,0,it.width, it.height,matrix, false)
        }
        /*bitmap1.recycle()
        return bitmap2*/
    }
}

interface ProgressChangedListener {
    fun onProgressChanged(progress: Int)
}

interface KnobChangeListener {
    fun onProgressChanged(knob: Knob, progress: Int)

    fun onStartTrackingTouch(knob: Knob)

    fun onStopTrackingTouch(knob: Knob)
}
