package com.yxf.clippathlayout;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;

public class Utils {

    private static final String TAG = getTAG(Utils.class);

    public static boolean DEUBG = false;

    public static void clipOutPath(Canvas canvas, Path path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(path);
        } else {
            canvas.clipPath(path, Region.Op.DIFFERENCE);
        }
    }

    public static void clipPath(Canvas canvas, Path path, int clipType) {
        if (clipType == PathInfo.CLIP_TYPE_IN) {
            canvas.clipPath(path);
        } else if (clipType == PathInfo.CLIP_TYPE_OUT) {
            clipOutPath(canvas, path);
        } else {
            Log.e(TAG, "clipPath: unsupported clip type : " + clipType);
        }
    }

    public static boolean isInUiThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static boolean isViewCanUse(View view) {
        return isInUiThread() && (view.getWidth() > 0);
    }

    public static void runOnUiThreadAfterViewCanUse(View view, Runnable runnable) {
        if (isViewCanUse(view)) {
            runnable.run();
        } else {
            view.post(runnable);
        }
    }

    public static String getTAG(Class c) {
        return Config.libTAG + "." + c.getSimpleName();
    }


    public static Rect maxContainSimilarRange(Path path, Rect similar, int boundWidth, int boundHeight) {
        PathRegion region = new NativePathRegion(path, PathInfo.CLIP_TYPE_IN);
        if (isRectInRegion(region, similar)) {
            return similar;
        }
        Rect result = similar;
        int centerX = boundWidth / 2;
        int centerY = boundHeight / 2;

        int outLeft, outTop, outRight, outBottom;
        int inLeft, inTop, inRight, inBottom;
        int left, top, right, bottom;
        outLeft = similar.left;
        outTop = similar.top;
        outRight = similar.right;
        outBottom = similar.bottom;
        inLeft = centerX;
        inTop = centerY;
        inRight = centerX;
        inBottom = centerY;
        while (true) {
            left = (outLeft + inLeft) / 2;
            top = (outTop + inTop) / 2;
            right = (outRight + inRight) / 2;
            bottom = (outBottom + inBottom) / 2;
            if (isRectInRegion(region, left, top, right, bottom)) {
                inLeft = left;
                inTop = top;
                inRight = right;
                inBottom = bottom;
            } else {
                outLeft = left;
                outTop = top;
                outRight = right;
                outBottom = bottom;
            }
            if (Math.abs(outLeft - inLeft) <= 1 &&
                    Math.abs(outTop - inTop) <= 1 &&
                    Math.abs(outRight - inRight) <= 1 &&
                    Math.abs(outBottom - inBottom) <= 1) {
                result.set(inLeft, inTop, inRight, inBottom);
                break;
            }
        }
        return result;
    }

    public static boolean isRectInRegion(PathRegion region, Rect rect) {
        return isRectInRegion(region, rect.left, rect.top, rect.right, rect.bottom);
    }

    public static boolean isRectInRegion(PathRegion region, int left, int top, int right, int bottom) {
        return (region.isInRegion(left, top) &&
                region.isInRegion(right, top) &&
                region.isInRegion(left, bottom) &&
                region.isInRegion(right, bottom));
    }


}
