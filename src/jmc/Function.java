package jmc;

import jmc.cas.*;
import jmc.graph.Plot;
import jmc.graph.Point;
import jmc.graph.Range;

import java.util.ArrayList;

/**
 * Function class acts as a bridge links Expression with UI in JGrapher
 */
public abstract class Function implements Evaluable {
    private String name;
    private boolean isVisible;
    private boolean asymptoteVisible;
    private boolean tangentLineVisible;
    private boolean tracingEnabled;
    private Plot plot;
    private Style graphStyle;
    private boolean dynamic;
    private float strokeWeight;
    private int color;
    private boolean matchAuxiliaryLinesColor;
    private boolean autoAsymptoteExtension;

    public enum Style {
        CONTINUOUS, DISCRETE
    }

    {
        isVisible = true;
        graphStyle = Style.CONTINUOUS;
        strokeWeight = 1.5f;
        color = -1;
        setMatchAuxiliaryLinesColor(false);
        setTracingEnabled(true);
        setAutoAsymptoteExtension(true);
    }

    /**
     * Default anonymous constructor
     */
    public Function() {
        this("");
    }

    public Function(String name) {
        this(name, false);
    }

    public Function(String name, boolean dynamic) {
        setName(name);
        setDynamic(dynamic);
    }

    public static Function implement(String name, Evaluable evaluable) {
        return implement(evaluable).setName(name);
    }

    public static Function implement(Evaluable evaluable) {
        return new Function() {
            @Override
            public double eval(double val) {
                return evaluable.eval(val);
            }
        };
    }

    /**
     * This is an abstract method to be defined by the anonymous subclass of Function; the definition
     * of the subclass fro this method should properly define how the function is going to be
     * evaluated.
     *
     * @param val the value that is going to be plugged into this Function for evaluation
     * @return the result gained from the evaluation with val and this Function instance's definition.
     */
    public abstract double eval(double val);

    /**
     * Calculates and stores a plot according to a specific Range
     *
     * @param rangeX the range in which a plot is going to be generated
     */
    public void updatePlot(Range rangeX, Range rangeY, float graphHeight) {
        plot = createPlot(rangeX, rangeY, graphHeight);
    }

    /**
     * TODO automatic graphic plotting density enhancement
     *
     * @param rangeX      the domain in which the graph is going to get graphed.
     * @param rangeY      the range in which the graph is going to get graphed.
     * @param graphHeight the height of the graph to be plotted.
     * @return the plotted graph plot according to the range.
     * @since May 7th, debugged updateStepVal().
     */
    public Plot createPlot(Range rangeX, Range rangeY, float graphHeight) {
        Plot plot = new Plot(rangeX);
        Range copied = new Range(rangeX);
        Point prevPoint = null;
        while (copied.hasNextStep()) {
            double temp = copied.getCurStep();
            Point curPoint = new Point(temp, this.eval(temp));
            if (prevPoint != null && (rangeY.isInScope(curPoint.getY()) ^ rangeY.isInScope(prevPoint.getY()))) {
                double diff_y = Math.abs(curPoint.getY() - prevPoint.getY());
                double pixels_y = Plot.map(Math.abs(diff_y), 0, rangeY.getSpan(), 0, graphHeight);
                if (pixels_y > 5 && graphStyle.equals(Style.CONTINUOUS)) {
                    double step = rangeX.getStep() / Math.abs(pixels_y);
                    Range newRangeX = new Range(prevPoint.getX() + step, curPoint.getX() - step, step);
                    while (newRangeX.hasNextStep()) {
                        double val = newRangeX.getCurStep();
                        double evaluated = eval(val);
                        Point newPoint = new Point(val, evaluated);
                        plot.add(newPoint);// TODO May 8th
                        newRangeX.next();
                    }
                }
            }
            prevPoint = curPoint;
            plot.add(curPoint);
            copied.next();
        }
        plot.sort(); //a computational expensive solution to the bug, yet it worked! TODO improve it
        plot.insertVerticalAsymptote(rangeY, this);
        return plot;
    }

    /**
     * Returns the calculated plot of a specific range according to the definition of this Function.
     */
    public Plot getPlot() {
        return plot;
    }


    public String getName() {
        return name;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Style getStyle() {
        return graphStyle;
    }

    public Function setGraphStyle(Style style) {
        this.graphStyle = style;
        return this;
    }

    public Function setName(String name) {
        this.name = name;
        return this;
    }

    public Function setDynamic(boolean temp) {
        this.dynamic = temp;
        return this;
    }

    public boolean isDynamic() {
        return dynamic;
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

    public float getStrokeWeight() {
        return strokeWeight;
    }

    public Function setStrokeWeight(float strokeWeight) {
        this.strokeWeight = strokeWeight;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Function setColor(int color) {
        this.color = color;
        return this;
    }

    public Function inheritStyle(Function other) {
        this.strokeWeight = other.strokeWeight;
        this.color = other.color;
        this.asymptoteVisible = other.asymptoteVisible;
        this.tangentLineVisible = other.tangentLineVisible;
        this.autoAsymptoteExtension = other.autoAsymptoteExtension;
        this.setGraphStyle(other.getStyle());
        this.setDynamic(other.dynamic);
        return this;
    }

    public boolean isAsymptoteVisible() {
        return asymptoteVisible;
    }

    public Function setAsymptoteVisible(boolean asymptoteVisible) {
        this.asymptoteVisible = asymptoteVisible;
        return this;
    }

    public boolean isTangentLineVisible() {
        return tangentLineVisible;
    }

    public void setTangentLineVisible(boolean tangentLineVisible) {
        this.tangentLineVisible = tangentLineVisible;
    }

    /**
     * TODO debug, java doc, not yet functional!
     *
     * @param x
     * @param y
     * @param allowed_diff
     * @param attempts
     * @return
     */
    public boolean containsPoint(double x, double y, double allowed_diff, int attempts) {
        double step = allowed_diff / attempts;
        Point org_point = new Point(x, y);
        for (double i = x - allowed_diff; i <= x + allowed_diff; i += step) {
            Point cur_point = new Point(i, this.eval(i));
            if (Point.dist(org_point, cur_point) <= allowed_diff)
                return true;
        }
        for (double i = y - allowed_diff; i <= y + allowed_diff; y += step) {
            ArrayList<Double> extrapolated = this.numericalSolve(i, x - allowed_diff, x + allowed_diff, allowed_diff, attempts);
            if (extrapolated.size() == 0) continue;
            Point cur_point = new Point(extrapolated.get(0), i);
            if (Point.dist(org_point, cur_point) <= allowed_diff)
                return true;
        }
        return false;
    }

    public boolean isAutoAsymptoteExtension() {
        return autoAsymptoteExtension;
    }

    public void setAutoAsymptoteExtension(boolean autoAsymptoteExtension) {
        this.autoAsymptoteExtension = autoAsymptoteExtension;
    }

    public boolean tracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public boolean isMatchAuxiliaryLinesColor() {
        return matchAuxiliaryLinesColor;
    }

    public void setMatchAuxiliaryLinesColor(boolean matchAuxiliaryLinesColor) {
        this.matchAuxiliaryLinesColor = matchAuxiliaryLinesColor;
    }

    public boolean equals(Function other) {
        return this.getName().equals(other.getName());
    }
}
