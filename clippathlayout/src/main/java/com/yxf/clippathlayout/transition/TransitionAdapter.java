package com.yxf.clippathlayout.transition;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.yxf.clippathlayout.Utils;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;
import com.yxf.clippathlayout.transition.generator.TransitionPathGenerator;

public class TransitionAdapter implements PathGenerator, ProgressController {

    private static final String TAG = Utils.getTAG(TransitionViewLayout.class);

    public static final int PATH_CENTER_VIEW_CENTER = -1;

    private static final float INVALID_SCALE = -1f;

    private static final int DEFAULT_ANIMATOR_DURATION = 600;

    private TransitionPathGenerator mTransitionPathGenerator;
    private TransitionLayout mTransitionLayout;

    private int mPathCenterX = PATH_CENTER_VIEW_CENTER, mPathCenterY = PATH_CENTER_VIEW_CENTER;

    public static final float FAILED_PERCENT = 0f;
    public static final float SUCCESSFAULLY_PERCENT = 1f;

    private float mPercent = 0f;

    private float mScale = INVALID_SCALE;

    private boolean mInvalidateOriginPath = true;

    private Path mOriginPath = new Path();
    private Path mPath = new Path();

    private Matrix mMatrix = new Matrix();

    private ValueAnimator mValueAnimator;

    public TransitionAdapter(TransitionPathGenerator generator, TransitionLayout layout) {
        if (generator == null) {
            throw new NullPointerException("TransitionPathGenerator is null");
        }
        if (layout == null) {
            throw new NullPointerException("TransitionLayout is null");
        }
        mTransitionPathGenerator = generator;
        mTransitionLayout = layout;
    }

    public void reset() {
        mPercent = 0f;
        mScale = INVALID_SCALE;
        mInvalidateOriginPath = true;
        if (mOriginPath != null) {
            mOriginPath.reset();
        }
    }

    public void setPathCenter(int x, int y) {
        mPathCenterX = x;
        mPathCenterY = y;
        mInvalidateOriginPath = true;
    }

    public void update() {
        mInvalidateOriginPath = true;
    }


    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (mPathCenterX == PATH_CENTER_VIEW_CENTER) {
            mPathCenterX = width / 2;
        }
        if (mPathCenterY == PATH_CENTER_VIEW_CENTER) {
            mPathCenterY = height / 2;
        }
        if (Float.compare(mScale, INVALID_SCALE) == 0 || mInvalidateOriginPath) {
            calculateScale(width, height);
        }
        if (mInvalidateOriginPath) {
            mOriginPath = mTransitionPathGenerator.generatePath(mOriginPath, view, width, height);
        }
        transformPath(width, height);
        mInvalidateOriginPath = false;
        return mPath;
    }

    private void transformPath(int width, int height) {
        mMatrix.reset();
        mMatrix.postTranslate(-width / 2, -height / 2);
        float scale = mScale * mPercent;
        mMatrix.postScale(scale, scale);
        mMatrix.postTranslate(mPathCenterX, mPathCenterY);
        mOriginPath.transform(mMatrix, mPath);
    }

    private void calculateScale(int width, int height) {
        Rect rect = mTransitionPathGenerator.maxContainSimilarRange(new Rect(0, 0, width, height));
        float left, top, right, bottom;
        if (rect.width() <= 0 || rect.height() <= 0) {
            throw new RuntimeException("calculateScale: the width or height of the rect get from maxContainSimilarRange is illegal , rect : " + rect);
        }
        left = mPathCenterX * 2 / (rect.width() * 1f);
        top = mPathCenterY * 2 / (rect.height() * 1f);
        right = (width - mPathCenterX) * 2 / (rect.width() * 1f);
        bottom = (height - mPathCenterY) * 2 / (rect.height() * 1f);
        mScale = Math.max(Math.max(left, right), Math.max(top, bottom));
    }

    @Override
    public void setProgress(float percent) {
        if (percent < 0f) {
            percent = 0f;
        } else if (percent > 1f) {
            percent = 1f;
        }
        if ((Float.compare(mPercent, percent) == 0)) {
            //percent not change ,return
            return;
        }
        mPercent = percent;
        mTransitionLayout.setProgress(percent);
    }

    @Override
    public float getProgress() {
        return mPercent;
    }

    public ProgressController getController() {
        return this;
    }

    public void animate() {
        ValueAnimator animator = getAnimatorInternal();
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    public ValueAnimator getAnimator() {
        return getAnimatorInternal();
    }

    private ValueAnimator getAnimatorInternal() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setProgress((Float) animation.getAnimatedValue());
                }
            });
            mValueAnimator.setDuration(DEFAULT_ANIMATOR_DURATION);
        }
        return mValueAnimator;
    }
}
