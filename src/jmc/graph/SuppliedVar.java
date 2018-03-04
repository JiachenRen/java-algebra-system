package jmc.graph;

import jmc.cas.Variable;

/**
 * Created by Jiachen on 3/4/18.
 * Supplied Variable
 */
public class SuppliedVar extends Variable {
    private double val;

    SuppliedVar(String name) {
        super(name);
    }

    void setVal(double val) {
        this.val = val;
    }

    @Override
    public double eval(double x) {
        return val;
    }

    @Override
    public double val() {
        return val;
    }
}
