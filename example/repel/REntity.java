package example.repel;

import example.PositionEntity;
import javafx.scene.paint.Color;
import net.popsim.src.simu.Entity;
import net.popsim.src.simu.World;
import net.popsim.src.util.Vector;

public class REntity extends PositionEntity {

    public static final double DAMP = 0.99;
    public static final double RANGE = 128;
    public static final double RANGE2 = RANGE * RANGE;

    public final Vector mA;
    private double mTimestep;

    public REntity(World world) {
        super(world, 1.5, Color.WHITE);
        mA = new Vector();
        mTimestep = 1D / mWorld.getContext().getTickFrequency();
    }

    @Override
    public void update() {
        Vector p = new Vector();
        for (Entity e : mWorld.getEntities()) {
            if (e instanceof REntity && e != this) {
                REntity me = (REntity) e;
                Vector diff = mCurrentPosition.subtract(me.mCurrentPosition, new Vector());
                double mag2 = diff.squareMag();
                if (mag2 <= RANGE2) {
                    while (mag2 == 0)
                        mag2 = diff.set((mRng.nextDouble() - 0.5), (mRng.nextDouble() - 0.5)).squareMag();
                    diff.multiply(1 / mag2, diff);
                    p.add(diff, p);
                }
            }
        }
        mA.add(p.multiply(512, p), mA);
        Vector v = mCurrentPosition.subtract(mPastPosition, new Vector());
        v.multiply(DAMP / mTimestep, v); // Dampen and fix to actual velocity
        v.add(mA.multiply(mTimestep, p), v);
        mFuturePosition.add(v.multiply(mTimestep, v), mFuturePosition);
    }

    @Override
    public void finish() {
        boundCheck(DAMP);
        super.finish();
        mA.set(0, 0);
    }
}
