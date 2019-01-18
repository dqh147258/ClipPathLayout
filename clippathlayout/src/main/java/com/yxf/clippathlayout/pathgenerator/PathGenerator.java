package com.yxf.clippathlayout.pathgenerator;

import android.graphics.Path;
import android.view.View;

public interface PathGenerator {

    Path generatePath(Path old, View view, int width, int height);

}
