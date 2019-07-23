package com.baby.viewtools.sortrecyclerviewlist

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import com.common.getFirstSpell
import com.common.toPingYin
import java.util.*

class SortRecyclerViewList(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : androidx.recyclerview.widget.RecyclerView(context, attrs, defStyleAttr) {
    inner class Builder {
        lateinit var dataList: List<SortModel>
        var titleClosure: TitleItemDecoration.Params.() -> Unit = {}
            private set
        var adapterClosure: () -> SortAdapter = {
            SortAdapter(context, dataList)
        }
            private set

        fun titleItemDecoration(init: TitleItemDecoration.Params.() -> Unit = {}) {
            titleClosure = init
        }

        fun adapter(sortAdapter: SortAdapter = adapterClosure(), init: SortAdapter.() -> Unit) {
            adapterClosure = {
                sortAdapter.init()
                sortAdapter
            }
        }
    }

    private val manager by lazy {
        LinearLayoutManager(context).apply {
            orientation = RecyclerView.VERTICAL
        }
    }
    private var sortList = mutableListOf<SortModel>()
    private lateinit var datalist: List<SortModel>
    var sortAdapter: SortAdapter? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    fun build(block: Builder.() -> Unit): SortRecyclerViewList {
        val builder = Builder()
        builder.block()
        this.datalist = builder.dataList.also {
            Collections.sort<SortModel>(it, PinyinComparator())
        }
        this.sortList.addAll(datalist)
        this.sortAdapter = builder.adapterClosure()
        adapter = sortAdapter
        layoutManager = manager
        //如果add两个，那么按照先后顺序，依次渲染。
        addItemDecoration(TitleItemDecoration(context, sortList, builder.titleClosure))
        addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        return this
    }

    //导航
    fun navigateLetter(letter: String): SortRecyclerViewList {
        val position = sortAdapter?.getPositionForSection(letter[0].toInt()) ?: -1
        if (position != -1) {
            manager.scrollToPositionWithOffset(position, 0)
        }
        return this
    }

    /**
     * 根据输入中的值来过滤数据并更新RecyclerView
     *
     * @param filterStr
     */
    fun searchData(filterStr: String): SortRecyclerViewList {
        if (TextUtils.isEmpty(filterStr)) {
            sortList.clear()
            sortList.addAll(datalist)
        } else {
            val filterDateList: MutableList<SortModel> = ArrayList()
            for (sortModel in sortList) {
                val name = sortModel.name
                if (name.contains(filterStr) ||
                        name.getFirstSpell().startsWith(filterStr)
                        //不区分大小写
                        || name.getFirstSpell().toLowerCase().startsWith(filterStr)
                        || name.getFirstSpell().toUpperCase().startsWith(filterStr)) {
                    filterDateList.add(sortModel)
                }
            }
            // 根据a-z进行排序
            Collections.sort(filterDateList, PinyinComparator())
            sortList.clear()
            sortList.addAll(filterDateList)
        }
        adapter?.notifyDataSetChanged()
        return this
    }
}

data class SortModel(val id: Int, var name: String, var letters: String, var bundle: Bundle? = null) {
    companion object {
        fun filledData(date: Array<String>): MutableList<SortModel> {
            val sortList = ArrayList<SortModel>()
            for (i in date.indices) {
                val name = date[i]
                //汉字转换成拼音
                val sortString = name.toPingYin().substring(0, 1).toUpperCase()
                // 正则表达式，判断首字母是否是英文字母
                val letters = if (sortString.matches("[A-Z]".toRegex())) {
                    sortString.toUpperCase()
                } else {
                    "#"
                }
                sortList.add(SortModel(i, name, letters))
            }
            return sortList
        }

        fun filledData(id: Int, name: String, bundle: Bundle? = null): SortModel {
            //汉字转换成拼音
            val sortString = name.toPingYin().substring(0, 1).toUpperCase()
            // 正则表达式，判断首字母是否是英文字母
            val letters = if (sortString.matches("[A-Z]".toRegex())) {
                sortString.toUpperCase()
            } else {
                "#"
            }
            return SortModel(id, name, letters, bundle)
        }

    }
}

class PinyinComparator : Comparator<SortModel> {
    override fun compare(o1: SortModel, o2: SortModel): Int {
        return when {
            o1.letters == "@" || o2.letters == "#" -> 1
            o1.letters == "#" || o2.letters == "@" -> -1
            else -> o1.letters.compareTo(o2.letters)
        }
    }
}