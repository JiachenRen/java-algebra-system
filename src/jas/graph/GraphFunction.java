package jas.graph;

import jas.Function;
import jas.core.Node;
import jas.core.components.Variable;

import java.util.ArrayList;

/**
 * GraphFunction class acts as a bridge links Compiler with UI in JGrapher
 */
public class GraphFunction extends Function {
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
    private Variable independentVar = new Variable("x");
    private ArrayList<SuppliedVar> suppliedVars;

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
    public GraphFunction(Node node) {
        this("", node);
    }

    public GraphFunction(String name, Node node) {
        this(name, false, node);
    }

    public GraphFunction(String name, boolean dynamic, Node node) {
        super(name, node);
        suppliedVars = new ArrayList<>();
        initSuppliedVars();
        setDynamic(dynamic);
    }

    public void initSuppliedVars() {
        if (getNode().isMultiVar()) {
            ArrayList<Variable> vars = getNode().extractVariables();
            for (Variable v : vars) {
                if (!v.equals(independentVar)) {
                    SuppliedVar sv = new SuppliedVar(v.getName());
                    suppliedVars.add(sv);
                    setNode(getNode().replace(v, sv)); //god this bug took me forever to find
                }
            }
        }
    }

    public Node getNode() {
        return (Node) getEvaluable();
    }

    public void setNode(Node o) {
        setEvaluable(o);
    }

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

    @Override
    public double eval(double val) { // this might compromise speed
        return getNode().eval(val);
    }

    @Override
    public GraphFunction setName(String name) {
        super.setName(name);
        return this;
    }

    /**
     * Returns the calculated plot of a specific range according to the definition of this GraphFunction.
     */
    public Plot getPlot() {
        return plot;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public GraphFunction setDynamic(boolean temp) {
        this.dynamic = temp;
        return this;
    }

    public boolean isMultiVar() {
        return this.suppliedVars.size() > 0;
    }

    public float getStrokeWeight() {
        return strokeWeight;
    }

    public GraphFunction setStrokeWeight(float strokeWeight) {
        this.strokeWeight = strokeWeight;
        return this;
    }

    public int getColor() {
        return color;
    }

    public GraphFunction setColor(int color) {
        this.color = color;
        return this;
    }

    public GraphFunction inheritStyle(GraphFunction other) {
        this.strokeWeight = other.strokeWeight;
        this.color = other.color;
        this.asymptoteVisible = other.asymptoteVisible;
        this.tangentLineVisible = other.tangentLineVisible;
        this.autoAsymptoteExtension = other.autoAsymptoteExtension;
        this.setGraphStyle(other.getStyle());
        this.setDynamic(other.dynamic);
        return this;
    }

    public GraphFunction setGraphStyle(Style style) {
        this.graphStyle = style;
        return this;
    }

    public Style getStyle() {
        return graphStyle;
    }

    public boolean isAsymptoteVisible() {
        return asymptoteVisible;
    }

    public GraphFunction setAsymptoteVisible(boolean asymptoteVisible) {
        this.asymptoteVisible = asymptoteVisible;
        return this;
    }

    public boolean isTangentLineVisible() {
        return tangentLineVisible;
    }

    public void setTangentLineVisible(boolean tangentLineVisible) {
        this.tangentLineVisible = tangentLineVisible;
    }

    //    /**
//     * TODO debug, java doc, not yet functional!
//     *
//     * @param x
//     * @param y
//     * @param allowed_diff
//     * @param attempts
//     * @return
//     */
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

    public ArrayList<SuppliedVar> getSuppliedVars() {
        return suppliedVars;
    }

    public boolean isDiscrete() {
        return this.graphStyle.equals(Style.DISCRETE);
    }

    public enum Style {
        CONTINUOUS, DISCRETE
    }


}
