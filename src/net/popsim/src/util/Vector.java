package net.popsim.src.util;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A 2D Vector with double precision.
 */
public class Vector implements Cloneable {

    public static final String X = "X";
    public static final String Y = "Y";

    /**
     * Constructs a Vector using polar coordinates.
     *
     * @param theta the angle (in radians)
     * @param mag   the magnitude
     *
     * @return The Vector Equivalent to the polar coordinate representation.
     */
    public static Vector fromPolar(double theta, double mag) {
        return new Vector((float) (mag * Math.cos(theta)), (float) (mag * Math.sin(theta)));
    }

    /**
     * X component of the vector.
     */
    @Expose
    @SerializedName(X)
    public double mX;
    /**
     * Y component of the vector.
     */
    @Expose
    @SerializedName(Y)
    public double mY;

    /**
     * Constructs a new Vector with the given components.
     *
     * @param x x component
     * @param y y component
     */
    public Vector(double x, double y) {
        set(x, y);
    }

    /**
     * Constructs an empty vector (ie with components of zero).
     */
    public Vector() {
        this(0, 0);
    }

    /**
     * Sets the components of this Vector
     *
     * @param x x component
     * @param y y component
     *
     * @return This, for convenience.
     */
    public Vector set(double x, double y) {
        mX = x;
        mY = y;
        return this;
    }

    /**
     * Adds this Vector and another Vector, storing the result in a third Vector.
     *
     * @param other  the other Vector to add
     * @param result the Vector to store the result in
     *
     * @return The resulting vector.
     */
    public Vector add(Vector other, Vector result) {
        return result.set(this.mX + other.mX, this.mY + other.mY);
    }

    /**
     * Subtracts another Vector from this Vector and stores the result in a third vector.
     *
     * @param other  the other Vector to subtract
     * @param result the Vector to store the result in
     *
     * @return The resulting Vector.
     */
    public Vector subtract(Vector other, Vector result) {
        return result.set(this.mX - other.mX, this.mY - other.mY);
    }

    /**
     * Multiplies this vector by a value and stores the result in another Vector.
     *
     * @param scalar the factor to multiply by
     * @param result the Vector to store the result in
     *
     * @return The resulting Vector.
     */
    public Vector multiply(double scalar, Vector result) {
        return result.set(mX * scalar, mY * scalar);
    }

    /**
     * Normalizes this vector (ie, sets the magnitude to 1) by multiplying by the inverse of the magnitude.
     *
     * @param result the Vector to store the result in
     *
     * @return The resulting Vector.
     */
    public Vector normalize(Vector result) {
        double m = mag();
        if (m == 0)
            return result.set(0, 0);
        else return multiply(1 / mag(), result);
    }

    /**
     * @return The square magnitude of the vector, computed with the Pythagorean distance formula.
     */
    public double squareMag() {
        return dot(this);
    }

    /**
     * @return The magnitude of the vector.
     * @see #squareMag()
     */
    public double mag() {
        return Math.sqrt(squareMag());
    }

    /**
     * @param other another vector
     *
     * @return The dot-product of this vector and the given one.
     */
    public double dot(Vector other) {
        return mX * other.mX + mY * other.mY;
    }

    @Override
    public Vector clone() {
        return new Vector(mX, mY);
    }

    @Override
    public String toString() {
        return String.format("<%.2f, %.2f>", mX, mY);
    }
}
