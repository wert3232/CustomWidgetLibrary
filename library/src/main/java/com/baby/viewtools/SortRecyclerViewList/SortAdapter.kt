package com.baby.viewtools.SortRecyclerViewList

import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast


import com.library.R

class SortAdapter(private val context: Context, private var mData: List<SortModel>) : RecyclerView.Adapter<SortAdapter.ViewHolder>() {
    @LayoutRes
    var itemResLayout = R.layout.item_name
    private var mOnItemClickListener = {view: View, position: Int ->
        Toast.makeText(context, mData[position].name, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(itemResLayout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.tvName = view.findViewById<View>(R.id.tvName) as TextView
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            mOnItemClickListener.invoke(holder.itemView, position)
        }
        holder.tvName!!.text = this.mData[position].name
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    //**********************itemClick************************
    fun setOnItemClickListener(onItemClickListener: (view: View, position: Int) -> Unit) {
        this.mOnItemClickListener = onItemClickListener
    }
    //**************************************************************

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvName: TextView? = null
    }

    /**
     * 提供给Activity刷新数据
     * @param list
     */
    fun updateList(list: List<SortModel>) {
        this.mData = list
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Any {
        return mData[position]
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    fun getSectionForPosition(position: Int): Int {
        return mData[position].letters[0].toInt()
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    fun getPositionForSection(section: Int): Int {
        for (i in 0 until itemCount) {
            val sortStr = mData[i].letters
            val firstChar = sortStr.toUpperCase()[0]
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }

}
