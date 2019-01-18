package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.view.Gravity;
import android.view.View;

public class CirclePathGenerator implements PathGenerator {

    private int mGravity;

    public CirclePathGenerator() {
        this(Gravity.CENTER);
    }

    public CirclePathGenerator(int gravity) {
        mGravity = gravity;
    }

    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (old == null) {
            old = new Path();
        }
        old.reset();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2;
        if (height > width) {
            switch (mGravity) {
                case Gravity.TOP:
                    centerY = centerX;
                    break;
                case Gravity.BOTTOM:
                    centerY = height - centerX;
                    break;

            }
        } else if (height < width) {
            switch (mGravity) {
                case Gravity.LEFT:
                    centerX = centerY;
                    break;
                case Gravity.RIGHT:
                    centerX = width - centerY;
                    break;
            }
        }
        return generateCirclePath(old, centerX, centerY, radius);
    }

    private Path generateCirclePath(Path old, int centerX, int centerY, int radius) {
        old.addCircle(centerX, centerY, radius, Path.Direction.CW);
        return old;
    }
}
