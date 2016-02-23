package net.popsim.src.fx.ui;

import net.popsim.src.util.ScriptCompiler;
import net.popsim.src.util.config.JsonConfigLoader;
import net.popsim.src.util.io.HeadedPrintStream;
import net.popsim.src.util.io.SplitPrintStream;

import java.io.*;
import java.util.*;

public class ContextHelper {

    /**
     * The working directory at runtime.
     */
    public static final File DIR_HOME = new File("").getAbsoluteFile();
    /**
     * The name of this application.
     */
    public static final String APP_NAME = "PopSim";
    private static final JsonConfigLoader CONFIG_LOADER = new JsonConfigLoader(Context.class);

    public static void setupOutput() throws Exception {
        //Set up our standard out and err wrapper streams. These catch output and sneak a header in, as well as write it to a file.
        try {
            // First move the old logs
            String outForm = "out_%d.log";
            String errForm = "err_%d.log";
            int logPersist = 1;
            File logDir = new File(DIR_HOME, "log");
            if (!logDir.exists())
                if (!logDir.mkdir())
                    throw new Exception("Unable to create log directory");
            File oldOut = new File(logDir, String.format(outForm, logPersist));
            File oldErr = new File(logDir, String.format(errForm, logPersist));
            for (int i = logPersist; i > 0; i--) {
                if (oldOut.exists())
                    if (!oldOut.delete())
                        System.err.println("Unable to delete file " + oldOut);
                File newOut = new File(logDir, i > 1? String.format(outForm, i - 1) : "out.log");
                if (!newOut.renameTo(oldOut))
                    System.err.println("Unable to rename file from " + newOut + " to " + oldOut);
                oldOut = newOut;

                if (oldErr.exists())
                    if (!oldErr.delete())
                        System.err.println("Unable to delete file " + oldErr);
                File newErr = new File(logDir, i > 1? String.format(errForm, i - 1) : "err.log");
                if (!newErr.renameTo(oldErr))
                    System.err.println("Unable to rename file from " + newErr + " to " + oldErr);
                oldErr = newErr;
            }
            // Set the system out and err to ones with headers and split to files
            System.setOut(new HeadedPrintStream(new SplitPrintStream(System.out, new FileOutputStream(oldOut)), true));
            System.setErr(new HeadedPrintStream(new SplitPrintStream(System.err, new FileOutputStream(oldErr)), true));
        } catch (IOException e) {
            throw new Exception("Unable to log to files", e);
        }
        System.out.println(String.format("%s Standard Output Stream", APP_NAME));
        System.err.println(String.format("%s Standard Error Stream", APP_NAME));
    }

    public static void setupCompiler() throws Exception {
        try {
            System.out.println("Initializing ScriptCompiler");
            ScriptCompiler.init();
        } catch (Exception e) {
            throw new Exception("Unable to initialize ScriptCompiler", e);
        }
    }

    public static Context createContextFile(File file) throws JsonConfigLoader.ConfigException {
        Context c = new Context();
        CONFIG_LOADER.save(file, c);
        try {
            c.postLoad();
        } catch (Exception e) {
            throw new JsonConfigLoader.ConfigException("Error with default values", e);
        }
        return c;
    }

    public static Context makeContext(File file) throws JsonConfigLoader.ConfigException {
        if (!file.exists())
            return createContextFile(file);
        else return (Context) CONFIG_LOADER.load(file);
    }

    public static long parseSeed(String seed) {
        if (seed == null || seed.isEmpty())
            return System.currentTimeMillis();
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException e) {
            return seed.hashCode();
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V, A extends V> List<A> findInMap(Map<K, V> map, Class<A> type, K[] keys) {
        ArrayList<A> result = new ArrayList<>();
        List<K> toCheck;
        if (keys.length > 0)
            toCheck = Arrays.asList(keys);
        else toCheck = new ArrayList<>(map.keySet());
        for (K key : toCheck) {
            V value = map.get(key);
            if (value != null && type.isInstance(value))
                result.add((A) value);
        }
        return result;
    }
}
