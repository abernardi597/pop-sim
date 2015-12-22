package net.popsim.src.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

// Todo: Document
public class JsonConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    private final Class<? extends Target> mType;

    public JsonConfigLoader(Class<? extends Target> type) {
        mType = type;
    }

    public Target load(File source) throws ConfigException {
        Target target;
        try (FileReader reader = new FileReader(source)) {
            target = GSON.fromJson(reader, mType);
            target.postLoad();
            // Reader is closed automatically
        } catch (FileNotFoundException e) {
            throw new ConfigException("Unable to locate file: " + source.getAbsolutePath(), e);
        } catch (Exception e) {
            throw new ConfigException("Error while loading file", e);
        }
        return target;
    }

    public void save(File destination, Target target) throws ConfigException {
        try (FileWriter writer = new FileWriter(destination)) {
            GSON.toJson(target, writer);
            // Writer is closed automatically
        } catch (Exception e) {
            throw new ConfigException("Error while saving file", e);
        }
    }

    public interface Target {

        /**
         * Called on the Target after it is parsed from JSON.
         *
         * @throws Exception if some error arises.
         */
        void postLoad() throws Exception;
    }

    public static class ConfigException extends Exception {

        public ConfigException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
