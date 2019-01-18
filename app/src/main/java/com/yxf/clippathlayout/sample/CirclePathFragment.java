package com.yxf.clippathlayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.impl.ClipPathFrameLayout;
import com.yxf.clippathlayout.pathgenerator.CirclePathGenerator;
import com.yxf.clippathlayout.pathgenerator.OvalPathGenerator;

public class CirclePathFragment extends Fragment {

    private ImageView mImageView;
    private ClipPathFrameLayout mLayout;

    private TextView mPathFunctionSwitchView;
    private TextView mClipTypeSwitchView;

    private int mApplyFlag = PathInfo.APPLY_FLAG_DRAW_AND_TOUCH;

    private int mClipType = PathInfo.CLIP_TYPE_IN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (ClipPathFrameLayout) inflater.inflate(R.layout.fragment_circle_path, null);

        mImageView = mLayout.findViewById(R.id.image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.displayToast("View内");
            }
        });
        updatePathInfo();
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.displayToast("View外");
            }
        });

        mPathFunctionSwitchView = mLayout.findViewById(R.id.switch_path_function);
        mPathFunctionSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mApplyFlag) {
                    case PathInfo.APPLY_FLAG_DRAW_ONLY:
                        mApplyFlag = PathInfo.APPLY_FLAG_TOUCH_ONLY;
                        break;
                    case PathInfo.APPLY_FLAG_TOUCH_ONLY:
                        mApplyFlag = PathInfo.APPLY_FLAG_DRAW_AND_TOUCH;
                        break;
                    case PathInfo.APPLY_FLAG_DRAW_AND_TOUCH:
                        mApplyFlag = PathInfo.APPLY_FLAG_DRAW_ONLY;
                        break;
                }
                updatePathInfo();
            }
        });
        mClipTypeSwitchView = mLayout.findViewById(R.id.switch_clip_type);
        mClipTypeSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mClipType) {
                    case PathInfo.CLIP_TYPE_IN:
                        mClipType = PathInfo.CLIP_TYPE_OUT;
                        break;
                    case PathInfo.CLIP_TYPE_OUT:
                        mClipType = PathInfo.CLIP_TYPE_IN;
                        break;
                }
                updatePathInfo();
            }
        });

        new PathInfo.Builder(new OvalPathGenerator(), mClipTypeSwitchView)
                .setApplyFlag(PathInfo.APPLY_FLAG_DRAW_AND_TOUCH)
                .setClipType(PathInfo.CLIP_TYPE_IN)
                .create()
                .apply(mLayout);

        return mLayout;
    }

    private void updatePathInfo() {
        new PathInfo.Builder(new CirclePathGenerator(), mImageView)
                .setApplyFlag(mApplyFlag)
                .setClipType(mClipType)
                .create()
                .apply();
    }
}
