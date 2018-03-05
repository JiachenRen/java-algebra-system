package jmc.graph;

import jmc.cas.Mode;
import jmc.cas.Operable;
import jmc.cas.Variable;

/**
 * Created by Jiachen on 3/4/18.
 * Supplied Variable
 */
public class SuppliedVar extends Variable {
    private double val;

    public SuppliedVar(String name) {
        super(name);
    }

    public SuppliedVar setVal(double val) {
        this.val = val;
        return this;
    }

    @Override
    public double eval(double x) {
        return val;
    }

    @Override
    public double val() {
        return val;
    }

    @Override
    public SuppliedVar clone() {
        super.clone();
        return new SuppliedVar(this.getName()).setVal(this.val);
    }

    @Override
    public Operable replace(Operable o, Operable r) {
        return o.equals(this) ? r : this;
    }

    @Override
    public boolean equals(Operable other) {
        return other instanceof SuppliedVar && ((SuppliedVar) other).getName().equals(getName());
    }

    public String toString() {
        return Mode.DEBUG ? "&" + getName() + "&" : getName();
    }
}
