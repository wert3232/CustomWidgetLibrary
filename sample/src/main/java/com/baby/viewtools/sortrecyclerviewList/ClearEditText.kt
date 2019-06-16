/*
package com.baby.viewtools.sortrecyclerviewList

import android.content.Context
import android.graphics.controller.Drawable
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.EditText
import com.yfz.customwidgetlibrary.R

class ClearEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = android.R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyle), OnFocusChangeListener, TextWatcher {
    private val mClearDrawable by lazy {
        (compoundDrawables[2] ?: ContextCompat.getController(context,R.controller.emotionstore_progresscancelbtn))!!
    }

    init {
        mClearDrawable.setBounds(0, 0, mClearDrawable.intrinsicWidth, mClearDrawable.intrinsicHeight)
        setClearIconVisible(false)
        onFocusChangeListener = this
        addTextChangedListener(this)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            if (event.action == MotionEvent.ACTION_UP) {
                val touchable = event.x > (width - paddingRight - mClearDrawable.intrinsicWidth) && event.x < width - paddingRight
                if (touchable) {
                    this.setText("")
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(text!!.length > 0)
        } else {
            setClearIconVisible(false)
        }
    }

    protected fun setClearIconVisible(visible: Boolean) {
        val right = if (visible) mClearDrawable else null
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], right, compoundDrawables[3])
    }


    override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        setClearIconVisible(s.length > 0)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable) {

    }


    fun setShakeAnimation() {
        this.animation = shakeAnimation(5)
    }

    companion object {
        fun shakeAnimation(counts: Int): Animation {
            val translateAnimation = TranslateAnimation(0f, 10f, 0f, 0f)
            translateAnimation.interpolator = CycleInterpolator(counts.toFloat())
            translateAnimation.duration = 1000
            return translateAnimation
        }
    }
}
*/
