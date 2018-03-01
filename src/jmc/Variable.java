package jmc;

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
        return /*(char) 27 + "[35;1m" + */name/* + (char) 27 + "[0m"*/;
    }

    public Variable replicate() {
        return new Variable(name);
    }

    public boolean equals(Operable other) {
        return other instanceof Variable && ((Variable) other).getName().equals(this.name);
    }

    public Operable plugIn(Operable other) {
        return other;
    }
}
