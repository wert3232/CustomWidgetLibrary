package com.yfz.customwidgetlibrary

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.wesail.balanceview.AxisView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*axis.setValueChangeListener(object : AxisView.OnValueChangeListener{
            override fun onChange(x: Int, y: Int, axisLength: Int) {
                Log.e("www2","$x    $y    $axisLength")
            }
        })*/
    }
}
