package jmc.cas;

import jmc.graph.Graph;

/**
 * Created by Jiachen on 03/05/2017.
 * Wrapper class for a number
 */
public class RawValue implements Operable {
    private Number number;

    public RawValue(Number number) {
        this.number = number;
    }

    public RawValue(RawValue rawValue) {
        this(rawValue.number);
    }

    public double eval(double x) {
        return number.doubleValue();
    }

    /**
     * Removes the extra ".0" at the end of the number
     *
     * @return the formatted String representation of the number.
     */
    public String toString() {
        double extracted = number.doubleValue();
        String formatted = Graph.formatForDisplay(extracted);
        if (formatted.endsWith(".0")) formatted = formatted.substring(0, formatted.length() - 2);
        return extracted >= 0 ? formatted : "(" + formatted + ")";
    }

    public RawValue replicate() {
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

    public RawValue inverse() {
        if (isInteger()) return new Fraction(1, intValue());
        else return Fraction.convertToFraction(doubleValue(), Fraction.TOLERANCE).inverse();
    }

    public boolean equals(Operable other) {
        return other instanceof RawValue && ((RawValue) other).doubleValue() == this.doubleValue();
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
}
