package com.baby.viewtools

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.library.R
import android.text.InputFilter



class EditDialog @JvmOverloads constructor(context: Context, theme: Int = 0) : Dialog(context, theme), View.OnClickListener {
    var tag = -1
    private val self = this
    private val confirm by lazy {
        findViewById<TextView>(R.id.textview_confirm)
    }
    private val cancel by lazy {
        findViewById<TextView>(R.id.textview_cancel)
    }
    private val title by lazy {
        findViewById<TextView>(R.id.textview_title)
    }
    private val edit by lazy {
        findViewById<TextView>(R.id.editText)
    }

    private var callback: Callback ?= null

    init {
        initView()
        initData()
        setListener()
    }
    fun setTag(tag : Int) : EditDialog{
        this.tag = tag
        return this
    }
    fun setMaxLength(length : Int) : EditDialog{
        edit.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(length));
        return this
    }
    fun setCallBack(callback: Callback) : EditDialog{
        this.callback = callback
        return this
    }

    fun setTitle(titleText: String) : EditDialog{
        title.text = titleText
        return this
    }
    fun setEditContent(titleText: String) : EditDialog{
        edit.text = titleText
        return this
    }
    private fun setListener() : EditDialog{
        confirm.setOnClickListener(this)
        cancel.setOnClickListener(this)
        return this
    }

    private fun initData() {

    }

    private fun initView() {
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(null)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_edit)
    }

    interface Callback {
        fun onConfirm(dialog: Dialog, confirm: TextView?, cancel: TextView?,newName:String,tag: Int)
        fun onCanncel(dialog: Dialog, confirm: TextView?, cancel: TextView?)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.textview_confirm) {
            val newName = edit.text.toString()
            if(TextUtils.isEmpty(newName)) return
            this.dismiss()
            if (callback != null) {
                callback!!.onConfirm(this, confirm, cancel,newName,tag)
            }
        } else if (v.id == R.id.textview_cancel) {
            this.dismiss()
            if (callback != null) {
                callback!!.onCanncel(this, confirm, cancel)
            }
        }
    }

    override fun show() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        wm.defaultDisplay.getSize(size)
        super.show()
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    fun show(isClear : Boolean){
        if(isClear){
            edit.text = ""
        }
        show()
    }
    fun show(name : String){
        edit.text = name
        show()
    }
}

