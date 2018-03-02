package jmc.cas;

import jmc.utils.ColorFormatter;

import java.util.ArrayList;

/**
 * Created by Jiachen on 16/05/2017.
 */
public class Constants {
    public interface ComputedConst {
        double compute();
    }

    private static ArrayList<Constant> constants;

    static {
        constants = new ArrayList<>();
        define("e", () -> Math.E);
        define("pi", () -> Math.PI);
        define("rand", Math::random);
    }

    public static boolean contains(String symbol) {
        for (Constant constant : constants) {
            if (constant.name.equals(symbol))
                return true;
        }
        return false;
    }


    public static boolean startsWidthConstant(String exp) {
        for (Constant constant : constants) {
            if (exp.startsWith(constant.name))
                return true;
        }
        return false;
    }

    /**
     * add or define a Constant object into the static ArrayList Constants.
     * TODO debug
     *
     * @param name          the name of the constant
     * @param computedConst the new computed const instance
     */
    public static void define(String name, ComputedConst computedConst) {
        boolean defined = false;
        for (Constant constant : constants) {
            if (constant.name.equals(name)) {
                constant.computedConst = computedConst;
                defined = true;
            }
        }
        if (!defined) constants.add(new Constant(name, computedConst));
    }

    public static double valueOf(String constant) {
        for (Constant c : constants) {
            if (c.name.equals(constant))
                return c.computedConst.compute();
        }
        return 0.0;
    }

    private static class Constant implements Operable {
        private ComputedConst computedConst;
        private String name;

        Constant(String name, ComputedConst computedConst) {
            this.computedConst = computedConst;
            this.name = name;
        }

        Constant(Constant other) {
            this.computedConst = other.computedConst;
            this.name = other.name;
        }

        public String toString() {
            return name;
        }

        public double eval(double x) {
            return computedConst.compute();
        }

        public Constant replicate() {
            return new Constant(name, computedConst);
        }

        public boolean equals(Operable other) {
            return other instanceof Constant && ((Constant) other).name.equals(this.name);
        }

        public Operable plugIn(Operable nested) {
            return new Constant(this);
        }
    }

    /**
     * prints out a list of all defined constants
     */
    public static void list() {
        int count = 0;
        for (Constant constant : constants) {
            System.out.println(ColorFormatter.coloredLine("[36;1m", "<" + count + ">\t" + constant.toString() + "\t-> " + constant.computedConst.compute(), "->", "<", ">"));
            count++;
        }
    }

    public static Constant getConstant(String name) {
        for (Constant constant : constants) {
            if (constant.name.equals(name))
                return constant;
        }
        return null;
    }
}
