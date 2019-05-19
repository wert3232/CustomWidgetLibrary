package com.yfz.customwidgetlibrary

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.qqtheme.framework.picker.FilePicker
import cn.qqtheme.framework.util.StorageUtils
import com.baby.viewtools.ConfirmDialog
import com.baby.viewtools.sortrecyclerviewlist.SortModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.www2.*
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class MainActivity : AppCompatActivity() {

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
}

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
