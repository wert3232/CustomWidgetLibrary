package com.yfz.customwidgetlibrary;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import leo.android.cglib.proxy.Enhancer;
import leo.android.cglib.proxy.MethodInterceptor;
import leo.android.cglib.proxy.MethodProxy;


public class CGlibProxyFactory implements MethodInterceptor {
    private Object targetObject;


    public Object createProxyInstance(Context context, Object targetObject) {
        this.targetObject = targetObject;//传入用户类

        Enhancer enhancer = new Enhancer(context);//Enhancer是cglib的核心类
        enhancer.setSuperclass(this.targetObject.getClass());
        enhancer.setInterceptor(this);
        return enhancer.create(); //生成代理对象
    }

    @Override
    public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
        Log.e("test","begin print " + object.getClass().getName() + "  mothed:" + methodProxy.getMethodName());
        TestBean bean = (TestBean) this.targetObject;
//        Object result = methodProxy.invokeSuper(object, args);
        Object result = methodProxy.invokeSuper(bean, args);
        Log.e("test","end " + result);
//        Object result = methodProxy.getProxyMethod().invoke(bean,targetObject);
//        if (!methodProxy.getMethodName().equals("hello")) {
//            Log.e("test","list is null,method is stoped");
//        } else {
//            //执行代理方法,传入实例和方法参数
//            result = methodProxy.invokeSuper(targetObject, objects);
//        }
        return result;
    }
}
