package com.baby.viewtools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.library.R;

public class ConfirmDialog extends Dialog implements View.OnClickListener {
    private Window window;
    private ConfirmDialog self = this;
    private TextView confirm;
    private TextView cancel;
    private TextView title;
    private Callback callback;
    private Boolean isAutoDismiss = true;

    public ConfirmDialog(Context context) {
        this(context, 0);
    }

    public ConfirmDialog(Context context, int theme) {
        this(context, theme,0);
    }
    public ConfirmDialog(Context context, int theme, @LayoutRes int layoutId) {
        super(context, theme);
        initView(layoutId);
        initData();
        setListener();
    }

    public ConfirmDialog setCallBack(Callback callback) {
        this.callback = callback;
        return this;
    }

    public ConfirmDialog setContent(String content) {
        if (title != null) {
            title.setText(content);
        }
        return this;
    }

    private void setListener() {
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    private void initData() {


    }

    private void initView(@LayoutRes int layoutId) {
        this.window = this.getWindow();
        this.window.requestFeature(Window.FEATURE_NO_TITLE);
        this.window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(false);
        if(layoutId == 0){
            setContentView(R.layout.dialog_confirm);
        }else {
            setContentView(layoutId);
        }
        confirm = findViewById(R.id.textview_confirm);
        cancel = findViewById(R.id.textview_cancel);
        title = findViewById(R.id.textview_title);
    }

    public ConfirmDialog setAutoDismiss(Boolean isAutoDismiss) {
        this.isAutoDismiss = isAutoDismiss;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.textview_confirm) {
            if (isAutoDismiss) {
                this.dismiss();
            }
            if (callback != null) {
                callback.onConfirm(this, confirm, cancel);
            }
        } else if (v.getId() == R.id.textview_cancel) {
            this.dismiss();
            if (callback != null) {
                callback.onCancel(this, confirm, cancel);
            }
        }
    }

    @Override
    public void show() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);

        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public interface Callback {
        void onConfirm(Dialog dialog, TextView confirm, TextView cancel);

        void onCancel(Dialog dialog, TextView confirm, TextView cancel);
    }
}

