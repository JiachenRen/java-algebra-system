package jmc.cas.components;

import jmc.cas.LeafNode;
import jmc.cas.Mode;
import jmc.cas.Operable;
import jmc.cas.operations.BinaryOperation;

import java.text.DecimalFormat;

/**
 * Created by Jiachen on 03/05/2017.
 * Wrapper class for a number
 * This class should NEVER modify a instance after it is created. Be sure to make new ones for every single operation.
 */
public class RawValue extends LeafNode {
    public static RawValue UNDEF = new RawValue(Double.NaN);
    public static RawValue ONE = new RawValue(1);
    public static RawValue ZERO = new RawValue(0);
    public static RawValue TWO = new RawValue(2);
    public static RawValue INFINITY = new RawValue(Double.POSITIVE_INFINITY);

    private Double n;

    public RawValue(RawValue rawValue) {
        this(rawValue.n);
    }

    public RawValue(double n) {
        this.n = n;
    }

    public static boolean isInteger(double n) {
        return new RawValue(n).isInteger();
    }

    public boolean isInteger() {
        return (n % 1) == 0;
    }

    public double doubleValue() {
        return n;
    }

    public double eval(double x) {
        return doubleValue();
    }

    public RawValue inverse() {
        if (isInteger()) return new Fraction(1, longValue());
        else return Fraction.convertToFraction(doubleValue(), Fraction.TOLERANCE).inverse();
    }

    public boolean isZero() {
        return doubleValue() == 0;
    }

    public boolean isInfinite() {
        return n.isInfinite();
    }

    /**
     * Removes the extra ".0" at the end of the n
     *
     * @return the formatted String representation of the n.
     */
    public String toString() {
        if (isUndefined()) {
            return "undef";
        }
        return format(doubleValue());
    }

    private String format(double d) {
        DecimalFormat decimalFormat = new DecimalFormat(Mode.DECIMAL_FORMAT);
        return decimalFormat.format(d);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPositive() {
        return doubleValue() > 0;
    }


    public long longValue() {
        return n.longValue();
    }


    public boolean isUndefined() {
        return n.isNaN();
    }


    public RawValue copy() {
        return new RawValue(n);
    }

    public Operable explicitNegativeForm() {
        if (this.equals(RawValue.ONE.negate())) return this;
        return doubleValue() < 0 ? new BinaryOperation(RawValue.ONE.negate(), "*", this.copy().negate()) : this.copy();
    }

    @Override
    public Operable firstDerivative(Variable v) {
        if (isUndefined()) return RawValue.UNDEF;
        return RawValue.ZERO;
    }

    public boolean equals(Operable other) {
        return other instanceof RawValue
                && other.isUndefined()
                && this.isUndefined()
                || other instanceof RawValue
                && ((RawValue) other).doubleValue()
                == this.doubleValue();
    }

    public Operable simplify() {
        return this;
    }

    public double val() {
        return doubleValue();
    }

    public int complexity() {
        return 1;
    }


    @Override
    public RawValue negate() {
        return new RawValue(this.doubleValue() * -1);
    }


}
