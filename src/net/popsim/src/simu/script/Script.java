package net.popsim.src.simu.script;

import net.popsim.src.util.ScriptCompiler;

import java.io.File;

public interface Script {

    @SuppressWarnings("unchecked")
    static <T extends Script> Script compile(String name, File source) throws Exception {
        System.out.printf("Compiling script %s (%s)", name, source.getAbsolutePath());
        Class<T> compiled = ScriptCompiler.compileClass(source.getName().split("\\.")[0], ScriptCompiler.sourceFor(source));
        return compiled.newInstance();
    }
}
