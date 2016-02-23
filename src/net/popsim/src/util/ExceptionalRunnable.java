package net.popsim.src.util;

@FunctionalInterface
public interface ExceptionalRunnable {

    void run() throws Exception;
}
