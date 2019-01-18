package com.common

import android.content.Context
import java.lang.Exception

fun Context.storageList() : List<String>?{
    val storageManager = getSystemService(Context.STORAGE_SERVICE);
    try {
        val method = storageManager.javaClass.getMethod("getVolumePaths")
        val paths = method.invoke(storageManager) as? Array<*>
        return paths?.map {
            it as String
        }
    }catch (e: Exception){
        e.printStackTrace()
    }
    return null
}