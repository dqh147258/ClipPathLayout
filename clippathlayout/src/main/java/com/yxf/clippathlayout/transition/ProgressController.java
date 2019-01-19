package com.yxf.clippathlayout.transition;

public interface ProgressController {

    /**
     * @param percent 0 <= percent <= 1
     */
    void setProgress(float percent);

    float getProgress();

}
