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
import com.yfz.customwidgetlibrary.databinding.ActivitySeekbarTestBinding
import com.yfz.customwidgetlibrary.databinding.ActivityWheelTestBinding

class SeekBarTestActivity : AppCompatActivity() {
    val index by lazy {
        MutableLiveData<Int>().apply {
            value = 50
            observe(this@SeekBarTestActivity, Observer {
                Log.e("test","index $it")
            })
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySeekbarTestBinding>(this@SeekBarTestActivity, R.layout.activity_seekbar_test)
        binding.setVariable(BR.ui,this)
    }

}