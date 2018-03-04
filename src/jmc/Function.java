package jmc;

import jmc.cas.Evaluable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/2/18.
 * Function class
 */
public abstract class Function implements Evaluable {

    private String name;

    protected Function(String name) {
        setName(name);
    }

    public Function() {
        this("");
    }

    public static Function implement(Evaluable evaluable) {
        return new Function() {
            @Override
            public double eval(double val) {
                return evaluable.eval(val);
            }
        };
    }

    public static Function implement(String name, Evaluable evaluable) {
        return new Function(name) {
            @Override
            public double eval(double val) {
                return evaluable.eval(val);
            }
        };
    }

    /**
     * This is an abstract method to be defined by the anonymous subclass of GraphFunction; the definition
     * of the subclass fro this method should properly define how the function is going to be
     * evaluated.
     *
     * @param val the value that is going to be plugged into this GraphFunction for evaluation
     * @return the result gained from the evaluation with val and this GraphFunction instance's definition.
     */
    public abstract double eval(double val);

    public boolean equals(Function other) {
        return this.getName().equals(other.getName());
    }

    public ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy) {
        return numericalSolve(y, lowerBound, upperBound, accuracy, 1000);
    }

    public ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy, int steps) {
        if (Math.abs(upperBound - lowerBound) <= accuracy) {
            ArrayList<Double> results = new ArrayList<>();
            double solution = (lowerBound + upperBound) / 2;
            results.add(solution);
            return results;
        }
        ArrayList<Double> solutions = new ArrayList<>();
        double stepVal = (upperBound - lowerBound) / steps;
        boolean isAbove = this.eval(lowerBound) > y;
        for (double i = lowerBound + stepVal; i <= upperBound; i += stepVal) {
            double cur = this.eval(i);
            if (cur > y ^ isAbove) {
                isAbove = cur > y;
                solutions.addAll(numericalSolve(y, i - stepVal, i, accuracy, steps));
            }
        }
        return solutions;
    }

    public Function setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
