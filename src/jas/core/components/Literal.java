package jas.core.components;

import jas.core.Mode;
import jas.core.Node;
import jas.utils.ColorFormatter;

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
    public boolean equals(Node other) {
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
    public Node firstDerivative(Variable v) {
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

    @Override
    public String coloredString() {
        return ColorFormatter.color(this.toString(), Mode.LITERAL_COLOR);
    }

    public String get() {
        return content;
    }

    @Override
    public double val() {
        return Double.NaN;
    }

    @Override
    public Literal simplify() {
        return this;
    }

    @Deprecated
    public String getName() {
        return content;
    }
}
