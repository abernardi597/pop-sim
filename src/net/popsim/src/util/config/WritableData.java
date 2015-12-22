package net.popsim.src.util.config;

public interface WritableData extends ReadableData {

    /**
     * Stores a value under a given key.
     *
     * @param key   the key to store under
     * @param value the data to store
     * @param <T>   the type of data being stored
     */
    <T> void set(String key, T value);

    /**
     * Retrieves a value from the data, and then writes over it.
     * It is important to note that the previous data is assumed to be the same type as the new data.
     *
     * @param key   the key to store under
     * @param value the data to store
     * @param def   the default value to return, in case the data is not found
     * @param <T>   the type of data being stored
     *
     * @return The data that was previously stored under the given key or specified default value if no data was found.
     */
    default <T> T getAndSet(String key, T value, T def) {
        T old = get(key, def);
        set(key, value);
        return old;
    }

    /**
     * Retrieves a value from the data, and then writes over it.
     * It is important to note that the previous data is assumed to be the same type as the new data.
     *
     * @param key   the key to store under
     * @param value the data to store.
     * @param <T>   the type of data being stored
     *
     * @return The data that was previously stored under the given key or null if no data was found.
     */
    default <T> T getAndSet(String key, T value) {
        return getAndSet(key, value, null);
    }
}
