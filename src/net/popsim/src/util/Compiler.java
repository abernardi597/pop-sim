package net.popsim.src.util;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Compiler {

    private static JavaCompiler COMPILER;
    private static File DIR_OUTPUT;

    public static Stack<File> stackSubFiles(File parent, Stack<File> results) {
        File[] contents = parent.listFiles();
        if (contents != null)
            for (File f : contents) {
                if (f != null && f.isDirectory())
                    stackSubFiles(f, results);
                results.push(f);
            }
        return results;
    }

    public static void init() throws Exception {
        COMPILER = ToolProvider.getSystemJavaCompiler();
        if (COMPILER == null)
            throw new Exception("No java compiler on system. Is only the JDK installed?");
        DIR_OUTPUT = new File(Compiler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!DIR_OUTPUT.exists() && !DIR_OUTPUT.mkdirs())
            throw new Exception("Output directory inaccessible");
    }

    public static File getOutputDir() {
        return DIR_OUTPUT;
    }

    public static void compile(FileSource... sources) throws Exception {
        compile(Arrays.asList(sources));
    }

    public static void compile(List<FileSource> sources) throws Exception {
        // Prepare each file for compilation
        Stack<File> original;
        stackSubFiles(DIR_OUTPUT, original = new Stack<>());
        System.out.println("Cleaning output dir");
        for (FileSource src : sources) {
            File dest = src.getCompiledLocation(DIR_OUTPUT);
            if (dest.exists())
                if (!dest.delete())
                    throw new IOException("Unable to delete file: " + dest);
                else System.out.println("Deleted " + dest);
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
        System.out.println("Compiling classes:");
        for (FileSource f : sources)
            System.out.println("  " + f.getClassName());
        StringWriter out = new StringWriter();
        if (!COMPILER.getTask(out, null, null, args, null, sources).call())
            throw new Exception(String.format("Unable to compile classes: %s\n%s", sources, out.toString().trim()));
        // Calculate diff
        Stack<File> diff;
        (diff = stackSubFiles(DIR_OUTPUT, new Stack<>())).removeAll(original);
        // Mark for deletion
        System.out.println("Compilation output: " + DIR_OUTPUT);
        while (!diff.isEmpty()) {
            String path = diff.peek().getAbsolutePath();
            System.out.println("  " + path.substring(DIR_OUTPUT.getAbsolutePath().length()));
            diff.pop().deleteOnExit();
        }
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
