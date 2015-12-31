package net.popsim.src.util.config;

public interface ReadableData {

    /**
     * Retrieves a value from the data that is stored under a specified key, or a default value if no such entry exists.
     *
     * @param key the key to look up
     * @param def a default value to return, in case the key is not found
     * @param <T> the type of the requested data
     *
     * @return The value stored under the specified key or, if no value is found, the specified value.
     * If a value is found, but is of a different type, a ClassCastException will be thrown.
     */
    <T> T get(String key, T def);

    /**
     * Checks if a key exists in the data.
     *
     * @param key the key to look up
     *
     * @return True if there is a value associated with the given key, false otherwise.
     */
    boolean containsKey(String key);
}
