package net.popsim.src.simu;

public interface PeriodicScript extends Script {

    void init(World world);

    void run(World world);
}
