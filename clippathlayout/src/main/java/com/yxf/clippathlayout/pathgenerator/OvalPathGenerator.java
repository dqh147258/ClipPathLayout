package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;

public class OvalPathGenerator implements PathGenerator {
    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (old == null) {
            old = new Path();
        } else {
            old.reset();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            old.addOval(0, 0, width, height, Path.Direction.CW);
        } else {
            old.addOval(new RectF(0, 0, width, height), Path.Direction.CW);
        }
        return old;
    }
}
