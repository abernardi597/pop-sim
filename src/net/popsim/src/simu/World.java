package net.popsim.src.simu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.popsim.src.fx.ui.Context;
import net.popsim.src.simu.script.PeriodicScript;
import net.popsim.src.util.TickSchedule;
import net.popsim.src.util.Vector;

import java.util.*;

public class World {

    private final Context mContext;
    private final Random mRng;
    private final List<PeriodicScript> mPeriodicScripts;
    private final TickSchedule mScriptSchedule;
    private final ArrayList<Entity> mEntities;
    private final ArrayList<Entity> mToSpawn, mToKill;

    private long mTicks;

    public World(Context context) {
        mContext = context;
        mRng = new Random(mContext.getRngSeed());
        mPeriodicScripts = Collections.unmodifiableList(mContext.getScripts(PeriodicScript.class));
        mScriptSchedule = new TickSchedule(mPeriodicScripts.size(), (index, args) ->
                mPeriodicScripts.get(index).run(this)
        );

        mEntities = new ArrayList<>();
        mToSpawn = new ArrayList<>();
        mToKill = new ArrayList<>();
    }

    public void init() {
        for (PeriodicScript script : mPeriodicScripts)
            script.init(this);

        System.out.printf("World initialized [%dx%d] ", getWidth(), getHeight());
        System.out.printf("seed: %d\n", mContext.getRngSeed());
    }

    public void preUpdate() {
        mScriptSchedule.tick();
        while (mToSpawn.size() > 0)
            mEntities.add(mToSpawn.remove(0).init());
    }

    public void update() {
        mEntities.parallelStream().forEach(Entity::update);
        mTicks++;
    }

    public void postUpdate() {
        mEntities.parallelStream().forEach(Entity::finalizeUpdate);
        mEntities.removeAll(mToKill);
        mToKill.clear();
    }

    public void render(GraphicsContext gfx) {
        gfx.setFill(Color.grayRgb(64));
        gfx.fillRect(0, 0, getWidth(), getHeight());
        mEntities.forEach(entity -> entity.render(gfx));
    }

    public void schedulePeriodicScript(PeriodicScript script, long delay) {
        mScriptSchedule.schedule(mPeriodicScripts.indexOf(script), delay);
    }

    public Entity spawnEntity(Entity.Type type) {
        Entity entity = new Entity(type, this);
        mToSpawn.add(entity);
        return entity;
    }

    public void killEntity(Entity entity) {
        mToKill.add(entity);
    }

    public ArrayList<Entity> getEntitiesInRange(Entity entity, double radius) {
        ArrayList<Entity> results = new ArrayList<>();
        radius *= radius;
        for (Entity e : mEntities)
            if (entity != e && e.getPosition().subtract(entity.getPosition(), new Vector()).squareMag() <= radius)
                results.add(e);
        return results;
    }

    public Entity getClosestEntity(Entity entity) {
        Entity closest = null;
        double best = Double.MAX_VALUE;
        for (Entity e : mEntities) {
            if (e == entity)
                continue;
            double mag = e.getPosition().subtract(entity.getPosition(), new Vector()).squareMag();
            if (mag < best) {
                closest = e;
                best = mag;
            }
        }
        return closest;
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
