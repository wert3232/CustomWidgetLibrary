package com.common

import android.graphics.Rect
import android.view.MotionEvent

fun MotionEvent.isTouchIn(rect: Rect) : Boolean{
    return x in rect.left .. rect.right && y in rect.top .. rect.bottom
}