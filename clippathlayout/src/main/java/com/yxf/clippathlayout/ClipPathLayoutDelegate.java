package com.yxf.clippathlayout;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClipPathLayoutDelegate implements ClipPathLayout {

    private static final String TAG = Utils.getTAG(ClipPathLayoutDelegate.class);

    ViewGroup mParent;

    private HashMap<ViewKey, PathInfo> mPathInfoMap = new HashMap<ViewKey, PathInfo>();

    private PathRegionGenerator mPathRegionGenerator = PathRegionGenerators.createBitmapPathRegionGenerator();

    // Lazily-created holder for point computations.
    private float[] mTempPoint;

    private Matrix mTempMatrix;

    private ViewGetKey mTempViewGetKey;

    private boolean hasLayoutRequest = false;

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
        Utils.runOnUiThreadAfterViewCanUse(mParent, new Runnable() {
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
        Utils.runOnUiThreadAfterViewCanUse(mParent, new Runnable() {
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
        canvas.save();
        canvas.translate(child.getLeft(), child.getTop());
        if (hasLayoutRequest) {
            hasLayoutRequest = false;
            notifyAllPathChangedInternal(false);
        }
        ViewGetKey key = getTempViewGetKey(child.hashCode(), child);
        PathInfo info = mPathInfoMap.get(key);
        if (info != null) {
            if ((info.getApplyFlag() & PathInfo.APPLY_FLAG_DRAW_ONLY) != 0) {
                Path path = info.getPath();
                if (path != null) {
                    Utils.clipPath(canvas, path, info.getClipType());
                }
            }
        }
        resetTempViewGetKey();
        canvas.translate(-child.getLeft(), -child.getTop());
    }

    @Override
    public void afterDrawChild(Canvas canvas, View child, long drawingTime) {
        canvas.restore();
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
        Utils.runOnUiThreadAfterViewCanUse(mParent, new Runnable() {
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
        hasLayoutRequest = true;
    }

    private void notifyPathChangedInternal(final View child) {
        Utils.runOnUiThreadAfterViewCanUse(mParent, new Runnable() {
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
                    Log.e(TAG, "notifyPathChangedInternal: notify path changed failed , the info is null");
                    new Throwable().printStackTrace();
                    Log.d(TAG, "run: view : " + child.getClass().getCanonicalName());
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
            Log.w(TAG, "updatePath: view is invisible or gone");
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
