package net.popsim.src.fxui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.popsim.src.simu.script.Script;
import net.popsim.src.util.config.JsonConfigLoader;

import java.io.File;
import java.util.*;

public class Context implements JsonConfigLoader.Target {

    @Expose
    @SerializedName("Update interval")
    private long mTick;

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

    public Context() {
        // Default values
        mTick = 16; // 60 fps
        mRandomSeedString = "";
        mWorldSize = new int[] {100, 100};
        mScriptInfoMap = new HashMap<>();
    }

    @Override
    public void postLoad() throws Exception {
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
    }

    public long getTick() {
        return mTick;
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
}
