package com.yxf.clippathlayout;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;

public interface ClipPathLayout {


    boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint);

    void applyPathInfo(PathInfo info);

    void cancelPathInfo(View child);

    void beforeDrawChild(Canvas canvas, View child, long drawingTime);

    void afterDrawChild(Canvas canvas, View child, long drawingTime);

    void notifyPathChanged(View child);

    void notifyAllPathChanged();

    void requestLayout();


}
