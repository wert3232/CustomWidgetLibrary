/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/* This file contains AOSP code copied from /frameworks/base/core/java/android/widget/AbsSeekBar.java */
/*============================================================================*/
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*============================================================================*/

package com.h6ah4i.android.widget.verticalseekbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.SeekBar

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import com.common.ReflectionUtils
import com.common.*
import com.library.R

class VerticalSeekBar : AppCompatSeekBar {

    private var mIsDragging: Boolean = false
    private var mMethodSetProgressFromUser: Method? = null
    private var mRotationAngle = ROTATION_ANGLE_CW_90
    private var onlyTouchThumbCanSlide = false
    private var spreadTouchRange = 0
    private var wm: WindowManager? = null
    private val screenSize: Point? = null
    private var experiment = true
    var startIndex = 0
        set(value) {
            field = value
            if(max != total){
                max = total
            }
        }
    var endIndex = 100
        set(value) {
            field = value
            if(max != total){
                max = total
            }
        }
    val total get() =  endIndex - startIndex
    var index
        set(value){
            progress = value - startIndex
        }
        get() = progress + startIndex
    private var callBack: DownCallBack? = null
    private var thumb1: Drawable? = null
    private var simulateDownX = 0
    private var realDownX = 0f

    var rotationAngle: Int
        get() = mRotationAngle
        set(angle) {
            if (!isValidRotationAngle(angle)) {
                throw IllegalArgumentException("Invalid angle specified :$angle")
            }

            if (mRotationAngle == angle) {
                return
            }

            mRotationAngle = angle

            if (useViewRotation()) {
                val wrapper = wrapper
                wrapper?.applyViewRotation()
            } else {
                requestLayout()
            }
        }

    private val wrapper: VerticalSeekBarWrapper?
        get() {
            val parent = parent

            return if (parent is VerticalSeekBarWrapper) {
                parent
            } else {
                null
            }
        }

