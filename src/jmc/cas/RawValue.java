package jmc.cas;

import jmc.graph.Graph;

/**
 * Created by Jiachen on 03/05/2017.
 * Wrapper class for a number
 */
public class RawValue implements Operable, LeafNode {
    public static RawValue UNDEF = new RawValue(Double.NaN);
    private Number number;

    public RawValue(Number number) {
        this.number = number;
    }

    public RawValue(RawValue rawValue) {
        this(rawValue.number);
    }

    public double eval(double x) {
        return doubleValue();
    }
    public double val() {return doubleValue();}

    /**
     * Removes the extra ".0" at the end of the number
     *
     * @return the formatted String representation of the number.
     */
    public String toString() {
        if (isUndefined()) {
            return "undef";
        } else if (isInteger()) {
            String s = Integer.toString(intValue());
            if (s.length() <= 6) return s;
        } else {
            String s = Double.toString(doubleValue());
            if (s.length() <= 6) return s;
        }
        double extracted = number.doubleValue();
        String formatted = Graph.formatForDisplay(extracted);
        return extracted >= 0 ? formatted : "(" + formatted + ")";
    }

    public Operable simplify() {
        return this;
    }

    public RawValue clone() {
        return new RawValue(number);
    }

    public RawValue negate() {
        this.number = this.doubleValue() * -1;
        return this;
    }

    public double doubleValue() {
        return number.doubleValue();
    }

    public int intValue() {
        return number.intValue();
    }

    public boolean isInteger() {
        String s = number.toString();
        return s.endsWith(".0") || !s.contains(".");
    }

    public boolean isUndefined() {
        return new Double(number.doubleValue()).isNaN();
    }

    public RawValue inverse() {
        if (isInteger()) return new Fraction(1, intValue());
        else return Fraction.convertToFraction(doubleValue(), Fraction.TOLERANCE).inverse();
    }

    public boolean equals(Operable other) {
        return other instanceof RawValue
                && ((RawValue) other).isUndefined()
                && this.isUndefined()
                || other instanceof RawValue
                && ((RawValue) other).doubleValue()
                == this.doubleValue();
    }

    public boolean isZero() {
        return doubleValue() == 0;
    }

    public boolean isPositive() {
        return doubleValue() > 0;
    }

    /**
     * Since plugIn only applies to variable, a RawValue type should only return itself.
     *
     * @param nested the operable to be plugged in
     * @return new instance of self
     */
    public Operable plugIn(Variable var, Operable nested) {
        return this;
    }

    public int numNodes() {
        return 1;
    }
}
