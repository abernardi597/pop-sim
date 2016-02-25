package net.popsim.src.simu;

import javafx.scene.canvas.GraphicsContext;
import net.popsim.src.fx.ui.Context;

import java.util.ArrayList;
import java.util.Random;

public class World {

    protected final Simulation mSimulation;
    protected final Context mContext;
    protected final Random mRng;
    protected final ArrayList<Entity> mEntities;

    private long mTicks;

    public World(Simulation simulation, Context context) {
        mSimulation = simulation;
        mContext = context;
        mRng = new Random(mContext.getRngSeed());
        mEntities = new ArrayList<>();
    }

    public void init() {
        System.out.printf("World initialized [%dx%d] ", getWidth(), getHeight());
        System.out.printf("seed: %d\n", mContext.getRngSeed());
    }

    public void preUpdate() {}

    public void update() {
        mEntities.parallelStream().forEach(Entity::update);
        mTicks++;
    }

    public void postUpdate() {
        mEntities.parallelStream().forEach(Entity::finish);
    }

    public void render(GraphicsContext gfx) {
        mEntities.forEach(entity -> entity.render(gfx));
    }

    public long getNewRandomSeed() {
        return mRng.nextLong();
    }

    public Context getContext() {
        return mContext;
    }

    public int getWidth() {
        return mContext.getWorldWidth();
    }

    public int getHeight() {
        return mContext.getWorldHeight();
    }

    public long getTicks() {
        return mTicks;
    }
}
