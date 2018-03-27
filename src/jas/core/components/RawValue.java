package jas.core.components;

import jas.core.LeafNode;
import jas.core.Mode;
import jas.core.Node;
import jas.core.operations.Binary;
import jas.utils.ColorFormatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Created by Jiachen on 03/05/2017.
 * Wrapper class for a number
 * This class should NEVER modify a instance after it is created. Be sure to make new ones for every single operation.
 */
public class RawValue extends LeafNode {
    public static final RawValue UNDEF = new RawValue(Double.NaN);
    public static final RawValue ONE = new RawValue(1);
    public static final RawValue ZERO = new RawValue(0);
    public static final RawValue TWO = new RawValue(2);
    public static final RawValue INFINITY = new RawValue(Double.POSITIVE_INFINITY);

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

    public static boolean isNumeric(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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
        if (isInteger()) return new Fraction(BigInteger.ONE, toBigInteger());
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
        } else if (doubleValue() == 0) return "0"; // fix a bug where 0.negate().toString() returns "-0"
        return format(doubleValue());
    }

    public BigInteger toBigInteger() {
        return BigDecimal.valueOf(doubleValue()).toBigIntegerExact();
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

    public Node explicitNegativeForm() {
        if (this.equals(ONE.negate())) return this;
        return doubleValue() < 0 ? new Binary(ONE.negate(), "*", this.copy().negate()) : this.copy();
    }

    @Override
    public Node firstDerivative(Variable v) {
        if (isUndefined()) return UNDEF;
        return ZERO;
    }

    /**
     * @return string representation of the node coded with Ansi color codes.
     */
    @Override
    public String coloredString() {
        return ColorFormatter.color(this.toString(), Mode.NUMBER_COLOR);
    }

    public boolean equals(Node other) {
        return other instanceof RawValue
                && other.isUndefined()
                && this.isUndefined()
                || other instanceof RawValue
                && ((RawValue) other).doubleValue()
                == this.doubleValue();
    }

    public Node simplify() {
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
