package com.yxf.clippathlayout.transition.generator;

import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.yxf.clippathlayout.Utils;

import java.util.ArrayList;
import java.util.List;

public class RandomTransitionPathGenerator implements TransitionPathGenerator {

    private static final String TAG = Utils.getTAG(RandomTransitionPathGenerator.class);


    private List<TransitionPathGenerator> mTransitionPathGeneratorList = new ArrayList<TransitionPathGenerator>();

    private TransitionPathGenerator mTransitionPathGenerator;

    public RandomTransitionPathGenerator(TransitionPathGenerator defaultGenerator) {
        if (defaultGenerator == null) {
            throw new NullPointerException("default generator is null");
        }
        addInternal(defaultGenerator);
    }

    public void add(TransitionPathGenerator generator) {
        addInternal(generator);
    }

    private void addInternal(TransitionPathGenerator generator) {
        if (generator == null) {
            Log.e(TAG, "add: generator is null");
            return;
        }
        mTransitionPathGeneratorList.add(generator);
    }

    public void clear(TransitionPathGenerator defaultGenerator) {
        if (defaultGenerator == null) {
            throw new NullPointerException("default generator is null");
        }
        mTransitionPathGeneratorList.clear();
        addInternal(defaultGenerator);
    }

    @Override
    public Rect maxContainSimilarRange(Rect viewRange) {
        updateGenerator();
        return mTransitionPathGenerator.maxContainSimilarRange(viewRange);
    }

    @Override
    public Path generatePath(Path old, View view, int width, int height) {
        return mTransitionPathGenerator.generatePath(old, view, width, height);
    }

    private void updateGenerator() {
        int size = mTransitionPathGeneratorList.size();
        int index = (int) (Math.random() * size);
        mTransitionPathGenerator = mTransitionPathGeneratorList.get(index);
    }
}
