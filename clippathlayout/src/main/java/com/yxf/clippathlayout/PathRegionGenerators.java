package com.yxf.clippathlayout;

import android.graphics.Path;

public class PathRegionGenerators {

    public static PathRegionGenerator createBitmapPathRegionGenerator() {

        return new PathRegionGenerator() {

            @Override
            public PathRegion generatorPathRegion(Path path, int clipType, int width, int height) {
                return new BitmapPathRegion(path, clipType, width, height);
            }
        };
    }

    public static PathRegionGenerator createBitmapPathRegionGenerator(final int inSampleSize) {
        return new PathRegionGenerator() {
            @Override
            public PathRegion generatorPathRegion(Path path, int clipType, int width, int height) {
                return new BitmapPathRegion(path, width, height, inSampleSize);
            }
        };
    }

}
