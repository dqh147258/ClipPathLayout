package com.yxf.clippathlayout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ClipPathLayoutDelegate implements ClipPathLayout {

    private static final String TAG = Utils.getTAG(ClipPathLayoutDelegate.class);

    ViewGroup mParent;

    private HashMap<ViewKey, PathInfo> mPathInfoMap = new HashMap<ViewKey, PathInfo>();

    private PathRegionGenerator mPathRegionGenerator = PathRegionGenerators.createNativePathRegionGenerator();

    // Lazily-created holder for point computations.
    private float[] mTempPoint;

    private Matrix mTempMatrix;

    private ViewGetKey mTempViewGetKey;

    private Paint mPathPaint;
    private Paint mBitmapPaint;

    private PorterDuffXfermode mDstInMode;
    private PorterDuffXfermode mDstOutMode;

    private DrawFilter mOriginalDrawFilter;
    private Integer mOriginalLayerType;

    private int mCanvasSavedCount;

    private DrawFilter mAntiAliasDrawFilter;

    private boolean mHasLayoutRequest = false;

    private boolean mInBeforeDrawChild = false;

    private Queue<Runnable> mPendingTaskQueue = new LinkedList<Runnable>();

    private Runnable mBeforeDrawTask = new Runnable() {
        @Override
        public void run() {
            executePendingTask();
        }
    };

    public ClipPathLayoutDelegate(ViewGroup parent) {
        if (parent == null) {
            throw new NullPointerException("parent is a null value");
        }
        mParent = parent;
    }

    public ClipPathLayoutDelegate(ViewGroup parent, PathRegionGenerator generator) {
        this(parent);
        if (generator != null) {
            mPathRegionGenerator = generator;
        }
    }

    @Override
    public boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        final float[] point = getTempPoint();
        point[0] = x;
        point[1] = y;
        transformPointToViewLocal(point, child);
        boolean isInView = pointInView(child, point[0], point[1]);
        if (isInView) {
            ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
            PathInfo info = mPathInfoMap.get(key);
            if (info != null) {
                if ((info.getApplyFlag() & PathInfo.APPLY_FLAG_TOUCH_ONLY) != 0) {
                    PathRegion region = info.getPathRegion();
                    if (region != null) {
                        if (!region.isInRegion(point[0], point[1])) {
                            isInView = false;
                        }
                    }
                }
            }
            resetTempViewGetKey();
        }

        if (isInView && outLocalPoint != null) {
            outLocalPoint.set(point[0], point[1]);
        }
        return isInView;
    }

    private boolean pointInView(View child, float x, float y) {
        return x > 0 && y > 0 && x < (child.getRight() - child.getLeft()) &&
                y < (child.getBottom() - child.getTop());
    }

    private void transformPointToViewLocal(float[] point, View child) {
        point[0] += mParent.getScrollX() - child.getLeft();
        point[1] += mParent.getScrollY() - child.getTop();
        Matrix matrix = child.getMatrix();
        if (!matrix.isIdentity()) {
            Matrix invert = getTempMatrix();
            boolean result = matrix.invert(invert);
            if (result) {
                invert.mapPoints(point);
            }
        }
    }

    @Override
    public void applyPathInfo(final PathInfo info) {
        runBeforeDrawChild(new Runnable() {
            @Override
            public void run() {
                removeDeletedViewPathInfo();
                if (info.getView() == null) {
                    Log.e(TAG, "applyPathInfo: apply path info failed ,the view of info is null");
                    return;
                }
                mPathInfoMap.put(new ViewKey(info.hashCode(), info.getView()), info);
                notifyPathChangedInternal(info.getView());
            }
        });
    }

    @Override
    public void cancelPathInfo(View child) {
        cancelPathInfoInternal(child);
    }

    private void cancelPathInfoInternal(final View child) {
        if (child == null) {
            Log.e(TAG, "cancelPathInfo: child is null");
            return;
        }
        runBeforeDrawChild(new Runnable() {
            @Override
            public void run() {
                ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
                mPathInfoMap.remove(key);
                mParent.invalidate();
            }
        });
    }

    @Override
    public void beforeDrawChild(Canvas canvas, View child, long drawingTime) {
        if (mInBeforeDrawChild) {
            Log.e(TAG, "beforeDrawChild: can not recursive call this method");
            return;
        }
        mInBeforeDrawChild = true;
        executePendingTask();
        if (mHasLayoutRequest) {
            mHasLayoutRequest = false;
            notifyAllPathChangedInternal(false);
        }

        ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
        PathInfo info = mPathInfoMap.get(key);
        if (info != null && info.isAntiAlias()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCanvasSavedCount = canvas.saveLayer(child.getLeft(), child.getTop(), child.getRight(), child.getBottom(), null);
            } else {
                mCanvasSavedCount = canvas.saveLayer(child.getLeft(), child.getTop(), child.getRight(), child.getBottom(), null, Canvas.ALL_SAVE_FLAG);
            }
        } else {
            mCanvasSavedCount = canvas.save();
        }
        if (info != null) {
            if (!info.isAntiAlias()) {
                if ((info.getApplyFlag() & PathInfo.APPLY_FLAG_DRAW_ONLY) != 0) {
                    Path path = info.getPath();
                    if (path != null) {
                        canvas.translate(child.getLeft(), child.getTop());
                        Utils.clipPath(canvas, path, info.getClipType());
                        canvas.translate(-child.getLeft(), -child.getTop());
                    } else {
                        Log.d(TAG, "beforeDrawChild: path is null , hash code : " + info.hashCode());
                    }
                }
            } else {
                mOriginalDrawFilter = canvas.getDrawFilter();
                mOriginalLayerType = mParent.getLayerType();
                mParent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                if (mAntiAliasDrawFilter == null) {
                    mAntiAliasDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
                }
                canvas.setDrawFilter(mAntiAliasDrawFilter);
            }
        }
        resetTempViewGetKey();
        mInBeforeDrawChild = false;
    }

    @Override
    public void afterDrawChild(Canvas canvas, View child, long drawingTime) {
        ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
        PathInfo info = mPathInfoMap.get(key);
        if (info != null && info.isAntiAlias()) {
            if ((info.getApplyFlag() & PathInfo.APPLY_FLAG_DRAW_ONLY) != 0) {
                Path path = info.getPath();
                if (path != null) {
                    Bitmap bitmap = Bitmap.createBitmap(child.getWidth(), child.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bitmap);
                    if (mPathPaint == null) {
                        mPathPaint = new Paint();
                        mPathPaint.setAntiAlias(true);
                    }
                    if (mBitmapPaint == null) {
                        mBitmapPaint = new Paint();
                        mPathPaint.setAntiAlias(true);
                    }
                    c.drawPath(path, mPathPaint);
                    if (info.getClipType() == PathInfo.CLIP_TYPE_IN) {
                        if (mDstInMode == null) {
                            mDstInMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
                        }
                        mBitmapPaint.setXfermode(mDstInMode);
                    } else {
                        if (mDstOutMode == null) {
                            mDstOutMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
                        }
                        mBitmapPaint.setXfermode(mDstOutMode);
                    }
                    canvas.drawBitmap(bitmap, child.getLeft(), child.getTop(), mBitmapPaint);
                    //if use drawPath may cause some strange problems
                    /*canvas.translate(child.getLeft(), child.getTop());
                    canvas.drawPath(path, mBitmapPaint);
                    canvas.translate(-child.getLeft(),-child.getTop());*/
                } else {
                    Log.d(TAG, "beforeDrawChild: path is null , hash code : " + info.hashCode());
                }
            }
            mParent.setLayerType(mOriginalLayerType, null);
            canvas.setDrawFilter(mOriginalDrawFilter);
        }
        resetTempViewGetKey();
        canvas.restoreToCount(mCanvasSavedCount);
    }

    @Override
    public void notifyPathChanged(View child) {
        notifyPathChangedInternal(child);
    }

    @Override
    public void notifyAllPathChanged() {
        notifyAllPathChangedInternal(true);
    }

    private void notifyAllPathChangedInternal(final boolean reDraw) {
        runBeforeDrawChild(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<ViewKey, PathInfo>> iterator = mPathInfoMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    PathInfo info = iterator.next().getValue();
                    if (info != null) {
                        if (info.getView() != null) {
                            updatePath(info);
                            if (reDraw) {
                                //info.getView().invalidate() the method can not refresh view info of path ,
                                //mParent.invalidate() instead
                                mParent.invalidate();
                            }
                        } else {
                            iterator.remove();
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
        });
    }

    @Override
    public void requestLayout() {
        mHasLayoutRequest = true;
    }

    private void notifyPathChangedInternal(final View child) {
        runBeforeDrawChild(new Runnable() {
            @Override
            public void run() {
                ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
                PathInfo info = mPathInfoMap.get(key);
                if (info != null) {
                    if (info.getView() != null) {
                        updatePath(info);
                        //info.getView().invalidate() the method can not refresh view info of path ,
                        //mParent.invalidate() instead
                        mParent.invalidate();
                    } else {
                        Log.e(TAG, "notifyPathChangedInternal: update path failed , the view is null");
                        mPathInfoMap.remove(key);
                    }
                } else {
                    Log.d(TAG, "notifyPathChangedInternal: notify path changed failed , the info is null");
                    mPathInfoMap.remove(key);
                }
                resetTempViewGetKey();
            }
        });
    }

    private float[] getTempPoint() {
        if (mTempPoint == null) {
            mTempPoint = new float[2];
        }
        return mTempPoint;
    }

    private Matrix getTempMatrix() {
        if (mTempMatrix == null) {
            mTempMatrix = new Matrix();
        }
        return mTempMatrix;
    }

    private ViewGetKey getTempViewGetKey(int hashCode, View view) {
        if (mTempViewGetKey == null) {
            mTempViewGetKey = new ViewGetKey();
        }
        mTempViewGetKey.set(hashCode, view);
        return mTempViewGetKey;
    }

    // avoid memory leak
    private void resetTempViewGetKey() {
        mTempViewGetKey.set(-1, null);
    }

    private void updatePath(PathInfo info) {
        View view = info.getView();
        if (view == null) {
            Log.e(TAG, "updatePath: view is null ,update failed");
            return;
        }
        if (view.getVisibility() != View.VISIBLE) {
            Log.v(TAG, "updatePath: view is invisible or gone");
            return;
        }
        int width = view.getWidth();
        int height = view.getHeight();
        if (width == 0 || height == 0) {
            Log.v(TAG, "updatePath: the width or height of view is zero");
            return;
        }
        info.setPath(info.getPathGenerator().generatePath(info.getPath(), view, width, height));
        if ((info.getApplyFlag() & PathInfo.APPLY_FLAG_TOUCH_ONLY) != 0) {
            info.setPathRegion(mPathRegionGenerator.generatorPathRegion(info.getPath(), info.getClipType(), width, height));
        }
    }

    private void removeDeletedViewPathInfo() {
        Iterator<Map.Entry<ViewKey, PathInfo>> iterator = mPathInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            PathInfo info = iterator.next().getValue();
            if (info.getView() == null) {
                iterator.remove();
            }
        }
    }

    private void runBeforeDrawChild(Runnable runnable) {
        if (mInBeforeDrawChild) {
            runnable.run();
        } else {
            mPendingTaskQueue.add(runnable);
            mParent.removeCallbacks(mBeforeDrawTask);
            Utils.runOnUiThreadAfterViewCanUse(mParent, mBeforeDrawTask);
        }
    }

    private void executePendingTask() {
        while (mPendingTaskQueue.size() > 0) {
            mPendingTaskQueue.poll().run();
        }
    }


    private static class ViewKey {

        private final int mHashCode;
        private final WeakReference<View> mViewWeakReference;

        public ViewKey(int hashCode, View view) {
            mHashCode = hashCode;
            mViewWeakReference = new WeakReference<View>(view);
        }

        @Override
        public int hashCode() {
            return mHashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ViewKey) {
                ViewKey key = (ViewKey) obj;
                return mViewWeakReference.get() == key.mViewWeakReference.get();
            } else if (obj instanceof ViewGetKey) {
                ViewGetKey key = (ViewGetKey) obj;
                return mViewWeakReference.get() == key.mView;
            }
            return false;
        }
    }

    private static class ViewGetKey {

        private int mHashCode = -1;
        private View mView;

        public ViewGetKey() {

        }

        public void set(int hashCode, View view) {
            mHashCode = hashCode;
            mView = view;
        }

        @Override
        public int hashCode() {
            return mHashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ViewKey) {
                ViewKey key = (ViewKey) obj;
                if (key.mViewWeakReference.get() == mView) {
                    return true;
                }
            }
            return false;
        }
    }
}
