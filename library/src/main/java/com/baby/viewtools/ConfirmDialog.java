package com.baby.viewtools;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.library.R;
public class ConfirmDialog extends Dialog implements View.OnClickListener{
	private Window window;
    private ConfirmDialog self = this;
    private TextView confirm;
    private TextView cancel;
    private TextView title;
    private Callback callback;
	public ConfirmDialog(Context context, int theme) {
		super(context, theme);
		initView();
        initData();
        setListener();
	}

	public ConfirmDialog(Context context) {
		this(context,0);
	}

	public void setCallBack(Callback callback){
		this.callback = callback;
	}

	public void setContent(String content){
		if(title != null) {
			title.setText(content);
		}
	}

	private void setListener() {
		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	private void initData() {


	}

	private void initView() {
		this.window = this.getWindow();
        this.window.requestFeature(Window.FEATURE_NO_TITLE);
        this.window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_confirm);
		confirm = (TextView) findViewById(R.id.textview_confirm);
		cancel = (TextView) findViewById(R.id.textview_cancel);
		title  = (TextView) findViewById(R.id.textview_title);
	}
	public interface Callback{
		public void onConfirm(Dialog dialog, TextView confirm, TextView cancel);
		public void onCanncel(Dialog dialog, TextView confirm, TextView cancel);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.textview_confirm){
			this.dismiss();
			if (callback != null) {
				callback.onConfirm(this, confirm, cancel);
			}
		}else if(v.getId() == R.id.textview_cancel){
			this.dismiss();
			if (callback != null) {
				callback.onCanncel(this, confirm, cancel);
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
        getWindow().setLayout(  ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}

