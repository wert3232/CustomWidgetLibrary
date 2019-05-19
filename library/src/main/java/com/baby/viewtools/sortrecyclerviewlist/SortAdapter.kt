package com.baby.viewtools.sortrecyclerviewlist

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast


import com.library.R

open class SortAdapter(private val context: Context, private var mData: List<SortModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<SortAdapter.ViewHolder>() {
    @LayoutRes
    var itemResLayout = R.layout.item_name
    var selectPosition = -1
        set(value) {
            val old = field
            field = value
            if(old != -1){
                notifyItemChanged(old)
            }
        }
    private var onBindListener = { holder: ViewHolder, position: Int ->

    }
    private var mOnItemClickListener = { view: View, position: Int, mode: SortModel ->
        Toast.makeText(context,"id:${mode.id} name:${mData[position].name}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(itemResLayout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.tvName = view.findViewById<View>(R.id.tvName) as TextView
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            mOnItemClickListener.invoke(holder.itemView, position, getItem(position))
        }
        holder.tvName!!.text = this.mData[position].name
        onBindListener.invoke(holder,position)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    //**********************itemClick************************
    fun setOnItemClickListener(onItemClickListener: (view: View, position: Int, mode: SortModel) -> Unit) {
        this.mOnItemClickListener = onItemClickListener
    }
    fun setOnBindListener(onSelect: (holder: ViewHolder, position: Int) -> Unit){
        this.onBindListener = onSelect
    }
    //**************************************************************

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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

    fun getItem(position: Int): SortModel {
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
