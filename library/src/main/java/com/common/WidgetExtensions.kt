package com.common

import android.graphics.Rect
import android.view.MotionEvent
import com.baby.viewtools.ConfirmDialog

fun MotionEvent.isTouchIn(rect: Rect) : Boolean{
    return x in rect.left .. rect.right && y in rect.top .. rect.bottom
}