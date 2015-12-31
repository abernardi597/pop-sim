package net.popsim.src.simu.script;

import javafx.scene.canvas.GraphicsContext;
import net.popsim.src.simu.Entity;
import net.popsim.src.simu.World;
import net.popsim.src.util.config.WritableData;

public interface RenderScript extends Script {

    void init(World world, Entity entity, WritableData data);

    void render(World world, Entity entity, WritableData data, GraphicsContext gfx);
}
