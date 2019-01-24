package com.yxf.clippathlayout;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

public class NativePathRegion implements PathRegion {

    Region mRegion = new Region();

    private int mCLipType;

    public NativePathRegion(Path path, int clipType) {
        mCLipType = clipType;
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        mRegion.setPath(path, new Region((int) bounds.left,
                (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
    }

    @Override
    public boolean isInRegion(float x, float y) {
        return mRegion.contains((int) x, (int) y) ^ (mCLipType == PathInfo.CLIP_TYPE_OUT);
    }
}
