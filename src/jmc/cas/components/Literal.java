package jmc.cas.components;

import jmc.cas.Operable;

/**
 * Created by Jiachen on 3/23/18.
 * Literal: non-calculable nodes
 */
public class Literal extends Variable {
    private String content;

    public Literal(String content) {
        super(content);
        this.content = content;
    }

    @Override
    public boolean equals(Operable other) {
        return other instanceof Literal && ((Literal) other).content.equals(content);
    }

    @Override
    public Literal copy() {
        return new Literal(content);
    }

    @Override
    public String toString() {
        return "'" + content + "'";
    }

    /**
     * If this method is successfully implemented, it would be marked as a milestone.
     *
     * @param v the variable in which the first derivative is taken with respect to.
     * @return first derivative of the expression
     */
    @Override
    public Operable firstDerivative(Variable v) {
        return RawValue.ZERO;
    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public double eval(double x) {
        return Double.NaN;
    }

    public String get() {
        return content;
    }

    @Deprecated
    public String getName() {
        return content;
    }
}
