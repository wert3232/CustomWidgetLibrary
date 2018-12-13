package com.yfz.customwidgetlibrary

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import com.baby.viewtools.sortrecyclerviewlist.SortModel
import kotlinx.android.synthetic.main.www2.*

/*class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        *//*axis.setValueChangeListener(object : AxisView.OnValueChangeListener{
            override fun onChange(x: Int, y: Int, axisLength: Int) {
                Log.e("www2","$x    $y    $axisLength")
            }
        })*//*
    }
}*/

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.www2)
        sortRecyclerView.build{
            dataList = resources.getStringArray(R.array.date).mapIndexed { index, s ->
                SortModel.filledData(id = index,name = s)
            }
            titleItemDecoration {
                titleBgColor = ContextCompat.getColor(this@MainActivity,R.color.colorAccent)
                titleTextSize = 16f
            }
            adapter{
                itemResLayout = R.layout.item_name
            }
        }

        filter_edit.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable) {
                sortRecyclerView.searchData(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        sideBar.setOnTouchLetterChangeListener {
            sortRecyclerView.navigateLetter(it)
        }
    }
}
