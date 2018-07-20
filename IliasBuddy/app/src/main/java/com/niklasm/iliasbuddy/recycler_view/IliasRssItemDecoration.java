package com.niklasm.iliasbuddy.recycler_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Based on the following tutorial:
 * https://www.androidhive.info/2017/11/android-recyclerview-with-search-filter-functionality/
 */
public class IliasRssItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    private final Drawable DIVIDER_DRAWABLE;

    public IliasRssItemDecoration(@NonNull final Context CONTEXT) {
        final TypedArray a = CONTEXT.obtainStyledAttributes(IliasRssItemDecoration.ATTRS);
        DIVIDER_DRAWABLE = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void onDrawOver(@NonNull final Canvas CANVAS, @NonNull final RecyclerView PARENT,
                           final RecyclerView.State STATE) {
        drawVertical(CANVAS, PARENT);
    }

    private void drawVertical(@NonNull final Canvas CANVAS, @NonNull final RecyclerView PARENT) {
        final int left = PARENT.getPaddingLeft();
        final int right = PARENT.getWidth() - PARENT.getPaddingRight();

        final int childCount = PARENT.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = PARENT.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + DIVIDER_DRAWABLE.getIntrinsicHeight();
            DIVIDER_DRAWABLE.setBounds(left, top, right, bottom);
            DIVIDER_DRAWABLE.draw(CANVAS);
        }
    }

    @Override
    public void getItemOffsets(@NonNull final Rect OUT_RECT, final View VIEW,
                               final RecyclerView PARENT, final RecyclerView.State STATE) {
        OUT_RECT.set(0, 0, 0, DIVIDER_DRAWABLE.getIntrinsicHeight());

    }
}
