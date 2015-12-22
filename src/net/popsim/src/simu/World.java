package net.popsim.src.simu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.popsim.src.fxui.Context;
import net.popsim.src.util.TickSchedule;

import java.util.*;

public class World {

    private final Context mContext;
    private final Random mRng;
    private final List<PeriodicScript> mPeriodicScripts;
    private final TickSchedule mScriptSchedule;

    private long mTicks;

    public World(Context context) {
        mContext = context;
        mRng = new Random(mContext.getRngSeed());
        mPeriodicScripts = Collections.unmodifiableList(mContext.getScripts(PeriodicScript.class));
        mScriptSchedule = new TickSchedule(mPeriodicScripts.size(), (index, args) ->
                mPeriodicScripts.get(index).run(this)
        );

        for (PeriodicScript script : mPeriodicScripts)
            script.init(this);

        System.out.printf("World initialized [%dx%d] ", getWidth(), getHeight());
        System.out.printf("seed: %d\n", mContext.getRngSeed());
    }

    public void update() {
        mScriptSchedule.tick();
        mTicks++;
    }

    public void render(GraphicsContext gfx) {
        gfx.setFill(Color.grayRgb(64));
        gfx.fillRect(0, 0, getWidth(), getHeight());
    }

    public void schedulePeriodicScript(PeriodicScript script, long delay) {
        mScriptSchedule.schedule(mPeriodicScripts.indexOf(script), delay);
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
