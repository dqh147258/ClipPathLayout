package com.yxf.clippathlayout;

import android.graphics.Canvas;
import android.graphics.Path;
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


}
