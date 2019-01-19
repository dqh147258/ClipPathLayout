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
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.lang.ref.WeakReference;

public class TransitionFragmentContainer extends TransitionViewLayout implements Handler.Callback {

    private static final int MESSAGE_REMOVE_VIEW = 1;

    private MyHandler mHandler = new MyHandler(this);

    private Runnable mRemoveViewTask;


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
    public void addView(View child) {
        if (mHandler.hasMessages(MESSAGE_REMOVE_VIEW)) {
            mHandler.removeMessages(MESSAGE_REMOVE_VIEW);
        }
        TransitionAdapter adapter = switchView(child);
        ValueAnimator animator = adapter.getAnimator();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                executeRemoveViewTask();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                executeRemoveViewTask();
            }
        });
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
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
        if (mRemoveViewTask != null) {
            mRemoveViewTask.run();
            mRemoveViewTask = null;
        }
    }

    private boolean removeViewInternal(final View child) {
        if (child.getVisibility() != VISIBLE) {
            super.removeView(child);
            return true;
        }
        if (mHandler.hasMessages(MESSAGE_REMOVE_VIEW)) {
            executeRemoveViewTask();
        }
        mRemoveViewTask = new Runnable() {
            @Override
            public void run() {
                TransitionFragmentContainer.super.removeView(child);
            }
        };
        mHandler.sendEmptyMessage(MESSAGE_REMOVE_VIEW);
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REMOVE_VIEW:
                executeRemoveViewTask();
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
