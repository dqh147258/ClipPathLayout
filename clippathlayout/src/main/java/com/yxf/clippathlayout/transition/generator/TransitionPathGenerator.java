package com.yxf.clippathlayout.transition.generator;

import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.PathGenerator;

public interface TransitionPathGenerator extends PathGenerator {

    /*
     * 返回path在viewRange中包含的最大的和viewRange相似的的矩形区域,
     * 返回的矩形区域中心需要和参数矩形区域中心一致,
     * 为了尽量减少内存抖动,建议使用参数传入的矩形修改数值后返回
     */
    Rect maxContainSimilarRange(Rect viewRange);

}
