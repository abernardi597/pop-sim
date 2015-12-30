package net.popsim.src.util;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Class containing methods for compiling Java source at runtime.
 */
public class ScriptCompiler {

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
     * Assigns values to static fields and initializes the working directories.
     *
     * @throws Exception if something goes wrong (No java compiler, or File exceptions)
     */
    public static void init() throws Exception {
        //Get compiler
        COMPILER = ToolProvider.getSystemJavaCompiler();
        if (COMPILER == null)
            throw new Exception("No Java compiler on system");
        //Find output directory
        DIR_OUTPUT = new File(ScriptCompiler.class.getProtectionDomain().getCodeSource().getLocation().getFile().replace("%20", " "));
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
        //Find package declaration
        String pack = src.substring(8, src.indexOf(';'));
        // Make it into a relative path
        File outputDir = new File(DIR_OUTPUT, pack.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
        if (!outputDir.exists() && !outputDir.mkdirs())
            throw new IOException("Unable to create compilation output directory " + outputDir);
        outputDir.deleteOnExit();
        // Predict the compiled file
        File destination = new File(outputDir, name + ".class");
        if (destination.exists()) //Check if destination already exists
            throw new Exception("File already exists: " + destination);
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
        return Class.forName(pack + "." + name);
    }

    public static String sourceFor(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine())
            sb.append(scanner.nextLine()).append("\n");
        return sb.toString();
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
