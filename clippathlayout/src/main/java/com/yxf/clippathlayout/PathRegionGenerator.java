package com.yxf.clippathlayout;

import android.graphics.Path;

public interface PathRegionGenerator {

    PathRegion generatorPathRegion(Path path, int clipType, int width, int height);

}
