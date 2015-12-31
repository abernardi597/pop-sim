package net.popsim.src.simu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.scene.canvas.GraphicsContext;
import net.popsim.src.fxui.Context;
import net.popsim.src.simu.script.BehaviorScript;
import net.popsim.src.simu.script.RenderScript;
import net.popsim.src.util.TickSchedule;
import net.popsim.src.util.Vector;
import net.popsim.src.util.config.Data;

import java.util.*;

public class Entity {

    private final Type mType;
    private final World mWorld;
    private final Random mRng;

    private final Data mData;
    private final Vector mPosition, mFuture;
    private final TickSchedule mBehaviorSchedule, mRenderSchedule;

    public Entity(Type type, World world) {
        mType = type;
        mWorld = world;
        mRng = new Random(mWorld.getNewRandomSeed());

        mData = new Data(mType.mInitialData);

        mPosition = new Vector();
        mFuture = new Vector();

        mBehaviorSchedule = new TickSchedule(mType.mBehaviorScripts.size(), (index, args) ->
                mType.mBehaviorScripts.get(index).behave(mWorld, this, mData));
        mRenderSchedule = new TickSchedule(mType.mRenderScripts.size(), (index, args) ->
                mType.mRenderScripts.get(index).render(mWorld, this, mData, (GraphicsContext) args[0]));
    }

    public Entity init() {
        for (BehaviorScript script : mType.mBehaviorScripts)
            script.init(mWorld, this, mData);
        for (RenderScript script : mType.mRenderScripts)
            script.init(mWorld, this, mData);
        return this;
    }

    public void render(GraphicsContext gfx) {
        mRenderSchedule.tick(gfx);
    }

    public void update() {
        mBehaviorSchedule.tick();
    }

    public void finalizeUpdate() {
        for (BehaviorScript script : mType.mBehaviorScripts)
            script.finalize(mWorld, this, mData);
        mPosition.set(mFuture.mX, mFuture.mY);
    }

    public void scheduleBehavior(BehaviorScript script, long delay) {
        mBehaviorSchedule.schedule(mType.mBehaviorScripts.indexOf(script), delay);
    }

    public void scheduleRender(RenderScript script, long delay) {
        mRenderSchedule.schedule(mType.mRenderScripts.indexOf(script), delay);
    }

    public Type getType() {
        return mType;
    }

    public Random getRng() {
        return mRng;
    }

    public void setPosition(double x, double y) {
        mPosition.set(x, y);
        mFuture.set(x, y);
    }

    public double getXPosition() {
        return mPosition.mX;
    }

    public double getYPosition() {
        return mPosition.mY;
    }

    public Vector getFuturePosition() {
        return mFuture;
    }

    public Vector getPosition() {
        return mPosition;
    }

    public static class Type {

        @Expose
        @SerializedName("Behavior scripts")
        private String[] mBehaviorScriptInfo;
        private List<BehaviorScript> mBehaviorScripts;

        @Expose
        @SerializedName("Render scripts")
        private String[] mRenderScriptInfo;
        private List<RenderScript> mRenderScripts;

        @Expose
        @SerializedName("Data")
        private Map<String, Object> mInitialData;

        private String mName;

        public Type() {
            mBehaviorScriptInfo = new String[0];
            mRenderScriptInfo = new String[0];
            mInitialData = new HashMap<>();
            mName = "";
        }

        public void postLoad(Context context, String key) throws Exception {
            mBehaviorScripts = Collections.unmodifiableList(context.getScripts(BehaviorScript.class, mBehaviorScriptInfo));
            mRenderScripts = Collections.unmodifiableList(context.getScripts(RenderScript.class, mRenderScriptInfo));
            mInitialData = Collections.unmodifiableMap(mInitialData);
            mName = key;
        }

        public String getName() {
            return mName;
        }
    }
}
