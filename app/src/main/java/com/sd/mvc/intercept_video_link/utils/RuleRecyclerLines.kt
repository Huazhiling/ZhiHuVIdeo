package com.sd.mvc.intercept_video_link.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout


/**Å
 * Created by wwj on 2018/11/29.
 * 自定义的RecyclerView分割线
 */

class RuleRecyclerLines(private val mContext: Context, orientation: Int, height: Int) : RecyclerView.ItemDecoration() {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mOrientation: Int = 0
    private var height = 0

    //由于Divider也有长宽高，每一个Item需要向下或者向右偏移
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (mOrientation == HORIZONTAL_LIST) {
            //画横线，就是往下偏移一个分割线的高度
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.set(0, 0, 0, ConvertUtils.dp2px(height))
            }
        } else {
            //画竖线，就是往右偏移一个分割线的宽度
            outRect.set(0, 0, ConvertUtils.dp2px(height), 0)
        }
    }

    init {
        mPaint.color = -0x90805
        mPaint.style = Paint.Style.FILL
        setOrientation(orientation)
        this.height = height
    }

    fun setColor(color: Int) {
        mPaint.color = color
    }

    //设置屏幕的方向
    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mOrientation == HORIZONTAL_LIST) {
            drawHorizontalLine(c, parent, state)
        } else {
            drawVerticalLine(c, parent, state)
        }
    }

    private fun drawHorizontalLine(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        // 对于水平方向的分割线，两端的位置是不变的，可以直接通过RecyclerView来获取
        val left = parent.paddingLeft
        val right = parent.measuredWidth - parent.paddingRight
        // 这里获取的是一屏的Item数量
        val childCount = parent.childCount
        // 分割线从Item的底部开始绘制，且在最后一个Item底部不绘制
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            // 有的Item布局会设置layout_marginXXX
            val top = child.bottom + layoutParams.bottomMargin
            val bottom = top + ConvertUtils.dp2px(height)
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
        }
    }

    private fun drawVerticalLine(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val top = parent.paddingTop
        val bottom = parent.measuredHeight - parent.paddingBottom
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val childAt = parent.getChildAt(i)
            val layoutParams = childAt.layoutParams as RecyclerView.LayoutParams
            val left = childAt.right + layoutParams.rightMargin
            val right = left + ConvertUtils.dp2px(height)
            c.drawRect(left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat(), mPaint)
        }
    }

    companion object {
        val HORIZONTAL_LIST = LinearLayout.HORIZONTAL
        val VERTICAL_LIST = LinearLayout.VERTICAL
    }
}
