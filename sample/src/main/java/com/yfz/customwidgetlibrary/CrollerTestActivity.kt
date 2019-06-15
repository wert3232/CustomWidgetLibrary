package com.yfz.customwidgetlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class CrollerTestActivity : AppCompatActivity() {
    val index by lazy {
        MutableLiveData<Int>().apply {
            value = 50
            observe(this@CrollerTestActivity, Observer {
                Log.e("test","index $it")
            })
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<com.yfz.customwidgetlibrary.databinding.ActivityCrollerTestBinding>(this@CrollerTestActivity, R.layout.activity_croller_test)
        binding.setVariable(BR.ui,this)
    }

}