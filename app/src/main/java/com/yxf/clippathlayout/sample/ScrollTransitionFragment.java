package com.yxf.clippathlayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yxf.clippathlayout.Utils;
import com.yxf.clippathlayout.transition.ProgressController;
import com.yxf.clippathlayout.transition.TransitionAdapter;
import com.yxf.clippathlayout.transition.TransitionFrameLayout;

public class ScrollTransitionFragment extends Fragment implements NestedScrollView.OnScrollChangeListener {

    private static final String TAG = Utils.getTAG(ScrollTransitionFragment.class);

    private NestedScrollView mLayout;

    private TransitionFrameLayout mImageContainer;

    private ImageView mBelowView, mAboveView;

    private ProgressController mController;
    private TransitionAdapter mTransitionAdapter;

    private boolean mAbove = true;

    private boolean mOver = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (NestedScrollView) inflater.inflate(R.layout.fragment_scroll_transition, null);
        mImageContainer = mLayout.findViewById(R.id.image_container);
        mBelowView = mLayout.findViewById(R.id.below_image);
        mAboveView = mLayout.findViewById(R.id.above_image);
        mLayout.setOnScrollChangeListener(this);
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mController == null) {
                    initController();
                }
            }
        });
        return mLayout;
    }

    @Override
    public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
        int top = mImageContainer.getTop();
        int height = mImageContainer.getHeight();
        int scrollCenter = i1 + nestedScrollView.getHeight() / 2;
        if (scrollCenter < top && mAbove) {
            mOver = true;
        } else if (scrollCenter > top + height && !mAbove) {
            mOver = true;
        } else {
            mOver = false;
        }
        controlProgress(scrollCenter - top, height);
    }

    private void controlProgress(int current, int total) {
        if (mOver) {
            if (mController != null) {
                mTransitionAdapter.finish();
                mController = null;
            }
        } else {
            if (mController == null) {
                initController();
            }
            if (mAbove) {
                current = total - current;
            }
            mController.setProgress(current / (total * 1f));
        }
    }

    private void initController() {
        if (mAbove) {
            int x = mImageContainer.getWidth() * 3 / 4;
            int y = mImageContainer.getHeight() * 3 / 4;
            mTransitionAdapter = mImageContainer.switchView(mBelowView);
            mTransitionAdapter.setPathCenter(x, y);
            mController = mTransitionAdapter.getController();
            mAbove = false;
        } else {
            int x = mImageContainer.getWidth() / 4;
            int y = mImageContainer.getHeight() / 4;
            mTransitionAdapter = mImageContainer.switchView(mAboveView);
            mTransitionAdapter.setPathCenter(x, y);
            mController = mTransitionAdapter.getController();
            mAbove = true;
        }
    }
}
