package com.yxf.clippathlayout.sample;

import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.impl.ClipPathFrameLayout;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;

public class YinYangFishFragment extends Fragment {

    private ClipPathFrameLayout mLayout;
    private View mYinFishView, mYangFishView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (ClipPathFrameLayout) inflater.inflate(R.layout.fragment_yin_yang_fish, null);
        mYinFishView = mLayout.findViewById(R.id.yin_fish_view);
        mYangFishView = mLayout.findViewById(R.id.yang_fish_view);
        mYinFishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.displayToast("阴");
            }
        });
        mYangFishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.displayToast("阳");
            }
        });

        new PathInfo.Builder(new YinYangFishPathGenerator(270), mYinFishView)
                .create()
                .apply();

        new PathInfo.Builder(new YinYangFishPathGenerator(90), mYangFishView)
                .create()
                .apply();
        return mLayout;
    }


    private static class YinYangFishPathGenerator implements PathGenerator {

        private int mDegree;

        private RectF mRectF = new RectF();

        private Path mPath = new Path();

        public YinYangFishPathGenerator(int degree) {
            mDegree = degree;
        }

        @Override
        public Path generatePath(Path old, View view, int width, int height) {
            double radians = Math.toRadians(mDegree);
            int radius = Math.min(width, height) / 2;
            int centerX = width / 2;
            int centerY = height / 2;

            if (old == null) {
                old = new Path();
            } else {
                old.reset();
            }

            mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            old.moveTo(centerX + (float) (radius * Math.cos(radians)),
                    centerY + (float) (radius * Math.sin(radians)));
            old.arcTo(mRectF, mDegree, 180);

            int degree = mDegree + 180;
            double cRadians = Math.toRadians(degree);
            int cX = centerX + (int) (radius * Math.cos(cRadians) / 2);
            int cY = centerY + (int) (radius * Math.sin(cRadians) / 2);
            int cR = radius / 2;
            mRectF.set(cX - cR, cY - cR, cX + cR, cY + cR);
            old.arcTo(mRectF, degree, 180);
            old.close();
            mPath.reset();
            mPath.addCircle(cX, cY, cR / 3, Path.Direction.CW);
            old.op(mPath, Path.Op.DIFFERENCE);

            cX = centerX + (int) (radius * Math.cos(radians) / 2);
            cY = centerY + (int) (radius * Math.sin(radians) / 2);
            mRectF.set(cX - cR, cY - cR, cX + cR, cY + cR);
            mPath.reset();
            mPath.addArc(mRectF, mDegree, 180);
            mPath.close();
            old.op(mPath, Path.Op.DIFFERENCE);

            mPath.reset();
            mPath.moveTo(centerX + (float) (radius * Math.cos(radians)),
                    centerY + (float) (radius * Math.sin(radians)));
            mPath.addCircle(cX, cY, cR / 3, Path.Direction.CW);
            old.op(mPath, Path.Op.UNION);
            return old;
        }
    }
}
