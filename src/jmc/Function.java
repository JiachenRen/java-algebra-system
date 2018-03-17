package jmc;

import jmc.cas.Evaluable;
import jmc.cas.Nameable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/2/18.
 * Function class
 */
public class Function implements Evaluable, Nameable {

    private String name;
    private Evaluable evaluable;

    public Function(Evaluable evaluable) {
        this("", evaluable);
    }

    public Function(String name, Evaluable evaluable) {
        setName(name);
        this.evaluable = evaluable;
    }

    /**
     * This is an abstract method to be defined by the anonymous subclass of GraphFunction; the definition
     * of the subclass fro this method should properly define how the function is going to be
     * evaluated.
     *
     * @param val the value that is going to be plugged into this GraphFunction for evaluation
     * @return the result gained from the evaluation with val and this GraphFunction instance's definition.
     */
    public double eval(double val) {
        return evaluable.eval(val);
    }

    public boolean equals(Function other) {
        return this.getName().equals(other.getName());
    }

    public String getName() {
        return name;
    }

    public Function setName(String name) {
        this.name = name;
        return this;
    }

    public ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy) {
        return numericalSolve(y, lowerBound, upperBound, accuracy, 1000);
    }

    protected ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy, int steps) {
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

    public Evaluable getEvaluable() {
        return evaluable;
    }

    public Function setEvaluable(Evaluable evaluable) {
        this.evaluable = evaluable;
        return this;
    }
}
