package example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.popsim.src.simu.Entity;
import net.popsim.src.simu.World;
import net.popsim.src.util.Vector;

public abstract class PositionEntity extends Entity {

    protected final Vector mPastPosition, mCurrentPosition, mFuturePosition;
    protected double mSize;
    protected Color mColor;

    public PositionEntity(World world, double r, Color color) {
        super(world);
        mPastPosition = new Vector();
        mCurrentPosition = new Vector();
        mFuturePosition = new Vector();
        mSize = r;
        mColor = color;
    }

    public void setPosition(double x, double y) {
        mPastPosition.set(x, y);
        mCurrentPosition.set(x, y);
        mFuturePosition.set(x, y);
    }

    @Override
    public void finish() {
        mPastPosition.set(mCurrentPosition);
        mCurrentPosition.set(mFuturePosition);
    }

    @Override
    public void render(GraphicsContext gfx) {
        double r = mSize / 2;
        double x = mCurrentPosition.mX - r;
        double y = mCurrentPosition.mY - r;
        gfx.setFill(mColor);
        gfx.fillOval(x, y, mSize, mSize);
    }

    protected void boundCheck(double damp) {
        if (mFuturePosition.mX < 0) {
            mCurrentPosition.set(mFuturePosition.mX, mCurrentPosition.mY);
            mFuturePosition.mX = 0 + (-mFuturePosition.mX) * damp;
        }
        else if (mFuturePosition.mX >= mWorld.getWidth()) {
            mCurrentPosition.set(mFuturePosition.mX, mCurrentPosition.mY);
            mFuturePosition.mX = mWorld.getWidth() - (mFuturePosition.mX - mWorld.getWidth()) * damp;
        }
        if (mFuturePosition.mY < 0) {
            mCurrentPosition.set(mCurrentPosition.mX, mFuturePosition.mY);
            mFuturePosition.mY = 0 + (-mFuturePosition.mY) * damp;
        }
        else if (mFuturePosition.mY >= mWorld.getHeight()) {
            mCurrentPosition.set(mCurrentPosition.mX, mFuturePosition.mY);
            mFuturePosition.mY = mWorld.getHeight() - (mFuturePosition.mY - mWorld.getHeight()) * damp;
        }
    }
}
