package com.common

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 方法类 反射
 * @author syh
 */

object ReflectionUtils {

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */

    fun getDeclaredMethod(`object`: Any, methodName: String, vararg parameterTypes: Class<*>): Method? {
        var method: Method? = null

        var clazz: Class<*> = `object`.javaClass
        while (clazz != Any::class.java) {
            try {
                method = clazz.getDeclaredMethod(methodName, *parameterTypes)
                return method
            } catch (e: Exception) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }

            clazz = clazz.superclass
        }

        return null
    }

    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected, default)
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @param parameters : 父类中的方法参数
     * @return 父类中方法的执行结果
     */

    fun invokeMethod(`object`: Any, methodName: String, parameterTypes: Array<Class<*>>,
                     parameters: Array<Any>): Any? {
        //根据 对象、方法名和对应的方法参数 通过反射 调用上面的方法获取 Method 对象
        val method = getDeclaredMethod(`object`, methodName, *parameterTypes)

        //抑制Java对方法进行检查,主要是针对私有方法而言
        method!!.isAccessible = true

        try {
            if (null != method) {

                //调用object 的 method 所代表的方法，其方法的参数是 parameters
                return method.invoke(`object`, *parameters)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */

    fun getDeclaredField(`object`: Any, fieldName: String): Field? {
        var field: Field? = null

        var clazz: Class<*> = `object`.javaClass

        while (clazz != Any::class.java) {
            try {
                field = clazz.getDeclaredField(fieldName)
                return field
            } catch (e: Exception) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }

            clazz = clazz.superclass
        }

        return null
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @param value : 将要设置的值
     */

    fun setFieldValue(`object`: Any, fieldName: String, value: Any) {

        //根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        val field = getDeclaredField(`object`, fieldName)

        //抑制Java对其的检查
        field!!.isAccessible = true

        try {
            //将 object 中 field 所代表的值 设置为 value
            field.set(`object`, value)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return : 父类中的属性值
     */

    fun getFieldValue(`object`: Any, fieldName: String): Any? {

        //根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        val field = getDeclaredField(`object`, fieldName)

        //抑制Java对其的检查
        field!!.isAccessible = true

        try {
            //获取 object 中 field 所代表的属性值
            return field.get(`object`)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}

