package jmc.cas;

/**
 * Created by Jiachen on 16/05/2017.
 * Variable class
 */
public class Variable implements Operable {
    private String name;
    private double val;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public double eval(double x) {
        this.setVal(x);
        return getVal();
    }

    public String toString() {
        return name;
    }

    /**
     * Ensures that if "x" is defined as "x = 3", the value gets replicated as well.
     *
     * @return new Variable instance that is identical to self.
     */
    public Variable clone() {
        Variable newInstance = new Variable(name);
        newInstance.setVal(this.val);
        return newInstance;
    }

    public boolean equals(Operable other) {
        return other instanceof Variable && ((Variable) other).getName().equals(this.name);
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
}
