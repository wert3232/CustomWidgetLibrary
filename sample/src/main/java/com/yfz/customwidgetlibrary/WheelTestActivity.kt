package com.yfz.customwidgetlibrary

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.yfz.customwidgetlibrary.databinding.ActivityMain2Binding
import com.yfz.customwidgetlibrary.databinding.ActivityWheelTestBinding

class WheelTestActivity : AppCompatActivity() {
    val index by lazy {
        MutableLiveData<Int>().apply {
            value = 50
            observe(this@WheelTestActivity, Observer {
                Log.e("test","index $it")
            })
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityWheelTestBinding>(this@WheelTestActivity, R.layout.activity_wheel_test)
        binding.setVariable(BR.ui,this)
    }

}