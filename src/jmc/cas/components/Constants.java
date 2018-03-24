package jmc.cas.components;

import jmc.cas.JMCException;
import jmc.cas.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 16/05/2017.
 * Constants
 */
public class Constants {
    static {
        constants = new ArrayList<>();
        define("e", () -> Math.E);
        define("pi", () -> Math.PI);
        define("∞", () -> Double.POSITIVE_INFINITY);
        define("random", Math::random);
    }

    private static ArrayList<Constant> constants;
    public static final Constant E = get("e");
    public static final Constant PI = get("pi");

    public static boolean contains(String symbol) {
        for (Constant constant : constants) {
            if (constant.getName().equals(symbol))
                return true;
        }
        return false;
    }

    /**
     * add or define a Constant object into the static ArrayList Constants.
     *
     * @param name          the name of the constant
     * @param computedConst the new computed const instance
     */
    public static void define(String name, ComputedConst computedConst) {
        boolean defined = false;
        for (Constant constant : constants) {
            if (constant.getName().equals(name)) {
                constant.computedConst = computedConst;
                defined = true;
            }
        }
        if (!defined) constants.add(new Constant(name, computedConst));
    }

    public static double valueOf(String constant) {
        for (Constant c : constants) {
            if (c.getName().equals(constant))
                return c.computedConst.compute();
        }
        return 0.0;
    }

    public static ArrayList<Constant> list() {
        return constants;
    }

    public static Constant get(String name) {
        for (Constant constant : constants) {
            if (constant.getName().equals(name))
                return constant;
        }
        throw new JMCException("constant + \"" + name + "\" does not exist");
    }

    public interface ComputedConst {
        double compute();
    }

    public static class Constant extends Variable {
        private ComputedConst computedConst;

        public Constant(String name, ComputedConst computedConst) {
            super(name);
            this.computedConst = computedConst;

        }

        Constant(Constant other) {
            super(other.getName());
            this.computedConst = other.computedConst;
        }

        public double eval(double x) {
            return computedConst.compute();
        }

        public boolean equals(Operable other) {
            return other instanceof Constant && (((Constant) other).getName().equals(getName())
                    || other.val() == this.val());
        }

        @Override
        public double val() {
            return computedConst.compute();
        }

        @Override
        public Constant copy() {
            return new Constant(this);
        }

        @Override
        public Operable firstDerivative(Variable v) {
            return RawValue.ZERO;
        }

        public int complexity() {
            return 2;
        }

        public int numNodes() {
            return 1;
        }
    }
}
