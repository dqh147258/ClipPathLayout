package com.yxf.clippathlayout.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.yxf.clippathlayout.ClipPathLayout;
import com.yxf.clippathlayout.ClipPathLayoutDelegate;
import com.yxf.clippathlayout.PathInfo;

public class ClipPathLinearLayout extends LinearLayout implements ClipPathLayout {

    ClipPathLayoutDelegate mClipPathLayoutDelegate = new ClipPathLayoutDelegate(this);


    public ClipPathLinearLayout(Context context) {
        this(context, null);
    }

    public ClipPathLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipPathLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        return mClipPathLayoutDelegate.isTransformedTouchPointInView(x, y, child, outLocalPoint);
    }

    @Override
    public void applyPathInfo(PathInfo info) {
        mClipPathLayoutDelegate.applyPathInfo(info);
    }

    @Override
    public void cancelPathInfo(View child) {
        mClipPathLayoutDelegate.cancelPathInfo(child);
    }

    @Override
    public void beforeDrawChild(Canvas canvas, View child, long drawingTime) {
        mClipPathLayoutDelegate.beforeDrawChild(canvas, child, drawingTime);
    }

    @Override
    public void afterDrawChild(Canvas canvas, View child, long drawingTime) {
        mClipPathLayoutDelegate.afterDrawChild(canvas, child, drawingTime);
    }

    //the drawChild method is not belong to ClipPathLayout ,
    //but you should rewrite it without changing the return value of the method
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        beforeDrawChild(canvas, child, drawingTime);
        boolean result = super.drawChild(canvas, child, drawingTime);
        afterDrawChild(canvas, child, drawingTime);
        return result;
    }

    //do not forget to rewrite the method
    @Override
    public void requestLayout() {
        super.requestLayout();
        // the request layout method would be invoked in the constructor of super class
        if (mClipPathLayoutDelegate == null) {
            return;
        }
        mClipPathLayoutDelegate.requestLayout();
    }

    @Override
    public void notifyPathChanged(View child) {
        mClipPathLayoutDelegate.notifyPathChanged(child);
    }

    @Override
    public void notifyAllPathChanged() {
        mClipPathLayoutDelegate.notifyAllPathChanged();
    }
}
