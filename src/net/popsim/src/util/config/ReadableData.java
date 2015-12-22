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
}
