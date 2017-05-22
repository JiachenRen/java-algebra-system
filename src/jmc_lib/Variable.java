package jmc_lib;

/**
 * Created by Jiachen on 16/05/2017.
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
        this.val = x;
        return getVal();
    }

    public String toString() {
        return name;
    }

    public Variable replicate() {
        return new Variable(name);
    }
}
