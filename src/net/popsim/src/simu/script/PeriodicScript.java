package net.popsim.src.simu.script;

import net.popsim.src.simu.World;

public interface PeriodicScript extends Script {

    void init(World world);

    void run(World world);
}
