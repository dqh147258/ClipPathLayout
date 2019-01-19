package com.yxf.clippathlayout.transition;

import android.view.View;

import com.yxf.clippathlayout.ClipPathLayout;

public interface TransitionLayout extends ClipPathLayout, ProgressController {

    void setAdapter(TransitionAdapter adapter);

    TransitionAdapter switchView(View view);

}
