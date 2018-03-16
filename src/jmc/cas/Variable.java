package jmc.cas;

/**
 * Created by Jiachen on 16/05/2017.
 * Variable class
 */
public class Variable extends LeafNode implements Nameable {
    private String name;

    public Variable(String name) {
        if (name.equals("")) throw new JMCException("variable name cannot be empty");
        this.name = name;
    }

    public double eval(double x) {
        return x;
    }

    public String toString() {
        return name;
    }

    public boolean isUndefined() {
        return false;
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

    public boolean equals(Operable other) {
        return other instanceof Variable && ((Variable) other).getName().equals(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Ensures that {other} does not get modified.
     *
     * @param other the replacing variable
     * @return new instance of the replacing variable
     */
    public Operable plugIn(Variable var, Operable other) {
        return other;
    }

    public Operable simplify() {
        return this;
    }

    /**
     * Since a variable is not an arbitrary number, val() should return NaN.
     *
     * @return NaN
     */
    public double val() {
        return Double.NaN;
    }

}
