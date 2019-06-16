package com.yfz.customwidgetlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrollerTestActivity : AppCompatActivity(),CoroutineScope by MainScope(){
    val index by lazy {
        MutableLiveData<Int>().apply {
            value = 0
            observe(this@CrollerTestActivity, Observer {
                Log.e("test","index $it")
            })
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<com.yfz.customwidgetlibrary.databinding.ActivityCrollerTestBinding>(this@CrollerTestActivity, R.layout.activity_croller_test)
        binding.setVariable(BR.ui,this)
        binding.lifecycleOwner = this
        launch {
            repeat(10){
                delay(2000)
                index.value  = index.value!! + 5
            }
        }
    }

}