package com.yxf.clippathlayout.transition.generator;

import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.yxf.clippathlayout.Utils;
import com.yxf.clippathlayout.pathgenerator.PathGenerator;

import java.util.ArrayList;
import java.util.List;

public class RandomTransitionPathGenerator implements TransitionPathGenerator {

    private static final String TAG = Utils.getTAG(RandomTransitionPathGenerator.class);


    private List<PathGenerator> mPathGeneratorList = new ArrayList<PathGenerator>();

    private PathGenerator mPathGenerator;

    private Path mPath;

    public RandomTransitionPathGenerator(PathGenerator defaultGenerator) {
        if (defaultGenerator == null) {
            throw new NullPointerException("default generator is null");
        }
        addInternal(defaultGenerator);
    }

    public void add(PathGenerator generator) {
        addInternal(generator);
    }

    private void addInternal(PathGenerator generator) {
        if (generator == null) {
            Log.e(TAG, "add: generator is null");
            return;
        }
        mPathGeneratorList.add(generator);
    }

    public void clear(PathGenerator defaultGenerator) {
        if (defaultGenerator == null) {
            throw new NullPointerException("default generator is null");
        }
        mPathGeneratorList.clear();
        addInternal(defaultGenerator);
    }

    @Override
    public Rect maxContainSimilarRange(Rect similar, int boundWidth, int boundHeight) {
        if (mPathGenerator instanceof TransitionPathGenerator) {
            return ((TransitionPathGenerator) mPathGenerator).maxContainSimilarRange(similar, boundWidth, boundHeight);
        } else {
            return Utils.maxContainSimilarRange(mPath, similar, boundWidth, boundHeight);
        }
    }

    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        updateGenerator();
        mPath = mPathGenerator.generatePath(old, view, width, height);
        return mPath;
    }

    private void updateGenerator() {
        int size = mPathGeneratorList.size();
        int index = (int) (Math.random() * size);
        mPathGenerator = mPathGeneratorList.get(index);
    }
}