    constructor(context: Context) : super(context) {
        initialize(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context, attrs, defStyle, 0)
    }
    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar, defStyleAttr, defStyleRes)
            val commonA = context.obtainStyledAttributes(attrs, R.styleable.commonAttr, defStyleAttr, defStyleRes)
            startIndex = a.getInt(R.styleable.VerticalSeekBar_verticalSeekBar_start_index, 0)
            endIndex = a.getInt(R.styleable.VerticalSeekBar_verticalSeekBar_end_index, max)
            index = a.getInt(R.styleable.VerticalSeekBar_verticalSeekBar_index, progress + startIndex)
            val rotationAngle = a.getInteger(R.styleable.VerticalSeekBar_seekBarRotation, 0)
            onlyTouchThumbCanSlide = a.getBoolean(R.styleable.VerticalSeekBar_onlyTouchThumbCanSlide, false)
            //扩大触点位置
            spreadTouchRange = a.getInteger(R.styleable.VerticalSeekBar_spreadTouchRange, 0)
            if (isValidRotationAngle(rotationAngle)) {
                mRotationAngle = rotationAngle
            }
            experiment = commonA.getBoolean(R.styleable.commonAttr_experiment, true)
            a.recycle()
            commonA.recycle()
        }
        wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun setThumb(thumb: Drawable) {
        thumb1 = thumb
        super.setThumb(thumb)
    }

    interface DownCallBack {
        fun onDown(view: View)
    }

    fun setDownCallBack(callBack: DownCallBack) {
        this.callBack = callBack
    }

    fun setOnIndexChangeCallBack(callBack: VerticalSeekBar.(index: Int) -> Unit){
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                seekBar as VerticalSeekBar
                seekBar.callBack(index)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var _event = event
        if (_event.action == MotionEvent.ACTION_DOWN) {
            if (callBack != null) {
                callBack!!.onDown(this)
            }
            if (onlyTouchThumbCanSlide && !experiment) {
                val `object` = ReflectionUtils.getFieldValue(this, "mThumb")
                if (`object` is Drawable) {
                    val thumb = `object` as Drawable?
                    val rect = thumb!!.bounds
                    val offsetX = -paddingStart + thumb.intrinsicWidth / 2

                    //触点和thumb重合
                    if (!_event.isTouchInX(rect, offsetX, spreadTouchRange)) {
                        return false
                    }
                }
            }
        }
        //add
        if (experiment && onlyTouchThumbCanSlide) {
            if (thumb1 == null) {
                val `object` = ReflectionUtils.getFieldValue(this, "mThumb")
                if (`object` is Drawable) {
                    thumb1 = `object`
                }
            }
            if (thumb1 != null) {
                val rect = thumb1!!.bounds
                val size = Point()
                wm!!.defaultDisplay.getSize(size)
                val x = size.y.toFloat() - event.rawY - paddingStart.toFloat()
                val y = event.rawX
                val eventTime = event.eventTime
                val downTime = event.downTime
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val offsetX = -paddingStart + (rect.right - rect.left) / 2
                    simulateDownX = rect.centerX() - offsetX
                    realDownX = x
                    //                    _event = MotionEvent.obtain(downTime,eventTime,event.getAction(),x,y,event.getMetaState());
                    _event = MotionEvent.obtain(downTime, eventTime, event.action, simulateDownX.toFloat(), y, event.metaState)
                } else if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP
                        || event.action == MotionEvent.ACTION_CANCEL) {
                    val moveX = simulateDownX + (x - realDownX)
                    _event = MotionEvent.obtain(downTime, eventTime, event.action, moveX, y, event.metaState)
                }
            }
        }
        //
        return if (useViewRotation()) {
            onTouchEventUseViewRotation(_event)
        } else {
            onTouchEventTraditionalRotation(_event)
        }
    }

    private fun onTouchEventTraditionalRotation(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag(true)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> if (mIsDragging) {
                trackTouchEvent(event)
            }

            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    if (!onlyTouchThumbCanSlide) {
                        trackTouchEvent(event)
                    }
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should
                    // be interpreted as a tap-seek to that location.

                    onStartTrackingTouch()
                    if (!onlyTouchThumbCanSlide) {
                        trackTouchEvent(event)
                    }
                    onStopTrackingTouch()
                    attemptClaimDrag(false)
                }
                // ProgressBar doesn't know to repaint the thumb controller
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    private fun onTouchEventUseViewRotation(event: MotionEvent): Boolean {
        val handled = super.onTouchEvent(event)

        if (handled) {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> attemptClaimDrag(true)

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> attemptClaimDrag(false)
            }
        }

        return handled
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val paddingLeft = super.getPaddingLeft()
        val paddingRight = super.getPaddingRight()
        val height = height

        val available = height - paddingLeft - paddingRight
        val y = event.y.toInt()

        val scale: Float
        var value = 0f

        when (mRotationAngle) {
            ROTATION_ANGLE_CW_90 -> value = (y - paddingLeft).toFloat()
            ROTATION_ANGLE_CW_270 -> value = (height - paddingLeft - y).toFloat()
        }

        if (value < 0 || available == 0) {
            scale = 0.0f
        } else if (value > available) {
            scale = 1.0f
        } else {
            scale = value / available.toFloat()
        }

        val max = max
        val progress = scale * max

        _setProgressFromUser(progress.toInt(), true)
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag(active: Boolean) {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(active)
    }

    /**
     * This is called when the user has started touching this widget.
     */
    private fun onStartTrackingTouch() {
        mIsDragging = true
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    private fun onStopTrackingTouch() {
        mIsDragging = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isEnabled) {
            val handled: Boolean
            var direction = 0

            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    direction = if (mRotationAngle == ROTATION_ANGLE_CW_90) 1 else -1
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    direction = if (mRotationAngle == ROTATION_ANGLE_CW_270) 1 else -1
                    handled = true
                }
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT ->
                    // move view focus to previous/next view
                    return false
                else -> handled = false
            }

            if (handled) {
                val keyProgressIncrement = keyProgressIncrement
                var progress = progress

                progress += direction * keyProgressIncrement

                if (progress >= 0 && progress <= max) {
                    _setProgressFromUser(progress, true)
                }

                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        if (!useViewRotation()) {
            refreshThumb()
        }
    }

    @Synchronized
    private fun _setProgressFromUser(progress: Int, fromUser: Boolean) {
        if (mMethodSetProgressFromUser == null) {
            try {
                val m: Method
                m = ProgressBar::class.java.getDeclaredMethod("setProgress", Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
                m.isAccessible = true
                mMethodSetProgressFromUser = m
            } catch (e: NoSuchMethodException) {
            }

        }

        if (mMethodSetProgressFromUser != null) {
            try {
                mMethodSetProgressFromUser!!.invoke(this, progress, fromUser)
            } catch (e: IllegalArgumentException) {
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            }

        } else {
            super.setProgress(progress)
        }
        refreshThumb()
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (useViewRotation()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)

            val lp = layoutParams

            if (isInEditMode && lp != null && lp.height >= 0) {
                setMeasuredDimension(super.getMeasuredHeight(), lp.height)
            } else {
                setMeasuredDimension(super.getMeasuredHeight(), super.getMeasuredWidth())
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            super.onSizeChanged(w, h, oldw, oldh)
        } else {
            super.onSizeChanged(h, w, oldh, oldw)
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        if (!useViewRotation()) {
            when (mRotationAngle) {
                ROTATION_ANGLE_CW_90 -> {
                    canvas.rotate(90f)
                    canvas.translate(0f, (-super.getWidth()).toFloat())
                }
                ROTATION_ANGLE_CW_270 -> {
                    canvas.rotate(-90f)
                    canvas.translate((-super.getHeight()).toFloat(), 0f)
                }
            }
        }

        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    // refresh thumb position
    private fun refreshThumb() {
        onSizeChanged(super.getWidth(), super.getHeight(), 0, 0)
    }

    /*package*/ internal fun useViewRotation(): Boolean {
        val isSupportedApiLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        val inEditMode = isInEditMode
        return isSupportedApiLevel && !inEditMode
    }

    companion object {
        val ROTATION_ANGLE_CW_90 = 90
        val ROTATION_ANGLE_CW_270 = 270

        private fun isValidRotationAngle(angle: Int): Boolean {
            return angle == ROTATION_ANGLE_CW_90 || angle == ROTATION_ANGLE_CW_270
        }
    }
}
