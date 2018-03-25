package jmc.cas.components;

import jmc.cas.*;
import jmc.utils.ColorFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Jiachen on 16/05/2017.
 * Variable class
 */
public class Variable extends LeafNode implements Nameable {
    private String name;
    private static Map<String, Operable> storedVars = new HashMap<>();

    public Variable(String name) {
        if (name.equals("")) throw new JMCException("variable name cannot be empty");
        this.name = name;
    }

    public double eval(double x) {
        return x;
    }

    public boolean equals(Operable other) {
        return other instanceof Variable && ((Variable) other).getName().equals(name);
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
     * @return NaN or value of the definition
     */
    public double val() {
        return get().orElse(RawValue.UNDEF).val();
    }

    private Optional<Operable> get() {
        return get(this.name);
    }

    public boolean isUndefined() {
        return false;
    }

    public Operable simplify() {
        return get().orElse(this);
    }

    /**
     * Assign the variable with an Operable
     *
     * @param var the name of the variable
     * @param o   the Operable to be assigned to the variable name .
     */
    public static void store(Operable o, String var) {
        storedVars.put(var, o);
    }

    /**
     * @param var variable name
     * @return Optional type of var's definition
     */
    public static Optional<Operable> get(String var) {
        return Optional.ofNullable(storedVars.get(var));
    }

    /**
     * deletes the variable from stored variables
     *
     * @param var the name of the variable
     * @return definition of var
     */
    public static Operable del(String var) {
        return storedVars.remove(var);
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
        return new Variable(name);
    }

    public Operable explicitNegativeForm() {
        return this.copy();
    }

    public int complexity() {
        return 3;
    }

}
