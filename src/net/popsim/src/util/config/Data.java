package net.popsim.src.util.config;

import java.util.HashMap;

// Todo: Document
public class Data implements WritableData {

    protected final HashMap<String, Object> mDataMap;

    public Data(HashMap<? extends String, ?> map) {
        mDataMap = new HashMap<>(map);
    }

    public Data() {
        this(new HashMap<>());
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
}
