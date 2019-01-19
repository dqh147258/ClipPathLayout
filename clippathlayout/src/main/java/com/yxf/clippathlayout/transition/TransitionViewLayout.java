package com.yxf.clippathlayout.transition;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.Utils;
import com.yxf.clippathlayout.impl.ClipPathFrameLayout;
import com.yxf.clippathlayout.transition.generator.OvalTransitionPathGenerator;

import java.lang.ref.WeakReference;

public class TransitionViewLayout extends ClipPathFrameLayout implements TransitionLayout {

    private static final String TAG = Utils.getTAG(TransitionViewLayout.class);

    private WeakReference<View> mPreviousViewReference, mCurrentViewReference;
    private PathInfo mPreviousInfo, mCurrentInfo;

    private TransitionAdapter mTransitionAdapter =
            new TransitionAdapter(new OvalTransitionPathGenerator(), this);

    private int mApplyFlag = PathInfo.APPLY_FLAG_DRAW_ONLY;

    private float mPercent = 0f;

    public TransitionViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public TransitionViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 1) {
            for (int i = count - 2; i >= 0; i--) {
                getChildAt(i).setVisibility(GONE);
            }
        }
    }

    @Override
    public void setAdapter(TransitionAdapter adapter) {
        if (adapter == null) {
            Log.e(TAG, "setAdapter: adapter is null");
            return;
        }
        mTransitionAdapter = adapter;
    }

    @Override
    public TransitionAdapter switchView(View view) {
        if (view == null) {
            throw new NullPointerException("view is null");
        }
        updatePreviousView(view);
        cancelPreviousTransition();
        if (view.getParent() == this) {
            view.setVisibility(VISIBLE);
            view.bringToFront();
        } else if (view.getParent() != null) {
            throw new IllegalArgumentException(String.format("the view(%s) switched has another parent(%s)",
                    view.getClass().getCanonicalName(), view.getParent().getClass().getCanonicalName()));
        } else {
            super.addView(view);
        }
        mCurrentViewReference = new WeakReference<View>(view);
        applySwitch();
        return mTransitionAdapter;
    }

    public void setApplyFlag(int applyFlag) {
        mApplyFlag = applyFlag;
    }

    private void cancelPreviousTransition() {
        if (mPreviousInfo != null) {
            mPreviousInfo.cancel();
        }
        if (mCurrentInfo != null) {
            mCurrentInfo.cancel();
        }
    }

    private void applySwitch() {
        mTransitionAdapter.reset();
        if (mPreviousViewReference != null && mPreviousViewReference.get() != null) {
            mPreviousInfo = new PathInfo.Builder(mTransitionAdapter, mPreviousViewReference.get())
                    .setClipType(PathInfo.CLIP_TYPE_OUT)
                    .setApplyFlag(mApplyFlag)
                    .create()
                    .apply();
        }
        mCurrentInfo = new PathInfo.Builder(mTransitionAdapter, mCurrentViewReference.get())
                .setClipType(PathInfo.CLIP_TYPE_IN)
                .setApplyFlag(mApplyFlag)
                .create()
                .apply();
    }

    private void updatePreviousView(View current) {
        int frontIndex = getChildCount() - 1;
        if (frontIndex < 0) {
            mPreviousViewReference = null;
            return;
        }
        for (int i = frontIndex; i >= 0; i--) {
            View view = getChildAt(i);
            if (view.getVisibility() == VISIBLE) {
                if (current == view) {
                    mPreviousViewReference = null;
                    return;
                }
                mPreviousViewReference = new WeakReference<View>(view);
                return;
            }
        }
    }

    @Override
    public void setProgress(float percent) {
        if (Float.compare(percent, 0f) == 0) {

        } else if (Float.compare(percent, 1f) == 0) {
            if (mPreviousInfo != null) {
                mPreviousInfo.cancel();
            }
            if (mCurrentInfo != null) {
                mCurrentInfo.cancel();
            }
            if (mPreviousViewReference != null) {
                final View previous = mPreviousViewReference.get();
                if (previous != null) {
                    previous.setVisibility(GONE);
                }
            }

        } else {
            if (mPreviousViewReference != null) {
                final View previous = mPreviousViewReference.get();
                if (previous != null) {
                    notifyPathChanged(previous);
                }
            }
            final View current = mCurrentViewReference.get();
            if (current != null) {
                notifyPathChanged(current);
            }
        }
    }

    @Override
    public float getProgress() {
        return mPercent;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTransitionAdapter.getAnimator().cancel();
        cancelPreviousTransition();
    }
}
