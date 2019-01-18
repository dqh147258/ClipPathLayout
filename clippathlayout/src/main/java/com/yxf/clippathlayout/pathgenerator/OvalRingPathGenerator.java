package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class OvalRingPathGenerator implements PathGenerator {

    private float mInSideRadiusRate;
    private int mStartAngle;
    private int mSweepAngle;

    private RectF mRectF = new RectF();

    private Path mPath = new Path();
    private float[] mPoint = new float[2];

    public OvalRingPathGenerator(float inSideRadiusRate, int startAngle, int sweepAngle) {
        mInSideRadiusRate = inSideRadiusRate;
        mStartAngle = startAngle;
        mSweepAngle = sweepAngle;
    }


    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (old == null) {
            old = new Path();
        } else {
            old.reset();
        }
        float centerX = width / 2;
        float centerY = height / 2;
        mRectF.set(0, 0, width, height);
        calculateCoordinate(width, height, mStartAngle, mPoint);
        if (mSweepAngle < 360) {
            old.moveTo(centerX, centerY);
            old.lineTo(mPoint[0] + centerX, mPoint[1] + centerY);
        } else {
            mSweepAngle = 360;
            old.moveTo(mPoint[0] + centerX, mPoint[1] + centerY);
        }
        old.arcTo(mRectF, mStartAngle, mSweepAngle);
        if (mSweepAngle < 360) {
            old.lineTo(centerX, centerY);
        }
        float radiusX = centerX * mInSideRadiusRate;
        float radiusY = centerY * mInSideRadiusRate;
        mRectF.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
        mPath.addOval(mRectF, Path.Direction.CW);
        old.op(mPath, Path.Op.DIFFERENCE);
        return old;
    }

    private void calculateCoordinate(int width, int height, int angle, float[] point) {
        if (angle % 180 == 90) {
            point[0] = 0;
            point[1] = (float) (height * Math.sin(Math.toRadians(angle)));
            return;
        }
        double radians = Math.toRadians(angle);
        point[0] = (float) (width / 2 * Math.cos(radians));
        point[1] = (float) (width / 2 * Math.sin(radians)) / width * height;
    }
}
