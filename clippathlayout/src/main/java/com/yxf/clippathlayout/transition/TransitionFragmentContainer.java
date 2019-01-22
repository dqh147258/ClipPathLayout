package com.yxf.clippathlayout.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;

public class TransitionFragmentContainer extends TransitionFrameLayout implements Handler.Callback {

    private static final int MESSAGE_REMOVE_VIEW = 1;

    private static final int MESSAGE_START_ANIMATOR = 2;

    private MyHandler mHandler = new MyHandler(this);

    private Runnable mRemoveViewTask = new Runnable() {
        @Override
        public void run() {
            View view = mRemoveViewReference.get();
            if (view != null) {
                TransitionFragmentContainer.super.removeView(view);
            }
            mRemoveViewReference = null;
        }
    };

    private WeakReference<View> mRemoveViewReference;

    private ValueAnimator mValueAnimator;


    public TransitionFragmentContainer(@NonNull Context context) {
        this(context, null);
    }

    public TransitionFragmentContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionFragmentContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View findPreviousView(View current) {
        return findNextTopView(current);
    }

    @Override
    public void addView(View child) {
        TransitionAdapter adapter = switchView(child);
        if (mHandler.hasMessages(MESSAGE_REMOVE_VIEW)) {
            mHandler.removeMessages(MESSAGE_REMOVE_VIEW);
        }
        mValueAnimator = adapter.getAnimator();
        mValueAnimator.setInterpolator(new AccelerateInterpolator());
        mValueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                sendRemoveViewMessage();
            }
        });
        mValueAnimator.setInterpolator(new AccelerateInterpolator());
        startAnimator();
    }

    private void startAnimator() {
        View previous = getPreviousView();
        if (previous != null) {
            previous.setVisibility(VISIBLE);
        }
        View current = getCurrentView();
        if (current != null) {
            current.setVisibility(VISIBLE);
        }
        mValueAnimator.start();
    }

    @Override
    public void removeView(View view) {
        if (indexOfChild(view) == getChildCount() - 1) {
            view.setVisibility(VISIBLE);
        }
        removeViewInternal(view);
    }

    @Override
    public void removeViewAt(int index) {
        removeViewInternal(getChildAt(index));
    }

    private void executeRemoveViewTask() {
        if (mRemoveViewTask != null && mRemoveViewReference != null) {
            mRemoveViewTask.run();
        }
    }

    private void removeViewInternal(final View child) {
        if (child.getVisibility() != VISIBLE) {
            super.removeView(child);
            return;
        }
        executeRemoveViewTask();
        View current = findNextTopView(child);
        if (current == null) {
            mRemoveViewReference = new WeakReference<View>(child);
            sendRemoveViewMessage();
            return;
        }
        final TransitionAdapter adapter = switchView(current, true);
        if (mHandler.hasMessages(MESSAGE_REMOVE_VIEW)) {
            mHandler.removeMessages(MESSAGE_REMOVE_VIEW);
        }
        mRemoveViewReference = new WeakReference<View>(child);
        mValueAnimator = adapter.getAnimator();
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mValueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                sendRemoveViewMessage();
            }
        });
        startAnimator();
        return;
    }

    private void sendRemoveViewMessage() {
        mHandler.sendEmptyMessage(MESSAGE_REMOVE_VIEW);
    }

    private View findNextTopView(View child) {
        int index = getChildCount() - 1;
        if (index < 0) {
            return null;
        }
        for (int i = index; i >= 0; i--) {
            View view = getChildAt(i);
            if (child != view) {
                return view;
            }
        }
        return null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REMOVE_VIEW:
                executeRemoveViewTask();
                break;
            case MESSAGE_START_ANIMATOR:
                startAnimator();
                break;
        }
        return true;
    }

    private static class MyHandler extends Handler {

        private WeakReference<Callback> mCallbackWeakReference;


        public MyHandler(Callback callback) {
            super(Looper.getMainLooper());
            mCallbackWeakReference = new WeakReference<Callback>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Callback callback = mCallbackWeakReference.get();
            if (callback != null) {
                callback.handleMessage(msg);
            }
        }
    }
}
