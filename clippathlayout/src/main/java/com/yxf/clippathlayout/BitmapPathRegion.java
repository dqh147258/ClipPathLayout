package com.yxf.clippathlayout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;


public class BitmapPathRegion implements PathRegion {

    private Bitmap mBitmap;

    private final int mInSampleSize;

    public BitmapPathRegion(Path path, int clipType, int width, int height) {
        this(path, clipType, width, height, 16);
    }

    public BitmapPathRegion(Path path, int clipType, int width, int height, int inSampleSize) {
        mInSampleSize = inSampleSize;
        if (width > 0 && height > 0) {
            Bitmap in = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(in);
            Utils.clipPath(canvas, path, clipType);
            canvas.drawColor(Color.GREEN);
            Matrix matrix = new Matrix();
            matrix.setScale(1 / (inSampleSize * 1f), 1 / (inSampleSize * 1f));
            mBitmap = Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), matrix, false);
        }
    }


    @Override
    public boolean isInRegion(float x, float y) {
        if (x < 0 || y < 0) {
            return false;
        }
        if (mBitmap == null) {
            return false;
        }
        int px = (int) (x / mInSampleSize);
        int py = (int) (y / mInSampleSize);
        if (px >= mBitmap.getWidth() || py >= mBitmap.getHeight()) {
            return false;
        }
        return mBitmap.getPixel(px, py) == Color.GREEN;
    }
}
