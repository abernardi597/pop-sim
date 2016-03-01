package example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.popsim.src.simu.Entity;
import net.popsim.src.simu.World;
import net.popsim.src.util.Vector;

public abstract class PositionEntity extends Entity {

    public final Vector mPastPosition, mCurrentPosition, mFuturePosition;
    public double mSize;
    public Color mColor;

    public PositionEntity(World world, double r, Color color) {
        super(world);
        mPastPosition = new Vector();
        mCurrentPosition = new Vector();
        mFuturePosition = new Vector();
        mSize = r;
        mColor = color;
    }

    public PositionEntity setPosition(double x, double y) {
        mPastPosition.set(x, y);
        mCurrentPosition.set(x, y);
        mFuturePosition.set(x, y);
        return this;
    }

    @Override
    public void finish() {
        mPastPosition.set(mCurrentPosition);
        mCurrentPosition.set(mFuturePosition);
    }

    @Override
    public void render(GraphicsContext gfx) {
        double d = mSize * 2;
        double x = mCurrentPosition.mX - mSize;
        double y = mCurrentPosition.mY - mSize;
        gfx.setFill(mColor);
        gfx.fillOval(x, y, d, d);
    }

    protected void boundCheck(double damp) {
        boundCheck(mSize, mSize, mWorld.getWidth() - mSize, mWorld.getHeight() - mSize, damp);
    }

    protected void boundCheck(double x0, double y0, double x1, double y1, double damp) {
        if (mFuturePosition.mX < x0) {
            mCurrentPosition.mX = fix(mCurrentPosition.mX, x0, damp);
            mFuturePosition.mX = fix(mFuturePosition.mX, x0, damp);
        }
        else if (mFuturePosition.mX >= x1) {
            mCurrentPosition.mX = fix(mCurrentPosition.mX, x1, damp);
            mFuturePosition.mX = fix(mFuturePosition.mX, x1, damp);
        }
        if (mFuturePosition.mY < y0) {
            mCurrentPosition.mY = fix(mCurrentPosition.mY, y0, damp);
            mFuturePosition.mY = fix(mFuturePosition.mY, y0, damp);
        }
        else if (mFuturePosition.mY >= y1) {
            mCurrentPosition.mY = fix(mCurrentPosition.mY, y1, damp);
            mFuturePosition.mY = fix(mFuturePosition.mY, y1, damp);
        }
    }

    protected double fix(double d, double r, double damp) {
        return r - (d - r) * damp;
    }
}
