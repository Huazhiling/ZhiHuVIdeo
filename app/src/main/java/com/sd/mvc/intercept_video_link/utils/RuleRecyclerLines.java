package com.sd.mvc.intercept_video_link.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ConvertUtils;

/**
 * Created by wwj on 2018/11/29.
 * 自定义的RecyclerView分割线
 */

public class RuleRecyclerLines extends RecyclerView.ItemDecoration {
    private final Paint mPaint;
    private Context mContext;
    private int mOrientation;
    public static final int HORIZONTAL_LIST = LinearLayout.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayout.VERTICAL;
    private int height = 0;

    //由于Divider也有长宽高，每一个Item需要向下或者向右偏移
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL_LIST) {
            //画横线，就是往下偏移一个分割线的高度
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.set(0, 0, 0, ConvertUtils.dp2px(height));
            }
        } else {
            //画竖线，就是往右偏移一个分割线的宽度
            outRect.set(0, 0, ConvertUtils.dp2px(height), 0);
        }
    }

    public RuleRecyclerLines(Context context, int orientation, int height) {
        this.mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFFF6F7FB);
        mPaint.setStyle(Paint.Style.FILL);
        setOrientation(orientation);
        this.height = height;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    //设置屏幕的方向
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL_LIST) {
            drawHorizontalLine(c, parent, state);
        } else {
            drawVerticalLine(c, parent, state);
        }
    }

    private void drawHorizontalLine(Canvas c, RecyclerView parent, RecyclerView.State state) {
        // 对于水平方向的分割线，两端的位置是不变的，可以直接通过RecyclerView来获取
        int left = parent.getPaddingLeft();
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        // 这里获取的是一屏的Item数量
        int childCount = parent.getChildCount();
        // 分割线从Item的底部开始绘制，且在最后一个Item底部不绘制
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            // 有的Item布局会设置layout_marginXXX
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + ConvertUtils.dp2px(height);
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int top = parent.getPaddingTop();
        int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View childAt = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childAt.getLayoutParams();
            int left = childAt.getRight() + layoutParams.rightMargin;
            int right = left + ConvertUtils.dp2px(height);
            c.drawRect(left, right, top, bottom, mPaint);
        }
    }
}
