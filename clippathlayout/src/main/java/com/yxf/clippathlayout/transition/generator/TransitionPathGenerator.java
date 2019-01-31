package com.yxf.clippathlayout.transition.generator;

import android.graphics.Rect;

import com.yxf.clippathlayout.pathgenerator.PathGenerator;

public interface TransitionPathGenerator extends PathGenerator {

    /**
     * @param similar 相似矩形参考
     * @param boundWidth Path的范围区域宽
     * @param boundHeight Path的范围区域高
     * @return 返回最大的和@param similar相似的的矩形区域,
     * 返回的矩形区域中心必须是Path的中心,即(boundWidth/2,boundHeight/2),
     * 为了尽量减少内存抖动,建议使用参数传入的矩形修改数值后返回
     */
    Rect maxContainSimilarRange(Rect similar, int boundWidth, int boundHeight);

}
