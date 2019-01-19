package com.yxf.clippathlayout.transition.generator;


import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.OvalPathGenerator;

public class OvalTransitionPathGenerator extends OvalPathGenerator implements TransitionPathGenerator {

    @Override
    public Rect maxContainSimilarRange(Rect viewRange) {
        int centerX = viewRange.centerX();
        int centerY = viewRange.centerY();
        int radiusX = (int) (viewRange.width() * Math.cos(Math.PI / 4) / 2);
        int radiusY = radiusX * viewRange.height() / viewRange.width();
        return new Rect(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
    }
}
