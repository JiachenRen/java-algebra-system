package jmc.cas.components;

import jmc.cas.*;
import jmc.utils.ColorFormatter;

/**
 * Created by Jiachen on 16/05/2017.
 * Variable class
 */
public class Variable extends LeafNode implements Nameable {
    private String name;
    private Operable stored;

    public Variable(String name) {
        if (name.equals("")) throw new JMCException("variable name cannot be empty");
        this.name = name;
    }

    public double eval(double x) {
        return x;
    }

    public boolean equals(Operable other) {
        return other instanceof Variable && ((Variable) other).getName().equals(this.name);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Since a variable is not an arbitrary number, val() should return NaN.
     * If a value is stored in the variable, return the value.
     *
     * @return NaN
     */
    public double val() {
        return stored == null ? Double.NaN : stored.val();
    }

    public boolean isUndefined() {
        return false;
    }

    public Operable simplify() {
        return this.stored == null ? this : stored;
    }

    /**
     * Assign the variable with an Operable
     *
     * @param o the Operable to be assigned to this variable.
     */
    public void store(Operable o) {
        this.stored = o;
    }

    public void del() {
        this.stored = null;
    }

    /**
     * @param v the variable in which the first derivative is taken with respect to.
     * @return if v == this, 1, otherwise 0; rule of derivative applies.
     */
    public Operable firstDerivative(Variable v) {
        if (v.equals(this)) return RawValue.ONE;
        return RawValue.ZERO;
    }

    /**
     * @return string representation of the operable coded with Ansi color codes.
     */
    @Override
    public String coloredString() {
        return ColorFormatter.color(this.toString(), Mode.VARIABLE_COLOR);
    }

    /**
     * Ensures that if "x" is defined as "x = 3", the value gets replicated as well.
     *
     * @return new Variable instance that is identical to self.
     */
    public Variable copy() {
        Variable v = new Variable(name);
        v.store(stored);
        return v;
    }

    public Operable explicitNegativeForm() {
        return this.copy();
    }

    public int complexity() {
        return 3;
    }

}
