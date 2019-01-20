package com.yxf.clippathlayout.transition;

import android.view.View;

import com.yxf.clippathlayout.ClipPathLayout;

public interface TransitionLayout extends ClipPathLayout {

    void setAdapter(TransitionAdapter adapter);

    TransitionAdapter switchView(View view);

    TransitionAdapter switchView(View view, boolean reverse);

    void update(boolean finished);

}
