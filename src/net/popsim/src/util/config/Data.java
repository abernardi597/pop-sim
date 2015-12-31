package net.popsim.src.util.config;

import java.util.HashMap;
import java.util.Map;

// Todo: Document
public class Data implements WritableData {

    protected final HashMap<String, Object> mDataMap;

    public Data(Map<? extends String, ?> map) {
        if (map != null)
            mDataMap = new HashMap<>(map);
        else mDataMap = new HashMap<>();
    }

    public Data() {
        this(null);
    }

    @Override
    public <T> void set(String key, T value) {
        mDataMap.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        if (mDataMap.containsKey(key))
            def = (T) mDataMap.get(key); // Will throw a ClassCastException if types are incompatible
        return def;
    }

    @Override
    public boolean containsKey(String key) {
        return mDataMap.containsKey(key);
    }
}
