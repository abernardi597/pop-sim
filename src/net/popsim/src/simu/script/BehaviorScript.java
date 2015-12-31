package net.popsim.src.simu.script;

import net.popsim.src.simu.Entity;
import net.popsim.src.simu.World;
import net.popsim.src.util.config.WritableData;

public interface BehaviorScript extends Script {

    void init(World world, Entity entity, WritableData data);

    void behave(World world, Entity entity, WritableData data);

    void finalize(World world, Entity entity, WritableData data);
}
