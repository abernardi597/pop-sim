package net.popsim.src.simu;

import javafx.scene.canvas.GraphicsContext;
import net.popsim.src.fx.ui.Context;

import java.util.Random;

public abstract class Entity {

    protected final Context mContext;
    protected final World mWorld;
    protected final Random mRng;

    public Entity(World world) {
        mContext = world.getContext();
        mWorld = world;
        mRng = new Random(world.getNewRandomSeed());
    }

    public abstract void update();

    public abstract void finish();

    public abstract void render(GraphicsContext gfx);
}
