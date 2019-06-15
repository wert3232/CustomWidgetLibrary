package com.yfz.customwidgetlibrary;

import android.util.Log;

public class TestBean {
    private int i = 0;
    public TestBean(int i){
        this.i = i;
    }
    public void hello(){
        Log.e("test", "hello world" + i);
    }
}
