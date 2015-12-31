package net.popsim.src.fxui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.popsim.src.simu.Entity;
import net.popsim.src.simu.script.Script;
import net.popsim.src.util.config.JsonConfigLoader;

import java.io.File;
import java.util.*;
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
    @SerializedName("Scripts")
    private Map<String, String> mScriptInfoMap;
    private Map<String, Script> mScriptMap;

    @Expose
    @SerializedName("Entity types")
    private Map<String, Entity.Type> mEntityTypes;

    public Context() {
        // Default values
        mTickFrequency = 60; // 60 Hz
        mRandomSeedString = "";
        mWorldSize = new int[] {100, 100};
        mScriptInfoMap = new HashMap<>();
        mEntityTypes = new HashMap<>();
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
        // Script map
        HashMap<String, Script> sMap = new HashMap<>(mScriptInfoMap.size());
        for (Map.Entry<String, String> e : mScriptInfoMap.entrySet()) {
            String name = e.getKey();
            if (sMap.containsKey(name))
                throw new Exception("Duplicate script name: " + name);
            Script script = Script.compile(name, new File(ContextHelper.DIR_HOME, e.getValue()));
            sMap.put(name, script);
        }
        mScriptMap = Collections.unmodifiableMap(sMap);
        // Entity types
        for (Map.Entry<String, Entity.Type> entry : mEntityTypes.entrySet())
            entry.getValue().postLoad(this, entry.getKey());
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

    public <T extends Script> List<T> getScripts(Class<T> type, String... names) {
        return ContextHelper.findInMap(mScriptMap, type, names);
    }

    public List<Entity.Type> getEntityTypes(String... names) {
        return ContextHelper.findInMap(mEntityTypes, Entity.Type.class, names);
    }
}
