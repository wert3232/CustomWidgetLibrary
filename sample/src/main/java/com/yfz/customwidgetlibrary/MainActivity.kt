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
import com.yfz.customwidgetlibrary.databinding.ActivityMain2Binding

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMain2Binding>(this@MainActivity, R.layout.activity_main_2)

        val factory = CGlibProxyFactory()
        val testBean = factory.createProxyInstance(this,TestBean(2)) as TestBean
        testBean.hello()
        /*val start = System.currentTimeMillis()
        val printer = MyProxy(this).getProxy(Printer::class.java) as Printer
        Log.e("Test","take time ${System.currentTimeMillis() - start}")
        printer.print()*/
    }

}
/*class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test.setOnClickListener {
            Toast.makeText(this@MainActivity,"111",Toast.LENGTH_LONG).show()
            FilePicker(this@MainActivity, FilePicker.FILE).apply {
                setShowHideDir(false)
                setRootPath(StorageUtils.getExternalRootPath() + "/")
                setAllowExtensions(arrayOf(".txt"))
                setItemHeight(30)
                setFillScreen(true)
                setOnFilePickListener { currentPath ->
                    Toast.makeText(this@MainActivity,currentPath,Toast.LENGTH_LONG).show()
                }
            }.show()
        }
    }
}*/

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

/*
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
                itemResLayout = R.layout.item_name2
                setOnItemClickListener { view, position, mode ->

                }
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
*/
