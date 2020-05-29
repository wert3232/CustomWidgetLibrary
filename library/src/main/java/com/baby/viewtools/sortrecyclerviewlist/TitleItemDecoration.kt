package com.baby.viewtools.sortrecyclerviewlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.View

/**
 * 有分类title的 ItemDecoration
 */

class TitleItemDecoration(
    context: Context,
    private val mData: List<SortModel>,
    private var paramsBlock: Params.() -> Unit
) : RecyclerView.ItemDecoration() {
    inner class Params {
        var titleHeight = 30f
        var titleTextSize = 16f
        var titleBgColor = Color.parseColor("#FFDFDFDF")
        var titleTextColor = Color.parseColor("#FF000000")
    }

    private val style = Params().also {
        it.paramsBlock()
    }
    private val mBounds: Rect = Rect()
    private val mTitleHeight: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, style.titleHeight, context.resources.displayMetrics).toInt()
    private val mTitleTextSize: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, style.titleTextSize, context.resources.displayMetrics).toInt()
    private val mPaint = Paint().apply {
        textSize = mTitleTextSize.toFloat()
        isAntiAlias = true
    }

    init {

    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition
            if (position > -1) {
                if (position == 0) {//等于0的时候绘制title
                    drawTitle(c, left, right, child, params, position)
                } else {
                    if (mData[position].letters != mData[position - 1].letters) {
                        //字母不为空，并且不等于前一个，也要title
                        drawTitle(c, left, right, child, params, position)
                    }
                }
            }
        }
    }

    /**
     * 绘制Title区域背景和文字的方法
     * 最先调用，绘制最下层的title
     * @param c
     * @param left
     * @param right
     * @param child
     * @param params
     * @param position
     */
    private fun drawTitle(c: Canvas, left: Int, right: Int, child: View, params: RecyclerView.LayoutParams, position: Int) {
        mPaint.color = style.titleBgColor
        c.drawRect(left.toFloat(), (child.top - params.topMargin - mTitleHeight).toFloat(), right.toFloat(), (child.top - params.topMargin).toFloat(), mPaint)
        mPaint.color = style.titleTextColor

        mPaint.getTextBounds(mData[position].letters, 0, mData[position].letters.length, mBounds)
        c.drawText(mData[position].letters,
                child.paddingLeft.toFloat(),
                (child.top - params.topMargin - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(), mPaint)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val position =
            (parent.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).findFirstVisibleItemPosition()
        if (position == -1) return //在搜索到没有的索引的时候position可能等于-1，所以在这里判断一下
        val lettersTag = mData[position].letters
        val child = parent.findViewHolderForLayoutPosition(position)?.itemView ?: return
        //Canvas是否位移过的标志
        var flag = false
        if (position + 1 < mData.size) {
            //当前第一个可见的Item的字母索引，不等于其后一个item的字母索引，说明悬浮的View要切换了
            if (lettersTag != mData[position + 1].letters) {
                //当第一个可见的item在屏幕中剩下的高度小于title的高度时，开始悬浮Title的动画
                if (child.height + child.top < mTitleHeight) {
                    c.save()
                    flag = true
                    /**
                     * 下边的索引把上边的索引顶上去的效果
                     */
                    c.translate(0f, (child.height + child.top - mTitleHeight).toFloat())

                    /**
                     * 头部折叠起来的视效（下边的索引慢慢遮住上边的索引）
                     */
                    /*c.clipRect(parent.getPaddingLeft(),
                            parent.getPaddingTop(),
                            parent.getRight() - parent.getPaddingRight(),
                            parent.getPaddingTop() + child.getHeight() + child.getTop());*/
                }
            }
        }
        mPaint.color = style.titleBgColor
        c.drawRect(parent.paddingLeft.toFloat(),
                parent.paddingTop.toFloat(),
                (parent.right - parent.paddingRight).toFloat(),
                (parent.paddingTop + mTitleHeight).toFloat(), mPaint)
        mPaint.color = style.titleTextColor
        mPaint.getTextBounds(lettersTag, 0, lettersTag.length, mBounds)
        c.drawText(lettersTag, child.paddingLeft.toFloat(),
                (parent.paddingTop + mTitleHeight - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
                mPaint)
        if (flag) c.restore()//恢复画布到之前保存的状态

    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (position > -1) {
            //等于0的时候绘制title
            if (position == 0) {
                outRect.set(0, mTitleHeight, 0, 0)
            } else {
                if (mData[position].letters != mData[position - 1].letters) {
                    //字母不为空，并且不等于前一个，绘制title
                    outRect.set(0, mTitleHeight, 0, 0)
                } else {
                    outRect.set(0, 0, 0, 0)
                }
            }
        }
    }

}
