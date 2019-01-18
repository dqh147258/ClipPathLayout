package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.view.View;

public class RhombusPathGenerator implements PathGenerator {
    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (old == null) {
            old = new Path();
        } else {
            old.reset();
        }
        old.moveTo(width / 2, 0);
        old.lineTo(width, height / 2);
        old.lineTo(width / 2, height);
        old.lineTo(0, height / 2);
        old.close();
        return old;
    }
}
