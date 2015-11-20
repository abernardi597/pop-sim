package net.popsim.src.internal;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

/**
 * Class containing methods for compiling Java source at runtime.
 */
public class SnippetCompiler {

    /**
     * The system's compiler.
     *
     * @see ToolProvider#getSystemJavaCompiler()
     */
    private static JavaCompiler COMPILER;
    /**
     * The directory where the compiled classes reside.
     */
    private static File DIR_OUTPUT;
    /**
     * The directory to store compiled snippets. Namely, "DIR_OUTPUT/net/june/src/snippets".
     *
     * @see #DIR_OUTPUT
     */
    private static File DIR_CLASSES;

    /**
     * Assigns values to static fields and initializes the working directories.
     *
     * @throws Exception if something goes wrong (No java compiler, or File exceptions)
     */
    public static void init() throws Exception {
        //Get compiler
        COMPILER = ToolProvider.getSystemJavaCompiler();
        if (COMPILER == null)
            throw new Exception("No Java compiler on system");
        //Find output directories
        DIR_OUTPUT = new File(SnippetCompiler.class.getProtectionDomain().getCodeSource().getLocation().getFile().replace("%20", " "));
        DIR_CLASSES = new File(DIR_OUTPUT, "net/popsim/src/snippets");
        //Delete if it already exists
        if (DIR_CLASSES.exists())
            deleteAll(DIR_CLASSES);
        else if (!DIR_CLASSES.mkdir())
            throw new IOException("Unable to create snippet compilation directory: " + DIR_CLASSES);
        //Make sure the directory is cleaned after runtime, so that it is fresh for another run
        DIR_CLASSES.deleteOnExit();
    }

    /**
     * Compiles the given Class.
     *
     * @param name the name of the Class (filename without .java)
     * @param src  the literal source of the class
     *
     * @return The compiled Class as a runtime object.
     * @throws Exception if there was a compilation error, an error finding the class
     */
    public static Class compileClass(String name, String src) throws Exception {
        File destination = new File(DIR_CLASSES, name + ".class");
        if (destination.exists()) //Check if destination already exists
            throw new Exception("File already exists: " + destination);
        //Prepend package declaration
        src = "package net.popsim.src.snippets;\n" + src;
        //Create classpath
        StringBuilder cp = new StringBuilder();
        for (URL url : ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
            cp.append(url.getFile().replace("%20", " ")).append(File.pathSeparator);
        //Construct argument list
        ArrayList<String> args = new ArrayList<>();
        args.add("-d"); //Destination flag
        args.add(DIR_OUTPUT.getAbsolutePath());
        args.add("-classpath"); //Classpath
        args.add(cp.toString());
        //For compilation messages
        StringWriter out = new StringWriter();
        if (!COMPILER.getTask(out, null, null, args, null, Collections.singletonList(new StringSource(name, src))).call())
            throw new Exception("Unable to compile file:\n" + out.toString());
        //Mark new file for deletion when runtime terminates to keep directory clean
        destination.deleteOnExit();
        //Find the class and return it
        return Class.forName("net.popsim.src.snippets." + name);
    }

    public static String sourceFor(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
            sb.append(scanner.nextLine()).append("\n");
        return sb.toString();
    }

    /**
     * Recursively deletes everything in the given directory. Does not delete the directory itself.
     *
     * @param dir the directory to purge
     */
    private static void deleteAll(File dir) {
        File[] subs = dir.listFiles();
        if (subs != null)
            for (File sub : subs) {
                if (sub.isDirectory())
                    deleteAll(sub);
                sub.delete();
            }
    }

    /**
     * Checks if the given arguments can be used to satisfy the parameters of a given method
     *
     * @param m    the method to check
     * @param args the arguments to check
     *
     * @return True if the method can be invoked with the arguments, false otherwise
     */
    private static boolean argsApplicable(Method m, Object[] args) {
        Class[] argClasses = m.getParameterTypes();
        if (argClasses.length == args.length) {
            for (int i = 0; i < args.length; i++)
                if (!argClasses[i].isInstance(args[i]))
                    return false;
            return true;
        }
        else return false;
    }

    /**
     * Small internal class to represent Java source as a string.
     */
    private static class StringSource extends SimpleJavaFileObject {

        /**
         * String containing the source code.
         */
        private final String mCode;

        /**
         * Constructs a source file object with the given name and source code.
         *
         * @param name the name of the class
         * @param code the source code
         */
        public StringSource(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            mCode = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return mCode;
        }
    }
}
