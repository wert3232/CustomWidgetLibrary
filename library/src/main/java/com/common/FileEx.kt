package com.common

import android.content.Context
import android.os.storage.StorageManager
import java.lang.Exception
import java.lang.reflect.InvocationTargetException

const val STORAGE_NEITHER = 0
const val STORAGE_INTERNAL_SD = 1
const val STORAGE_EXTERNAL_SD = 2
data class StorageObject(var path: String,var isRemoveAble: Boolean,var isPrimary: Boolean)
fun checkStorageState(path: String){

}
inline fun Context.getStorageList() : List<StorageObject>?{
    val manager = this.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    var storageVolumeClazz: Class<*>? = null
    try {
        storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
        val getVolumeList = manager.javaClass.getMethod("getVolumeList")
        val getPath = storageVolumeClazz!!.getMethod("getPath")
        val isPrimary = storageVolumeClazz.getMethod("isPrimary")
        val isRemovable = storageVolumeClazz.getMethod("isRemovable")
        val result= getVolumeList.invoke(manager) as? Array<*>
        val length = result?.size ?: 0
        return result?.map {
            val storageVolumeElement = it
            val storage = StorageObject(
                    path = getPath.invoke(storageVolumeElement) as String,
                    isRemoveAble = isRemovable.invoke(storageVolumeElement) as Boolean,
                    isPrimary = isPrimary.invoke(storageVolumeElement) as Boolean
            )
            storage
        }

    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }
    return null
}