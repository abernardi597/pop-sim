package net.popsim.src.simu;

import javafx.scene.canvas.GraphicsContext;

import java.util.*;
import java.util.function.Consumer;

public class World {

    private static <T> List<T> makeUnmodifiableCopy(List<T> list) {
        List<T> temp = new ArrayList<>(list.size());
        temp.addAll(list);
        return Collections.unmodifiableList(temp);
    }

    protected final Simulation mSimulation;
    protected final Context mContext;
    protected final Random mRng;
    protected final ArrayList<Entity> mEntities;

    private List<Entity> mEntityFreeze;
    private long mTicks;

    public World(Simulation simulation, Context context) {
        mSimulation = simulation;
        mContext = context;
        mRng = new Random(mContext.getRngSeed());
        mEntityFreeze = makeUnmodifiableCopy(mEntities = new ArrayList<>());
    }

    public void init() {
        System.out.printf("World initialized [%dx%d] ", getWidth(), getHeight());
        System.out.printf("seed: %d\n", mContext.getRngSeed());
    }

    public void preUpdate() {
        mEntityFreeze = makeUnmodifiableCopy(mEntities);
    }

    public void update() {
        forEachEntityParallel(Entity::update);
        mTicks++;
    }

    public void postUpdate() {
        forEachEntityParallel(Entity::finish);
    }

    public void render(GraphicsContext gfx) {
        forEachEntity(entity -> entity.render(gfx));
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

    public synchronized void spawnEntity(Entity e) {
        if (e == null)
            throw new IllegalArgumentException("Cannot spawn null entity");
        else mEntities.add(e);
    }

    public synchronized void killEntity(Entity e) {
        mEntities.remove(e);
    }

    public Iterator<Entity> getEntityIterator() {
        return getEntityIterator(0);
    }

    public Iterator<Entity> getEntityIterator(Entity start) {
        int index = mEntityFreeze.indexOf(start);
        if (index < 0 || index >= mEntityFreeze.size())
            throw new IllegalArgumentException("Entity is not in the entity freeze");
        return getEntityIterator(index + 1);
    }

    public Iterator<Entity> getEntityIterator(int start) {
        return mEntityFreeze.listIterator(start);
    }

    public void forEachEntity(Consumer<? super Entity> action) {
        mEntityFreeze.forEach(action);
    }

    public void forEachEntityParallel(Consumer<? super Entity> action) {
        mEntityFreeze.parallelStream().forEach(action);
    }

    public long getTicks() {
        return mTicks;
    }
}
