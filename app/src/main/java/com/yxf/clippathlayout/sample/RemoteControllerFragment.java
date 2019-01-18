package com.yxf.clippathlayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.impl.ClipPathFrameLayout;
import com.yxf.clippathlayout.pathgenerator.CirclePathGenerator;
import com.yxf.clippathlayout.pathgenerator.OvalPathGenerator;
import com.yxf.clippathlayout.pathgenerator.OvalRingPathGenerator;

public class RemoteControllerFragment extends Fragment {

    ClipPathFrameLayout mLayout;
    View mCenterView, mLeftView, mRightView, mTopView, mBottomView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (ClipPathFrameLayout) inflater.inflate(R.layout.fragment_remote_controller, null);
        mCenterView = mLayout.findViewById(R.id.center_view);
        mLeftView = mLayout.findViewById(R.id.left_view);
        mRightView = mLayout.findViewById(R.id.right_view);
        mTopView = mLayout.findViewById(R.id.top_view);
        mBottomView = mLayout.findViewById(R.id.bottom_view);

        new PathInfo.Builder(new OvalPathGenerator(), mCenterView)
                .create()
                .apply();

        new PathInfo.Builder(new OvalRingPathGenerator(0.45f, 44, 88), mBottomView)
                .create()
                .apply();
        new PathInfo.Builder(new OvalRingPathGenerator(0.45f, 134, 88), mLeftView)
                .create()
                .apply();
        new PathInfo.Builder(new OvalRingPathGenerator(0.45f, 224, 88), mTopView)
                .create()
                .apply();
        new PathInfo.Builder(new OvalRingPathGenerator(0.45f, 314, 88), mRightView)
                .create()
                .apply();

        return mLayout;
    }
}
