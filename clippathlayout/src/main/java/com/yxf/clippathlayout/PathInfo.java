package com.yxf.clippathlayout;

import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import com.yxf.clippathlayout.pathgenerator.PathGenerator;

import java.lang.ref.WeakReference;

public class PathInfo {

    private static final String TAG = Utils.getTAG(PathInfo.class);

    public static final int APPLY_FLAG_DRAW_ONLY = 1;
    public static final int APPLY_FLAG_TOUCH_ONLY = 1 << 1;
    public static final int APPLY_FLAG_DRAW_AND_TOUCH = APPLY_FLAG_DRAW_ONLY | APPLY_FLAG_TOUCH_ONLY;

    public static final int CLIP_TYPE_IN = 0;
    public static final int CLIP_TYPE_OUT = 1;

    private PathGenerator mPathGenerator;
    private WeakReference<View> mViewReference;

    private final int mSavedHashCode;

    private int mApplyFlag;

    private int mClipType;

    private Path mPath;
    private PathRegion mPathRegion;

    private boolean mAntiAlias;

    private PathInfo(PathGenerator generator, View view) {
        mPathGenerator = generator;
        mViewReference = new WeakReference<View>(view);
        mSavedHashCode = view.hashCode();
        mPath = new Path();
    }

    public PathInfo apply(ClipPathLayout layout) {
        layout.applyPathInfo(this);
        return this;
    }

    public PathInfo apply() {
        View view = mViewReference.get();
        if (view == null) {
            throw new NullPointerException("view is null");
        }
        if (view.getParent() instanceof ClipPathLayout) {
            apply((ClipPathLayout) view.getParent());
        } else if (view.getParent() != null) {
            throw new UnsupportedOperationException(
                    String.format("the parent(%s) of view(%s) does not implement ClipPathLayout",
                            view.getParent().getClass().getCanonicalName(), view.getClass().getCanonicalName()));
        } else {
            throw new UnsupportedOperationException(
                    String.format("the parent of view(%s) is null", view.getClass().getCanonicalName()));
        }
        return this;
    }

    public PathInfo cancel() {
        View view = mViewReference.get();
        if (view == null) {
            Log.d(TAG, "cancel: view is null");
            return this;
        }
        ViewParent parent = view.getParent();
        if (parent == null) {
            Log.d(TAG, "cancel: the parent of view is null");
            return this;
        }
        if (parent instanceof ClipPathLayout) {
            ((ClipPathLayout) parent).cancelPathInfo(view);
        } else {
            throw new UnsupportedOperationException(String.format("the parent(%s) of view(%s) does not implement ClipPathLayout",
                    view.getParent().getClass().getCanonicalName(), view.getClass().getCanonicalName()));
        }
        return this;
    }

    @Override
    public int hashCode() {
        return mSavedHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathInfo) {
            PathInfo info = (PathInfo) obj;
            if (info.mViewReference.get() == mViewReference.get()) {
                return true;
            }
        }
        return false;
    }

    PathGenerator getPathGenerator() {
        return mPathGenerator;
    }

    public int getApplyFlag() {
        return mApplyFlag;
    }

    public int getClipType() {
        return mClipType;
    }

    View getView() {
        return mViewReference.get();
    }

    Path getPath() {
        return mPath;
    }

    void setPath(Path path) {
        mPath = path;
    }

    PathRegion getPathRegion() {
        return mPathRegion;
    }

    void setPathRegion(PathRegion pathRegion) {
        mPathRegion = pathRegion;
    }

    public boolean isAntiAlias() {
        return mAntiAlias;
    }

    public static class Builder {
        private PathGenerator mPathGenerator;
        private View mView;
        private int mApplyFlag = APPLY_FLAG_DRAW_AND_TOUCH;
        private int mClipType = CLIP_TYPE_IN;
        private boolean mAntiAlias = false;

        /**
         * @param generator Path生成器
         * @param view      实现了ClipPathLayout接口的ViewGroup的子View
         */
        public Builder(PathGenerator generator, View view) {
            if (generator == null) {
                throw new NullPointerException("PathGenerator is null");
            }
            if (view == null) {
                throw new NullPointerException("view is null");
            }
            this.mPathGenerator = generator;
            this.mView = view;
        }

        public Builder setApplyFlag(int flag) {
            mApplyFlag = flag;
            return this;
        }

        public Builder setClipType(int type) {
            mClipType = type;
            return this;
        }

        public Builder setAntiAlias(boolean antiAlias) {
            mAntiAlias = antiAlias;
            return this;
        }

        public PathInfo create() {
            PathInfo info = new PathInfo(mPathGenerator, mView);
            info.mApplyFlag = mApplyFlag;
            info.mClipType = mClipType;
            info.mAntiAlias = mAntiAlias;
            return info;
        }
    }


}
