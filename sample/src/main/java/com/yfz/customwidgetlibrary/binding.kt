package com.yfz.customwidgetlibrary

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.StateListDrawable


/*
*
* imgsize
* */
/*
@BindingAdapter(value = ["imgTop", "imgHeightPercent"],requireAll = false)
fun AppCompatTextView.drawableTop(imgTop: Drawable?, imgHeightPercent:Float?){
    val heightPercent = imgHeightPercent ?: 0.8f
    val key = R.string.img_top
    val d = imgTop ?: return
    val tag = getTag(key) as? View.OnLayoutChangeListener
    if(tag != null){
        removeOnLayoutChangeListener(tag)
    }
    val onLayoutChangeListener = View.OnLayoutChangeListener{ v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        v as AppCompatTextView
        val viewWidth = v.measuredWidth
        val viewHeight = v.measuredHeight
        val drawableWidth = d.intrinsicWidth
        val drawableHeight = d.intrinsicHeight
        val vwidth = (viewWidth - v.paddingStart -  v.paddingRight)
        val vheight = (viewHeight-  v.paddingTop -  v.paddingBottom) * heightPercent
        d.setBounds(0, 0, vwidth, vheight.toInt())
        text = "$viewWidth:$viewHeight $vheight + $vwidth"
        this.setCompoundDrawables(null,d,null,null)
        Log.e("textView","$left, $top, $right, $bottom|$oldLeft, $oldTop, $oldRight, $oldBottom")
        Log.e("textView","$drawableHeight, $drawableWidth")
    }
    setTag(key,onLayoutChangeListener)
    addOnLayoutChangeListener(onLayoutChangeListener)
}*/
@BindingAdapter(value = ["imgTop", "imgHeightPercent"], requireAll = false)
fun AppCompatTextView.drawableTop(imgTop: Drawable?, imgHeightPercent: Float?) {
    val heightPercent = imgHeightPercent ?: 0.8f
    val key = R.string.img_top
    imgTop ?: return
    Log.e("test", imgTop.toString())
    val d = imgTop
    val tag = getTag(key) as? View.OnLayoutChangeListener
    if (tag != null) {
        removeOnLayoutChangeListener(tag)
    }
    val onLayoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        v as AppCompatTextView
        val viewWidth = v.measuredWidth
        val viewHeight = v.measuredHeight
        val drawableWidth = d.intrinsicWidth
        val drawableHeight = d.intrinsicHeight
        val vwidth = (viewWidth - v.paddingStart - v.paddingRight)
        val vheight = (viewHeight - v.paddingTop - v.paddingBottom) * heightPercent
        val imgRatio = drawableWidth.toFloat() / drawableHeight
        val viewRatio = vwidth / vheight
        val isWidthReference = imgRatio > viewRatio
        /*if(isWidthReference){
            d.setBounds(0, 0, vwidth, (vwidth / imgRatio).toInt())
        }else{
            d.setBounds(0, 0,  (vwidth * imgRatio).toInt(), vheight.toInt())
        }
        this.setCompoundDrawables(null, d, null, null)*/
        v.setPaddingRelative(paddingStart,(vheight + v.paddingTop).toInt(),paddingEnd,paddingBottom)
        text = "$viewWidth:$viewHeight $vheight + $vwidth"
//        Log.e("textView", "$left, $top, $right, $bottom|$oldLeft, $oldTop, $oldRight, $oldBottom")
//        Log.e("textView", "$drawableHeight, $drawableWidth")
//        Log.e("textView", "${d::class.java.name}")
    }
    setTag(key, onLayoutChangeListener)
    addOnLayoutChangeListener(onLayoutChangeListener)
}

/*private fun drawableToBitmap(controller: Drawable): Bitmap {
    //取drawable的宽高
    val width = controller.intrinsicWidth
    val height = controller.intrinsicHeight
    //取drawable的颜色格式
    val config = if (controller.opacity != PixelFormat.OPAQUE)
        Bitmap.Config.ARGB_8888
    else
        Bitmap.Config.RGB_565
    //创建对应的bitmap
    val bitmap = Bitmap.createBitmap(width, height, config)
    //创建对应的bitmap的画布
    val canvas = Canvas(bitmap)
    controller.setBounds(0, 0, width, height)
    //把drawable内容画到画布中
    controller.draw(canvas)
    return bitmap
}*/

