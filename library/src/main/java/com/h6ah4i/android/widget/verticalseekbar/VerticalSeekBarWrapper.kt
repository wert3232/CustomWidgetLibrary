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

package com.h6ah4i.android.widget.verticalseekbar

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class VerticalSeekBarWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val childSeekBar: VerticalSeekBar?
        get() {
            val child = if (childCount > 0) getChildAt(0) else null
            return if (child is VerticalSeekBar) child else null
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            onSizeChangedUseViewRotation(w, h, oldw, oldh)
        } else {
            onSizeChangedTraditionalRotation(w, h, oldw, oldh)
        }
    }

    private fun onSizeChangedTraditionalRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val lp = seekBar.layoutParams as FrameLayout.LayoutParams

            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = Math.max(0, h - vPadding)
            seekBar.layoutParams = lp

            seekBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            val seekBarMeasuredWidth = seekBar.measuredWidth
            seekBar.measure(
                View.MeasureSpec.makeMeasureSpec(Math.max(0, w - hPadding), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(Math.max(0, h - vPadding), View.MeasureSpec.EXACTLY)
            )

            lp.gravity = Gravity.TOP or Gravity.LEFT
            lp.leftMargin = (Math.max(0, w - hPadding) - seekBarMeasuredWidth) / 2
            seekBar.layoutParams = lp
        }

        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun onSizeChangedUseViewRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            seekBar.measure(
                View.MeasureSpec.makeMeasureSpec(Math.max(0, h - vPadding), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(Math.max(0, w - hPadding), View.MeasureSpec.AT_MOST)
            )
        }

        applyViewRotation(w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val seekBar = childSeekBar
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (seekBar != null && widthMode != View.MeasureSpec.EXACTLY) {
            val seekBarWidth: Int
            val seekBarHeight: Int
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val innerContentWidthMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(Math.max(0, widthSize - hPadding), widthMode)
            val innerContentHeightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(Math.max(0, heightSize - vPadding), heightMode)

            if (useViewRotation()) {
                seekBar.measure(innerContentHeightMeasureSpec, innerContentWidthMeasureSpec)
                seekBarWidth = seekBar.measuredHeight
                seekBarHeight = seekBar.measuredWidth
            } else {
                seekBar.measure(innerContentWidthMeasureSpec, innerContentHeightMeasureSpec)
                seekBarWidth = seekBar.measuredWidth
                seekBarHeight = seekBar.measuredHeight
            }

            val measuredWidth = ViewCompat.resolveSizeAndState(seekBarWidth + hPadding, widthMeasureSpec, 0)
            val measuredHeight = ViewCompat.resolveSizeAndState(seekBarHeight + vPadding, heightMeasureSpec, 0)

            setMeasuredDimension(measuredWidth, measuredHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /*package*/ internal fun applyViewRotation() {
        applyViewRotation(width, height)
    }

    private fun applyViewRotation(w: Int, h: Int) {
        val seekBar = childSeekBar

        if (seekBar != null) {
            val isLTR = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
            val rotationAngle = seekBar.rotationAngle
            val seekBarMeasuredWidth = seekBar.measuredWidth
            val seekBarMeasuredHeight = seekBar.measuredHeight
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val hOffset = (Math.max(0, w - hPadding) - seekBarMeasuredHeight) * 0.5f
            val lp = seekBar.layoutParams

            lp.width = Math.max(0, h - vPadding)
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT

            seekBar.layoutParams = lp

            ViewCompat.setPivotX(seekBar, (if (isLTR) 0 else Math.max(0, h - vPadding)).toFloat())
            ViewCompat.setPivotY(seekBar, 0f)

            when (rotationAngle) {
                VerticalSeekBar.ROTATION_ANGLE_CW_90 -> {
                    ViewCompat.setRotation(seekBar, 90f)
                    if (isLTR) {
                        ViewCompat.setTranslationX(seekBar, seekBarMeasuredHeight + hOffset)
                        ViewCompat.setTranslationY(seekBar, 0f)
                    } else {
                        ViewCompat.setTranslationX(seekBar, -hOffset)
                        ViewCompat.setTranslationY(seekBar, seekBarMeasuredWidth.toFloat())
                    }
                }
                VerticalSeekBar.ROTATION_ANGLE_CW_270 -> {
                    ViewCompat.setRotation(seekBar, 270f)
                    if (isLTR) {
                        ViewCompat.setTranslationX(seekBar, hOffset)
                        ViewCompat.setTranslationY(seekBar, seekBarMeasuredWidth.toFloat())
                    } else {
                        ViewCompat.setTranslationX(seekBar, -(seekBarMeasuredHeight + hOffset))
                        ViewCompat.setTranslationY(seekBar, 0f)
                    }
                }
            }
        }
    }

    private fun useViewRotation(): Boolean {
        val seekBar = childSeekBar
        return seekBar?.useViewRotation() ?: false
    }
}
