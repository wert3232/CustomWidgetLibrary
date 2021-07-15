package com.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.baby.viewtools.ConfirmDialog

fun MotionEvent.isTouchIn(rect: Rect, offsetX: Int = 0, offsetY: Int = 0): Boolean {
    val x = x + offsetX
    val y = y + offsetY
    return x.toInt() in rect.left..rect.right && y.toInt() in rect.top..rect.bottom
}

fun MotionEvent.isTouchInX(rect: Rect, offsetX: Int = 0, spreadTouchRange: Int = 0): Boolean {
    val x = x + offsetX
    return x.toInt() in (rect.left - spreadTouchRange)..(rect.right + spreadTouchRange)
}

fun Drawable.toBitmap(
        width: Int = intrinsicWidth,
        height: Int = intrinsicHeight,
        config: android.graphics.Bitmap.Config? = null
): Bitmap {
    if (this is BitmapDrawable) {
        if (config == null || bitmap.config == config) {
            // Fast-path to return original. Bitmap.createScaledBitmap will do this check, but it
            // involves allocation and two jumps into native code so we perform the check ourselves.
            if (width == intrinsicWidth && height == intrinsicHeight) {
                return bitmap
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }

    val oldLeft = bounds.left
    val oldTop = bounds.top
    val oldRight = bounds.right
    val oldBottom = bounds.bottom

    val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))

    setBounds(oldLeft, oldTop, oldRight, oldBottom)
    return bitmap
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.setCompatElevation(elevation: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.elevation = elevation
    }
}