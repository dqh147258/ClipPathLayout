package com.yxf.clippathlayout.transition.generator;

import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.PathGenerator;

public interface TransitionPathGenerator extends PathGenerator {

    /*
     * 返回path在viewRange中包含的最大的和viewRange相似的的矩形区域
     */
    Rect maxContainSimilarRange(Rect viewRange);

}
