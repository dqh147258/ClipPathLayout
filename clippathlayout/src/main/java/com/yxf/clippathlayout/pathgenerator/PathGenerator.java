package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.view.View;

public interface PathGenerator {

    /**
     * @param old 以前使用过的Path,如果以前为null,则可能为null
     * @param view Path关联的子View对象
     * @param width 生成Path所限定的范围宽度,一般是子View宽度
     * @param height 生成Path所限定的范围高度,一般是子View高度
     * @return 返回一个Path对象,必须为闭合的Path,将用于裁剪子View
     *
     * 其中Path的范围即left : 0 , top : 0 , right : width , bottom : height
     */
    Path generatePath(Path old, View view, int width, int height);

}
