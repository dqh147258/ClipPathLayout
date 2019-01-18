package com.yxf.clippathlayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.impl.ClipPathRelativeLayout;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;
import com.yxf.clippathlayout.pathgenerator.RhombusPathGenerator;

public class ControlButtonFragment extends Fragment {

    private ClipPathRelativeLayout mLayout;

    private View mLeftView, mTopView, mRightView, mBottomView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (ClipPathRelativeLayout) inflater.inflate(R.layout.fragment_control_button, null);
        mLeftView = mLayout.findViewById(R.id.left_view);
        mTopView = mLayout.findViewById(R.id.top_view);
        mRightView = mLayout.findViewById(R.id.right_view);
        mBottomView = mLayout.findViewById(R.id.bottom_view);

        PathGenerator generator = new RhombusPathGenerator();

        new PathInfo.Builder(generator, mLeftView)
                .create()
                .apply();
        new PathInfo.Builder(generator, mTopView)
                .create()
                .apply();
        new PathInfo.Builder(generator, mRightView)
                .create()
                .apply();
        new PathInfo.Builder(generator, mBottomView)
                .create()
                .apply();

        return mLayout;
    }
}
