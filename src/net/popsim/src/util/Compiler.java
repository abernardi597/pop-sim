package net.popsim.src.util;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Compiler {

    private static JavaCompiler COMPILER;
    private static File DIR_OUTPUT;
    private static ArrayList<JavaFileObject> TASK_LIST;

    public static void makeTempDirs(File f) throws IOException {
        Stack<File> toMake = new Stack<>();
        while (!f.exists()) {
            toMake.push(f);
            f = f.getParentFile();
        }
        while (!toMake.isEmpty()) {
            f = toMake.pop();
            if (!f.mkdir())
                throw new IOException("Unable to create directory: " + f.getAbsolutePath());
            f.deleteOnExit();
        }
    }

    public static void init() throws Exception {
        COMPILER = ToolProvider.getSystemJavaCompiler();
        if (COMPILER == null)
            throw new Exception("No java compiler on system. Is only the JDK installed?");
        DIR_OUTPUT = new File(Compiler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!DIR_OUTPUT.exists() && !DIR_OUTPUT.mkdirs())
            throw new Exception("Output directory inaccessible");
        TASK_LIST = new ArrayList<>();
    }

    public static File getOutputDir() {
        return DIR_OUTPUT;
    }

    public static void compile(FileSource... sources) throws Exception {
        // Prepare each file for compilation
        for (FileSource src : sources) {
            File dest = src.getCompiledLocation(DIR_OUTPUT);
            if (!dest.exists()) {
                // Make the compilation directory temporary, so it doesn't linger
                makeTempDirs(dest.getParentFile());
            }
            else if (!dest.delete())
                throw new IOException("Unable to delete file: " + dest);
        }
        List<String> args = new ArrayList<>();
        // Create classpath
        StringBuilder cp = new StringBuilder();
        for (URL url : ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
            cp.append(new File(url.toURI()).getAbsolutePath()).append(File.pathSeparator);
        // Construct argument list
        // Destination flag
        args.add("-d");
        args.add(DIR_OUTPUT.getAbsolutePath());
        // Classpath
        args.add("-classpath");
        args.add(cp.toString());
        // Output for compiler
        StringWriter out = new StringWriter();
        if (!COMPILER.getTask(out, null, null, args, null, Arrays.asList(sources)).call())
            throw new Exception(String.format("Unable to compile classes: %s\n%s", Arrays.toString(sources), out.toString().trim()));
        // Cleanup after runtime terminates
        for (FileSource src : sources)
            src.getCompiledLocation(DIR_OUTPUT).deleteOnExit();
    }

    public static class FileSource extends SimpleJavaFileObject {

        private final String mClassName;

        public FileSource(File src, String qualifiedName) {
            super(src.toURI(), Kind.SOURCE);
            mClassName = qualifiedName;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return uri.toURL().openStream();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            try (InputStream in = openInputStream()) {
                Scanner scan = new Scanner(in);
                StringBuilder buf = new StringBuilder();
                while (scan.hasNextLine())
                    buf.append(scan.nextLine()).append('\n');
                return buf.toString();
            } catch (IOException e) {
                throw new IOException("Unable to read " + uri, e);
            }
        }

        @Override
        public String toString() {
            return getClassName();
        }

        public String getClassName() {
            return mClassName;
        }

        public File getCompiledLocation(File outDir) {
            String path = getClassName().replace('.', File.separatorChar);
            return new File(outDir, path + ".class");
        }
    }
}
