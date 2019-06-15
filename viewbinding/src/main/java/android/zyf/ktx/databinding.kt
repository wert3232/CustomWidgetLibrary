package android.zyf.ktx

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter

/*
@BindingAdapter(value = ["imgTop", "imgSize"],requireAll = false)
fun AppCompatTextView.drawableTop(imgTop: Drawable?, imgSize:Int?){
    val d = imgTop ?: return
    val viewWidth = measuredWidth
    val viewHeight = measuredHeight
    val drawableWidth = d.intrinsicWidth
    val drawableHeight = d.intrinsicHeight
    val vwidth = width - paddingStart - paddingRight
    val vheight = height - paddingTop - paddingBottom
//    this.setCompoundDrawablesRelativeWithIntrinsicBounds(null,d,null,null)
    setBackgroundDrawable(d)

}

@BindingAdapter("testText")
fun AppCompatTextView.setTestText(test: String){
    text=test
}*/
