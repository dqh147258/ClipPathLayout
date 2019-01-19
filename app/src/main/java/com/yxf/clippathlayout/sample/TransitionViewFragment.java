package com.yxf.clippathlayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.yxf.clippathlayout.transition.TransitionAdapter;
import com.yxf.clippathlayout.transition.TransitionViewLayout;

public class TransitionViewFragment extends Fragment {

    TransitionViewLayout mLayout;
    View mBlueView, mGreenView;

    private int mLastX, mLastY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = (TransitionViewLayout) inflater.inflate(R.layout.fragment_transition_view, null);
        mBlueView = mLayout.findViewById(R.id.blue_view);
        mGreenView = mLayout.findViewById(R.id.green_view);
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                return false;
            }
        };
        mBlueView.setOnTouchListener(listener);
        mGreenView.setOnTouchListener(listener);
        mBlueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionAdapter adapter = mLayout.switchView(mGreenView);
                adapter.setPathCenter(mLastX, mLastY);
                adapter.animate();
            }
        });
        mGreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionAdapter adapter = mLayout.switchView(mBlueView);
                adapter.setPathCenter(mLastX, mLastY);
                adapter.animate();
            }
        });
        return mLayout;
    }
}
