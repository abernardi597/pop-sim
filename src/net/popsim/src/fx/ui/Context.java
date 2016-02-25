package net.popsim.src.fx.ui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.popsim.src.simu.World;
import net.popsim.src.util.Compiler;
import net.popsim.src.util.config.JsonConfigLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

    @Expose
    @SerializedName("World class")
    private String mWorldClassName;
    private Class<? extends World> mWorldClass;

    @Expose
    @SerializedName("Entity classes")
    private String[] mEntityClassNames;

    public Context() {
        // Default values
        mTickFrequency = 60; // 60 Hz
        mRandomSeedString = "";
        mWorldSize = new int[] {100, 100};
        mWorldClassName = World.class.getName();
        mEntityClassNames = new String[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postLoad() throws Exception {
        // Tick frequency
        mTickInterval = TimeUnit.SECONDS.toNanos(1) / mTickFrequency;
        // Random seed
        mRngSeed = ContextHelper.parseSeed(mRandomSeedString);
        // World dimensions
        if (mWorldSize.length != 2)
            throw new Exception("World dimensions should be two dimensional");
        // Store things to compile so we can one-hit KO
        List<Compiler.FileSource> toCompile = new ArrayList<>();
        // World class
        File src = ContextHelper.packageToFile(mWorldClassName);
        if (src.exists()) // If there is a compilation target, try to compile it
            toCompile.add(new Compiler.FileSource(src, mWorldClassName));
        // Entity classes
        for (String n : mEntityClassNames)
            toCompile.add(new Compiler.FileSource(ContextHelper.packageToFile(n), n));
        // Compile everything, if anything
        if (toCompile.size() > 0)
            Compiler.compile(toCompile);
        // Find the new (or old) classes
        Class c = Class.forName(mWorldClassName);
        if (World.class.isAssignableFrom(c))
            mWorldClass = (Class<? extends World>) Class.forName(mWorldClassName);
        else throw new Exception(mWorldClassName + " is not a subclass of World");
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

    public Class<? extends World> getWorldClass() {
        return mWorldClass;
    }
}
