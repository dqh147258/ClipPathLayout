package com.yxf.clippathlayout.transition.generator;

import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.CirclePathGenerator;

public class CircleTransitionPathGenerator extends CirclePathGenerator implements TransitionPathGenerator {


    @Override
    public Rect maxContainSimilarRange(Rect viewRange) {
        int radius = Math.min(viewRange.width(), viewRange.height()) / 2;
        int radiusX = (int) (radius * Math.sqrt(2) / 2);
        int radiusY = (int) (radius * Math.sqrt(2) / 2);
        int centerX = viewRange.centerX();
        int centerY = viewRange.centerY();
        viewRange.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
        return viewRange;
    }
}
