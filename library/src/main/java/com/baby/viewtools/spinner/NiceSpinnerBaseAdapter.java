package com.baby.viewtools.spinner;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.library.R;

public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {

    private final SpinnerTextFormatter spinnerTextFormatter;

    private int textColor;
    private Float textSize = 10f;
    private int backgroundSelector;
    private int itemHeight= ViewGroup.LayoutParams.WRAP_CONTENT;
    private int itemWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    int selectedIndex;

    NiceSpinnerBaseAdapter(Context context, int textColor, int backgroundSelector,
                           SpinnerTextFormatter spinnerTextFormatter) {
        this.spinnerTextFormatter = spinnerTextFormatter;
        this.backgroundSelector = backgroundSelector;
        this.textColor = textColor;
    }

    @Override public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        TextView textView;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            textView = convertView.findViewById(R.id.text_view_spinner);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemWidth, itemHeight);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                convertView.setBackground(ContextCompat.getDrawable(context, backgroundSelector));
            }
            if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                layoutParams = new AbsListView.LayoutParams(itemWidth, itemHeight);
            }
            convertView.setTag(new ViewHolder(textView));
            convertView.setLayoutParams(layoutParams);
            //Log.e("hello","spinnerTextFormatter:" + spinnerTextFormatter.format(getItem(position).toString()));
        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
        }
        textView.setText(spinnerTextFormatter.format(getItem(position).toString()));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setTextColor(textColor);
        return convertView;
    }
    public void setHeight(int height){
        itemHeight = height;
    }
    public void setTextSize(Float size){
        textSize = size;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }

    void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public abstract T getItemInDataset(int position);

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public abstract T getItem(int position);

    @Override public abstract int getCount();

    static class ViewHolder {
        TextView textView;

        ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }
}
