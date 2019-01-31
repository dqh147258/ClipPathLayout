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
import com.yxf.clippathlayout.pathgenerator.CirclePathGenerator;

import java.lang.ref.WeakReference;

public class TransitionFrameLayout extends ClipPathFrameLayout implements TransitionLayout {

    private static final String TAG = Utils.getTAG(TransitionFrameLayout.class);

    private WeakReference<View> mPreviousViewReference, mCurrentViewReference;
    private PathInfo mPreviousInfo, mCurrentInfo;

    private TransitionAdapter mTransitionAdapter;

    private int mApplyFlag = PathInfo.APPLY_FLAG_DRAW_ONLY;

    public TransitionFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public TransitionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAdapterInternal(new TransitionAdapter(new CirclePathGenerator()));
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
        setAdapterInternal(adapter);
    }

    private void setAdapterInternal(TransitionAdapter adapter) {
        if (adapter == null) {
            Log.e(TAG, "setAdapter: adapter is null");
            return;
        }
        mTransitionAdapter = adapter;
        mTransitionAdapter.setTransitionLayout(this);
    }

    @Override
    public TransitionAdapter switchView(View view) {
        return switchView(view, false);
    }

    /**
     * if you want add a view , just invoke switchView directly ,
     * do not invoke addView , it may cause some problem .
     *
     * @param view
     * @return
     */
    @Override
    public TransitionAdapter switchView(final View view, boolean reverse) {
        if (view == null) {
            throw new NullPointerException("view is null");
        }
        View previous = findPreviousView(view);
        if (previous == view) {
            Log.w(TAG, "switchView: the top visible view is the same as the view switched");
            new Throwable().printStackTrace();
            return mTransitionAdapter;
        }

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
        updateViewReference(previous, view);
        applySwitch(reverse);
        return mTransitionAdapter;
    }

    @Override
    public void update(boolean finished) {
        if (finished) {
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
            final View current;
            if (mCurrentViewReference != null && (current = mCurrentViewReference.get()) != null) {
                notifyPathChanged(current);
            }
        }
    }

    public void setApplyFlag(int applyFlag) {
        mApplyFlag = applyFlag;
    }

    private void cancelPreviousTransition() {
        mTransitionAdapter.cancelAnimator();
        if (mPreviousInfo != null) {
            mPreviousInfo.cancel();
        }
        if (mCurrentInfo != null) {
            mCurrentInfo.cancel();
        }
        mTransitionAdapter.reset();
    }

    private void applySwitch(boolean reverse) {
        mTransitionAdapter.setReverse(reverse);
        mTransitionAdapter.updateAnimator();
        if (mPreviousViewReference != null && mPreviousViewReference.get() != null) {
            mPreviousInfo = new PathInfo.Builder(mTransitionAdapter, mPreviousViewReference.get())
                    .setClipType(reverse ? PathInfo.CLIP_TYPE_IN : PathInfo.CLIP_TYPE_OUT)
                    .setApplyFlag(mApplyFlag)
                    .create()
                    .apply();
        }
        mCurrentInfo = new PathInfo.Builder(mTransitionAdapter, mCurrentViewReference.get())
                .setClipType(reverse ? PathInfo.CLIP_TYPE_OUT : PathInfo.CLIP_TYPE_IN)
                .setApplyFlag(mApplyFlag)
                .create()
                .apply();
    }

    protected View findPreviousView(View current) {
        int frontIndex = getChildCount() - 1;
        if (frontIndex < 0) {
            return null;
        }
        for (int i = frontIndex; i >= 0; i--) {
            View view = getChildAt(i);
            if (view.getVisibility() == VISIBLE) {
                return view;
            }
        }
        return null;
    }

    private void updateViewReference(View previous, View current) {
        if (previous != null) {
            mPreviousViewReference = new WeakReference<View>(previous);
        } else {
            mPreviousViewReference = null;
        }
        mCurrentViewReference = new WeakReference<View>(current);
    }

    protected View getPreviousView() {
        if (mPreviousViewReference != null) {
            return mPreviousViewReference.get();
        }
        return null;
    }

    protected View getCurrentView() {
        if (mCurrentViewReference != null) {
            return mCurrentViewReference.get();
        }
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelPreviousTransition();
    }
}
