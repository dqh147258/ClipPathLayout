package com.yxf.clippathlayout.transition.generator;

import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.RhombusPathGenerator;

public class RhombusTransitionPathGenerator extends RhombusPathGenerator implements TransitionPathGenerator {
    @Override
    public Rect maxContainSimilarRange(Rect viewRange) {
        int offsetX = viewRange.width() / 4;
        int offsetY = viewRange.height() / 4;
        int left = viewRange.left + offsetX;
        int right = viewRange.right - offsetX;
        int top = viewRange.top + offsetY;
        int bottom = viewRange.bottom - offsetY;
        viewRange.set(left, top, right, bottom);
        return viewRange;
    }
}
