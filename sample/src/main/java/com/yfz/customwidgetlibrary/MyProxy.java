package com.yfz.customwidgetlibrary;


import android.content.Context;
import android.util.Log;

import leo.android.cglib.proxy.Enhancer;
import leo.android.cglib.proxy.MethodInterceptor;
import leo.android.cglib.proxy.MethodProxy;

public class MyProxy implements MethodInterceptor {

    private Context context;

    public MyProxy(Context context) {
        this.context = context;
    }

    public Object getProxy(Class cls) {
        Enhancer e = new Enhancer(context);
        e.setSuperclass(cls);
        e.setInterceptor(this);
        return e.create();
    }

    @Override
    public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
        Log.d("test","begin print " + object.getClass().getName() + "  mothed:" + methodProxy.getMethodName());
        Object result = methodProxy.invokeSuper(object, args);
        Log.d("test","end print");
        return result;
    }

}