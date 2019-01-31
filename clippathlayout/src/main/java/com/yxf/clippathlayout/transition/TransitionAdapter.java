package com.yxf.clippathlayout.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.yxf.clippathlayout.Utils;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;
import com.yxf.clippathlayout.transition.generator.TransitionPathGenerator;

public class TransitionAdapter implements PathGenerator, ProgressController {

    private static final String TAG = Utils.getTAG(TransitionFrameLayout.class);

    public static final int PATH_CENTER_VIEW_CENTER = -1;

    private static final float INVALID_SCALE = -1f;

    public static final int DEFAULT_ANIMATOR_DURATION = 600;

    private int mDefaultDuration = DEFAULT_ANIMATOR_DURATION;

    private Interpolator mDefaultInterpolator = new AccelerateInterpolator();

    private PathGenerator mPathGenerator;
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

    private Rect mRect = new Rect();

    private ValueAnimator mValueAnimator;

    private boolean mReverse = false;

    private boolean immediately = false;

    public TransitionAdapter(PathGenerator generator) {
        if (generator == null) {
            throw new NullPointerException("TransitionPathGenerator is null");
        }
        mPathGenerator = generator;
    }

    void setTransitionLayout(TransitionLayout transitionLayout) {
        if (transitionLayout == null) {
            throw new NullPointerException("TransitionLayout is null");
        }
        mTransitionLayout = transitionLayout;
    }

    void setReverse(boolean reverse) {
        if (mReverse == reverse) {
            return;
        }
        mReverse = reverse;
    }

    void reset() {
        mPercent = 0f;
        mScale = INVALID_SCALE;
        mInvalidateOriginPath = true;
        mReverse = false;
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

    public void setDefaultDuration(int defaultDuration) {
        mDefaultDuration = defaultDuration;
    }

    public void setDefaultInterpolator(Interpolator defaultInterpolator) {
        if (defaultInterpolator == null) {
            throw new NullPointerException("Interpolator is null");
        }
        mDefaultInterpolator = defaultInterpolator;
    }

    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        if (mPathCenterX == PATH_CENTER_VIEW_CENTER) {
            mPathCenterX = width / 2;
        }
        if (mPathCenterY == PATH_CENTER_VIEW_CENTER) {
            mPathCenterY = height / 2;
        }
        if (mInvalidateOriginPath) {
            mOriginPath = mPathGenerator.generatePath(mOriginPath, view, width, height);
            calculateScale(width, height);
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
        int radiusX, radiusY;
        radiusX = Math.max(mPathCenterX, width - mPathCenterX);
        radiusY = Math.max(mPathCenterY, height - mPathCenterY);
        int centerX = width / 2;
        int centerY = height / 2;

        int x, y;
        if (width / (height * 1f) > radiusX / (radiusY * 1f)) {
            x = radiusY * width / height;
            y = height / 2;
        } else {
            x = width / 2;
            y = radiusX * height / width;
        }
        mRect.set(centerX - x, centerY - y, centerX + x, centerY + y);
        Rect rect;
        if (mPathGenerator instanceof TransitionPathGenerator) {
            rect = ((TransitionPathGenerator) mPathGenerator).maxContainSimilarRange(mRect, width, height);
        } else {
            rect = Utils.maxContainSimilarRange(mOriginPath, mRect, width, height);
        }
        if (rect.width() <= 0 || rect.height() <= 0) {
            throw new RuntimeException("calculateScale: the width or height of the rect get from maxContainSimilarRange is illegal , rect : " + rect);
        }
        mScale = Math.max(radiusX * 2 / (rect.width() * 1f), radiusY * 2 / (rect.height() * 1f));
    }


    @Override
    public void setProgress(float percent) {
        if (percent < 0f) {
            percent = 0f;
        }
        if (Float.compare(mPercent, percent) == 0) {
            //percent not change ,return
            return;
        }
        mPercent = percent;
        mTransitionLayout.update(false);
    }

    public void finish() {
        finishInternal();
    }

    private void finishInternal() {
        mTransitionLayout.update(true);
    }

    @Override
    public float getProgress() {
        return mPercent;
    }

    public ProgressController getController() {
        return this;
    }

    public boolean isImmediately() {
        return immediately;
    }

    public void setImmediately(boolean immediately) {
        this.immediately = immediately;
        if (mValueAnimator != null && !mValueAnimator.isRunning()) {
            mValueAnimator.setDuration(0);
        }
    }

    public void animate() {
        ValueAnimator animator = getAnimatorInternal();
        animator.setInterpolator(mDefaultInterpolator);
        animator.start();
    }

    public ValueAnimator getAnimator() {
        return getAnimatorInternal();
    }

    void cancelAnimator() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }

    void updateAnimator() {
        float start = mReverse ? 1f : 0f;
        float end = mReverse ? 0f : 1f;
        mValueAnimator = ValueAnimator.ofFloat(start, end);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((Float) animation.getAnimatedValue());
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                finishInternal();
            }
        });
        if (immediately) {
            mValueAnimator.setDuration(0);
        } else {
            mValueAnimator.setDuration(mDefaultDuration);
        }
    }

    private ValueAnimator getAnimatorInternal() {
        if (mValueAnimator == null) {
            updateAnimator();
        }
        return mValueAnimator;
    }
}
