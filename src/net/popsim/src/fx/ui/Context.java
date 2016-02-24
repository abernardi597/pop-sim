package net.popsim.src.fx.ui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.popsim.src.util.config.JsonConfigLoader;

import java.util.concurrent.TimeUnit;

public class Context implements JsonConfigLoader.Target {

    @Expose
    @SerializedName("Update frequency")
    private long mTickFrequency;
    private long mTickInterval;

    @Expose
    @SerializedName("Random seed")
    private String mRandomSeedString;
    private long mRngSeed;

    @Expose
    @SerializedName("World dimensions")
    private int[] mWorldSize;

    public Context() {
        // Default values
        mTickFrequency = 60; // 60 Hz
        mRandomSeedString = "";
        mWorldSize = new int[] {100, 100};
    }

    @Override
    public void postLoad() throws Exception {
        // Tick frequency
        mTickInterval = TimeUnit.SECONDS.toNanos(1) / mTickFrequency;
        // Random seed
        mRngSeed = ContextHelper.parseSeed(mRandomSeedString);
        // World dimensions
        if (mWorldSize.length != 2)
            throw new Exception("World dimensions should be two dimensional");
    }

    public long getTickFrequency() {
        return mTickFrequency;
    }

    public long getTickInterval() {
        return mTickInterval;
    }

    public long getRngSeed() {
        return mRngSeed;
    }

    public int getWorldWidth() {
        return mWorldSize[0];
    }

    public int getWorldHeight() {
        return mWorldSize[1];
    }
}
