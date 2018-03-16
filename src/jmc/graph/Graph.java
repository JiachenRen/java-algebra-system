package jmc.graph;

import jmc.cas.Compiler;
import jui.*;
import processing.core.PApplet;
import processing.core.PConstants;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Jiachen on 04/05/2017
 * May 21st: simple, powerful, impressive syntax:
 * this.addEventListener(Event.MOUSE_ENTERED, getParent()::noCursor);
 * this.addEventListener(Event.MOUSE_LEFT, getParent()::cursor);
 * <p>
 * TODO: Add polar & parametric graph.
 * TODO: add grids
 */
public class Graph extends Contextual {
    private Range rangeX;
    private Range rangeY;
    private double stepLength; //in pixels. Could be sub-pixel
    private double stepValueX;
    private double stepValueY;
    private float markLengthBig;
    private float axisMarkingTextSize;
    private float lineWidth = 1;
    private int maxMarkingLength;
    private int minMarkingLength;
    private ArrayList<GraphFunction> functions;
    private int asymptoteColor;
    private int tangentLineColor;
    private int tracingLineColor;
    private Mode mode;

    private float initMousePosX;
    private float initMousePosY;

    private boolean tracingOn;
    private boolean evaluationOn;
    private boolean axesVisible;

    {
        functions = new ArrayList<>();
        /*
        default window.
         */
        rangeX = new Range(-10, 10);
        rangeY = new Range(-10, 10);
        /*
        change the step length (in pixels) to adjust the accuracy of the graph.
        the lesser the step length, the more accurate the graph is going to be. Nevertheless,
        this value should remain at 1 under most circumstances as 1 is plenty for most graphing
        situations. For more complex the graph, the lower the step length should be. Nevertheless,
        one should be aware that as the step length comes down the the performance decrease
        accordingly. One would find it significantly slower.
        */
        stepLength = 0.1; //TODO changed from 1 to 0.1. May 17th. Might cause performance issues.
        markLengthBig = 2;
        axisMarkingTextSize = JNode.getParent().pixelDensity == 1 ? 10 : 5;
        setMaxMarkingLength(50);
        setMinMarkingLength(50);
        /*
        colors
         */
        asymptoteColor = JNode.getParent().color(255, 255, 255, 200);
        tangentLineColor = JNode.getParent().color(255, 255, 255, 200);
        tracingLineColor = JNode.getParent().color(255, 255, 255, 200);

        initEventListeners();
        setMode(Mode.DRAG);

        /*
        tracing defaults to false
         */
        setTracingOn(false);
        /*
        axes visible defaults to true
         */
        setAxesVisible(true);
    }

    private void initEventListeners() {
        this.addEventListener(new EventListener("DRAG", Event.MOUSE_DRAGGED).attachMethod(() -> {
            if (!isMouseOver()) return;
            float dmx = getParent().mouseX - getParent().pmouseX;
            float dmy = getParent().mouseY - getParent().pmouseY;
            float hs = Plot.map(Math.abs(dmx), 0, w, 0, rangeX.getHigh() - rangeX.getLow()) * (dmx > 0 ? -1 : 1);
            float vs = Plot.map(Math.abs(dmy), 0, h, 0, rangeY.getHigh() - rangeY.getLow()) * (dmy > 0 ? 1 : -1);
            double minX = rangeX.getLow() + hs;
            double maxX = rangeX.getHigh() + hs;
            double minY = rangeY.getLow() + vs;
            double maxY = rangeY.getHigh() + vs;
            this.setWindow(minX, maxX, minY, maxY);
        }).setDisabled(true));
        this.addEventListener(new EventListener("ZOOM_IN", Event.MOUSE_PRESSED).attachMethod(() -> {
            double converted[] = convertToPointOnGraph(getParent().mouseX, getParent().mouseY);
            zoom(0.5, converted[0], converted[1]);
        }).setDisabled(true));
        this.addEventListener(new EventListener("ZOOM_OUT", Event.MOUSE_PRESSED).attachMethod(() -> {
            double converted[] = convertToPointOnGraph(getParent().mouseX, getParent().mouseY);
            zoom(1.5, converted[0], converted[1]);
        }).setDisabled(true));

        this.addEventListener(new EventListener("ZOOM_RECT", Event.MOUSE_PRESSED).attachMethod(() -> {
            initMousePosX = getParent().mouseX;
            initMousePosY = getParent().mouseY;
        }).setDisabled(true));
        this.addEventListener(new EventListener("ZOOM_RECT", Event.MOUSE_RELEASED).attachMethod(() -> {
            if (Math.abs(initMousePosX - getParent().mouseX) < 1 || Math.abs(initMousePosY - getParent().mouseY) < 1)
                return;
            double converted[] = convertToPointOnGraph(initMousePosX, initMousePosY);
            double cur[] = convertToPointOnGraph(getParent().mouseX, getParent().mouseY);
            this.setWindow(converted[0] > cur[0] ? cur[0] : converted[0], converted[0] > cur[0] ? converted[0] : cur[0], converted[1] > cur[1] ? cur[1] : converted[1], converted[1] > cur[1] ? converted[1] : cur[1]);
        }).setDisabled(true));
        this.addEventListener(new EventListener("ZOOM_RECT", Event.MOUSE_HELD).attachMethod(() -> {
            getParent().fill(getTextColor(), 25);
            getParent().stroke(getTextColor());
            getParent().rect(initMousePosX, initMousePosY, getParent().mouseX - initMousePosX, getParent().mouseY - initMousePosY);
        }).setDisabled(true));
    }

    public Graph(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public Graph() {
        super();
        init();
    }

    //TODO debug constructor, it would not work!!!
    public Graph(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    private void init() {
        updateStepValue();
        if (!isRelative) {
            this.resize(w, h);
        }
    }

    public void display() {
        super.display();

        if (axesVisible)
            this.drawAxes();

        for (GraphFunction function : functions) {
            if (function.isDynamic()) {
                function.updatePlot(rangeX, rangeY, h);
                function.getPlot().updateCoordinates(rangeY, w, h);
            }
            if (function.isVisible())
                this.drawFunction(function);
        }

        if (tracingIsOn() && isMouseOver()) {
            double xPosOnGraph = this.convertToPointOnGraph(getParent().mouseX, getParent().mouseY)[0];
            TextInput xVal = JNode.getTextInputById("#XVAL");
            assert xVal != null;
            xVal.setIsFocusedOn(false);
            xVal.setContent("x = " + Double.toString(xPosOnGraph));
            this.trace(xPosOnGraph);
        }
    }

    private void drawAxesText(double val, float cx, float cy, boolean isYAxis) {
        String text = formatForDisplay(val);
        float textWidth = getParent().textWidth(text);
        if (isYAxis) {
            if (cx + markLengthBig * 2 + textWidth > w + x)
                getParent().text(text, cx - markLengthBig * 2 - textWidth / 2, cy);
            else getParent().text(text, cx + markLengthBig * 2 + textWidth / 2, cy);
        } else if (cx + textWidth / 2 < x + w && cx - textWidth / 2 > x) {
            float offset = markLengthBig * 2 + axisMarkingTextSize / 2;
            float base = cy + offset;
            getParent().text(text, cx, base > y + h ? cy - offset : base);
        }
    }

    private void drawAxisMarksY(float cx, float cy) {
        float right = cx + markLengthBig;
        float left = cx - markLengthBig;
        getParent().line(right > x + w ? x + w - 1 : right, cy, left < x + 1 ? x + 1 : left, cy);
    }


    private void drawAxisMarksX(float cx, float cy) {
        float up = cy - markLengthBig;
        float down = cy + markLengthBig;
        getParent().line(cx, up < y + 1 ? y + 1 : up, cx, down > y + h - 1 ? y + h - 1 : down);
    }


    /**
     * Draws the grids of the graph.
     */
    private void drawAxes() {
        getParent().stroke(getTextColor());
        getParent().fill(getTextColor());
        getParent().textSize(axisMarkingTextSize);
        getParent().strokeWeight(0.2f);
        {
            //drawing the y axis
            float cx = Plot.map(0, rangeX.getLow(), rangeX.getHigh(), x, x + w);
            cx = cx > x + w - 1 ? x + w - 1 : cx;
            cx = cx < x + 1 ? x + 1 : cx;
            getParent().line(cx, y, cx, y + h);

            float step = (float) (stepValueY / rangeY.getSpan() * h);
            float offset = Plot.map(0, rangeY.getLow(), rangeY.getHigh(), y + h, y);
            float upAnchor = offset - step;
            float downAnchor = offset + step;

            getParent().textAlign(PConstants.CENTER, PConstants.CENTER);
            double current = stepValueY;
            while (upAnchor > y || downAnchor < y + h) {
                if (upAnchor < y + h && upAnchor > y) {
                    drawAxesText(current, cx, upAnchor, true);
                    drawAxisMarksY(cx, upAnchor);
                }
                if (downAnchor > y && downAnchor < y + h) {
                    drawAxesText(-current, cx, downAnchor, true);
                    drawAxisMarksY(cx, downAnchor);
                }
                current += stepValueY;
                upAnchor -= step;
                downAnchor += step;
            }
        }

        //drawing the x axis
        float cy = y + h - Plot.map(0, rangeY.getLow(), rangeY.getHigh(), 0, h);
        cy = cy > y + h - 1 ? y + h - 1 : cy;
        cy = cy < y + 1 ? y + 1 : cy;
        getParent().line(x, cy, x + w, cy);
        float leftAnchor = Plot.map(0, rangeX.getLow(), rangeX.getHigh(), x, x + w);
        float rightAnchor = leftAnchor;
        float step = (float) (stepValueX / rangeX.getSpan() * w);

        double current = 0;
        while (leftAnchor > x) {
            if (leftAnchor < x + w) {
                drawAxisMarksX(leftAnchor, cy);
                drawAxesText(current, leftAnchor, cy, false);
            }
            current -= stepValueX;
            leftAnchor -= step;
        }

        current = 0;
        while (rightAnchor < x + w) {
            if (rightAnchor > x && current != 0) {
                drawAxisMarksX(rightAnchor, cy);
                drawAxesText(current, rightAnchor, cy, false);
            }
            current += stepValueX;
            rightAnchor += step;
        }
    }

    /**
     * updates the step number of markings and step length for the graph's grid.
     *
     * @since May 18th, alternative dividend scaling to keep a decimal scale.
     */
    private double calculateStepValue(Range range, int minNumMarkings, int maxNumMarkings) {
        double valRange = range.getSpan();
        double temp = 1.0d;
        int markings = (int) (valRange / temp);
        int flag = 0;
        while (markings < minNumMarkings) {
            temp /= flag % 3 == 1 ? 2.5d : 2.0d;
            markings = (int) (valRange / temp);
            flag++;
        }
        flag = 0;
        while (markings > maxNumMarkings) {
            temp *= flag % 3 == 1 ? 2.5d : 2.0d;
            markings = (int) (valRange / temp);
            flag++;
        }
        return temp;
    }

    /**
     * zooms in according to a scale given and a point that is the center-to-be
     *
     * @param scale   how many times larger than original. 1.5 would be zoom out while 0.5 would be zoom in.
     * @param centerX coordinate-x of the center on the graph
     * @param centerY coordinate-y of the center on the graph
     * @since May 21st
     */
    public void zoom(double scale, double centerX, double centerY) {
        rangeX.rescale(scale);
        rangeY.rescale(scale);
        this.pointAsCenter(centerX, centerY);
    }

    /**
     * puts the origin in the center of the graph
     */
    public void centerOrigin() {
        pointAsCenter(0, 0);
    }

    /**
     * centers the graph based on the designated point
     */
    public void pointAsCenter(double graphX, double graphY) {
        double gw = rangeX.getSpan() / 2;
        double gh = rangeY.getSpan() / 2;
        this.setWindow(graphX - gw, graphX + gw, graphY - gh, graphY + gh);
    }

    /**
     * equalized the axes of the graph so they look equal on the screen
     * note: the y axis is resized according to the x axis
     */
    public void equalizeAxes() {
        double xSpanOnScreen = w * stepValueX / rangeX.getSpan();
        double yWindowSpan = h / xSpanOnScreen * stepValueX;
        double currentSpan = rangeY.getSpan();
        double ratio = yWindowSpan / currentSpan;
        Range newRangeY = new Range(rangeY.getLow() * ratio, rangeY.getHigh() * ratio);
        setWindowY(newRangeY.getLow(), newRangeY.getHigh());
    }

    /**
     * @param val the double value to be formatted for display on screen.
     * @return formatted string from the double value
     */
    public static String formatForDisplay(double val) {
        if (Double.toString(val).length() <= 4) return Double.toString(val);
        NumberFormat formatter = new DecimalFormat("0.###E0");
        return formatter.format(val);
    }

    /**
     * plots the designated function within the window scope of this graph.
     * the function is colored and stroked with its designated style.
     *
     * @param function the GraphFunction instance to be plotted
     * @since May 21st 1:26 AM function color differentiated.
     */
    private void drawFunction(GraphFunction function) {
        getParent().stroke(function.getColor());
        getParent().strokeWeight(function.getStrokeWeight());
        getParent().noFill();
        if (function.getStyle().equals(GraphFunction.Style.CONTINUOUS)) {
            getParent().beginShape();
            for (int i = 0; i < function.getPlot().getCoordinates().size(); i++) {
                Point point = function.getPlot().getCoordinates().get(i);

                float translatedX = (float) point.getX() + x;
                float translatedY = y + h - (float) point.getY();

                if (!point.isValid() || !isInScope(translatedX, translatedY)) {
                    getParent().endShape();
                    getParent().beginShape();
                    continue;
                }

                if (point.isAsymptoteAnchor()) {
                    if (function.isAutoAsymptoteExtension()) getParent().vertex(translatedX, translatedY);
                    getParent().endShape();
                } else if (point.isAsymptoteTail()) {
                    getParent().beginShape();
                    if (function.isAutoAsymptoteExtension()) getParent().vertex(translatedX, translatedY);
                    if (function.isAsymptoteVisible()) {
                        getParent().pushStyle();
                        getParent().strokeWeight(lineWidth);
                        getParent().stroke(asymptoteColor);
                        JNode.dashLine(translatedX, y, translatedX, y + h, new float[]{4});
                        getParent().popStyle();
                    }
                } else {
                    getParent().vertex(translatedX, translatedY);
                }
            }
            getParent().endShape();
        } else if (function.getStyle().equals(GraphFunction.Style.DISCRETE)) {
            function.getPlot().getCoordinates().forEach(point -> {
                float translatedX = (float) point.getX() + x;
                float translatedY = y + h - (float) point.getY();
                if (isInScope(translatedX, translatedY))
                    getParent().point(translatedX, translatedY);
            });
        }
        if (function.isTangentLineVisible() && isMouseOver()) {
            double x = convertToPointOnGraph(getParent().mouseX, getParent().mouseY)[0];
            getParent().stroke(tangentLineColor);
            if (function.isMatchAuxiliaryLinesColor()) getParent().stroke(function.getColor());
            else getParent().stroke(getTextColor());
            drawTangentLineToPoint(x, function.getName());
        }
    }

    /**
     * break through Saturday, May 20th. Accelerated.
     * this method draws a tangent line to the designated curve.
     *
     * @param name the name of the function
     * @param x    a point on the function
     * @since May 21st, took me a while, but I fixed it!
     */
    public void drawTangentLineToPoint(double x, String name) {
        GraphFunction tangentLine = tangentLineToPoint(x, getFunction(name), rangeX.getStep());
        if (tangentLine == null) return;
        double y1 = rangeY.getLow(), y2 = rangeY.getHigh();
        ArrayList<Double> fx = solveInScope(rangeY.getLow(), tangentLine);
        ArrayList<Double> sx = solveInScope(rangeY.getHigh(), tangentLine);
        double x1, x2;
        if (fx.size() == 0 && sx.size() == 0) {
            x1 = rangeX.getLow();
            y1 = tangentLine.eval(x1);
            x2 = rangeX.getHigh();
            y2 = tangentLine.eval(x2);
        } else if (fx.size() == sx.size()) {
            x1 = fx.get(0);
            x2 = sx.get(0);
        } else {
            double slope = Double.valueOf(tangentLine.getName().split(",")[0]);
            if (fx.size() == 1) {
                x1 = fx.get(0);
                x2 = slope > 0 ? rangeX.getHigh() : rangeX.getLow();
                y2 = tangentLine.eval(x2);
            } else {
                x2 = sx.get(0);
                x1 = slope > 0 ? rangeX.getLow() : rangeX.getHigh();
                y1 = tangentLine.eval(x1);
            }
        }
        float p1[] = convertToCoordinateOnScreen(x1, y1);
        float p2[] = convertToCoordinateOnScreen(x2, y2);
        if (!isInScope(p1[0], p1[1]) || !isInScope(p2[0], p2[1])) return;
        getParent().strokeWeight(lineWidth);
        JNode.dashLine(p1[0], p1[1], p2[0], p2[1], new float[]{3, 5});
    }

    /**
     * solve for graphing purposes only.
     *
     * @param function the function to be solved for f(x) = y.
     * @return the numerical solutions to the designated function with an accuracy
     * that is directly proportional to the precision of the graph and within the
     * domain of the visible window of the graph.
     * @since May 20th
     */
    public ArrayList<Double> solveInScope(double y, GraphFunction function) {
        int precisionFactor = 5;
        return function.numericalSolve(y, rangeX.getLow(), rangeX.getHigh(), rangeX.getStep() / precisionFactor);
    }

    /**
     * calculates a tangent line to a function on a certain point and returns
     * the acquired function.
     *
     * @param x        the point on the graph in which a tangent line is going to be derived
     * @param function the function that the first derivative is going to be based on.
     * @since 9:37 PM, May 17th, breakthrough, succeeded!!!
     * May 21st: debugged floating point accuracy is now considered.
     */
    public static GraphFunction tangentLineToPoint(double x, GraphFunction function, double accuracy) {
        Point init = new Point(x - accuracy, function.eval(x - accuracy));
        Point end = new Point(x + accuracy, function.eval(x + accuracy));
        return Graph.createLinearFunction(init, end);
    }

    /**
     * #consider returning a interpreted function. (takes more time)
     * this method generates a compiled function from 2 points. (for now)
     *
     * @param first  the first point
     * @param second the second point
     * @return a linear implemented function from 2 points.
     */
    public static GraphFunction createLinearFunction(Point first, Point second) {
        double k = (first.getY() - second.getY()) / (first.getX() - second.getX());
        double b = first.getY() - k * first.getX();
        try {
            String k1 = new BigDecimal(k).toPlainString();
            String b1 = new BigDecimal(b).toPlainString();
            return new GraphFunction(Compiler.compile(k1 + "*x+(" + b1 + ")")).setName(k + "," + b);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @param function the function to be added into this graph.
     * @return this Graph instance for chained access.
     */
    public Graph add(GraphFunction function) {
        this.functions.add(function);
        if (w < 10 || h < 10) return this;
        function.updatePlot(rangeX, rangeY, h);
        function.getPlot().updateCoordinates(rangeY, w, h);
        return this;
    }

    public GraphFunction remove(String name) {
        for (int i = functions.size() - 1; i >= 0; i--) {
            GraphFunction function = functions.get(i);
            if (function.getName().equals(name))
                return functions.remove(i);
        }
        return null;
    }

    @Override
    public void resize(float x, float y) {
        super.resize(x, y);
        this.updateNumMarkings();
        this.updateFunctions();
    }

    private void updateNumMarkings() {
        if (w == 0 || h == 0 || minMarkingLength == 0 || maxMarkingLength == 0) return;
        stepValueX = this.calculateStepValue(rangeX, (int) w / minMarkingLength, (int) w / maxMarkingLength);
        stepValueY = this.calculateStepValue(rangeY, (int) h / minMarkingLength, (int) h / maxMarkingLength);
    }

    /**
     * The method calls the updates the coordinates on screen for each of the function.
     * This method should be called whenever the displayable is updated or plot points are altered.
     */
    private void updatePlotCoordinates() {
        for (GraphFunction function : functions) {
            if (!function.isDynamic())
                function.getPlot().updateCoordinates(rangeY, w, h);
        }
    }

    /**
     * This method re-plots each of the function according to the updated window dimension.
     * This method should be called each time when the domain or range of the graphing window is altered.
     */
    private void updatePlots() {
        this.updateStepValue();
        for (GraphFunction function : functions) {
            if (!function.isDynamic())
                function.updatePlot(rangeX, rangeY, h);
        }
    }

    public void setWindow(double minX, double maxX, double minY, double maxY) {
        rangeX = new Range(minX, maxX, rangeX.getStep());
        rangeY = new Range(minY, maxY);
        this.updateNumMarkings();
        new Thread(this::updateFunctions).start(); //performance enhancement May 20th
    }

    public void setWindowX(double minX, double maxX) {
        this.setWindow(minX, maxX, rangeY.getLow(), rangeY.getHigh());
    }

    public void setWindowY(double minY, double maxY) {
        this.setWindow(rangeX.getLow(), rangeX.getHigh(), minY, maxY);
    }

    /**
     * Updates the actual incremental value per plot point according to the designated step pixel length on screen
     *
     * @since April 7th
     */
    private void updateStepValue() {
        if (rangeX == null) rangeX = new Range(-10, 10);
        rangeX.setStep(rangeX.getSpan() / w * stepLength);
    }

    public void setStepLength(double pixels) {
        this.stepLength = pixels;
        this.updateFunctions();
    }

    /**
     * Overrides an existing function with a new definition.
     * NOTE: the overridden function would retain the original name
     *
     * @param name          the name of the existing function
     * @param function      the function that is going to replace the original one.
     * @param preserveStyle whether or not to preserve the graphics style of the original function
     */
    public boolean override(String name, GraphFunction function, boolean preserveStyle) {
        boolean overridden = false;
        for (int i = functions.size() - 1; i >= 0; i--) {
            if (functions.get(i).getName().equals(name)) {
                if (preserveStyle) function.inheritStyle(functions.get(i));
                functions.set(i, function.setName(name));
                overridden = true;
            }
        }
        if (!overridden) functions.add(function.setName(name));
        updateFunction(function);
        return overridden;
    }

    public GraphFunction getLastFunction() {
        if (functions.size() == 0) return null;
        return functions.get(functions.size() - 1);
    }

    public void updateFunction(GraphFunction func) {
        func.updatePlot(rangeX, rangeY, h);
        func.getPlot().updateCoordinates(rangeY, w, h);
    }

    public boolean override(String name, GraphFunction function) {
        return this.override(name, function, true);
    }

    /**
     * Comprehensive update
     */
    private void updateFunctions() {
        this.updatePlots();
        this.updatePlotCoordinates();
    }

    public GraphFunction getFunction(String name) {
        for (GraphFunction function : functions) {
            if (function.getName().equals(name))
                return function;
        }
        return null;
    }

    public double getMinX() {
        return rangeX.getLow();
    }

    public double getMaxX() {
        return rangeX.getHigh();
    }

    public double getMaxY() {
        return rangeY.getHigh();
    }

    public double getMinY() {
        return rangeY.getLow();
    }

    /**
     * converts an abs coordinate on screen to xy coordinate on graph.
     *
     * @param screenX x-coordinate on screen
     * @param screenY y-coordinate on screen
     * @return x-y coordinate on graph window
     */
    public double[] convertToPointOnGraph(float screenX, float screenY) {
        double cx = PApplet.map(screenX, x, x + w, (float) getMinX(), (float) getMaxX());
        double cy = PApplet.map(screenY, y, y + h, (float) getMaxY(), (float) getMinY());
        return new double[]{cx, cy};
    }


    /**
     * converts x-y coordinate graph to absolute coordinate on screen
     *
     * @param x x coordinate in the graph window
     * @param y y coordinate in the graph window
     * @return coordinate on screen
     */
    public float[] convertToCoordinateOnScreen(double x, double y) {
        float cx = Plot.map(x, rangeX.getLow(), rangeX.getHigh(), 0, w);
        float cy = Plot.map(y, rangeY.getLow(), rangeY.getHigh(), 0, h);
        return new float[]{this.x + cx, this.y + h - cy};
    }


    public void setMaxMarkingLength(int temp) {
        this.maxMarkingLength = temp;
        updateNumMarkings();
    }

    public void setMinMarkingLength(int temp) {
        this.minMarkingLength = temp;
        updateNumMarkings();
    }

    /**
     * TODO link with disable/enabling of event listeners
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        getEventListeners().forEach(eventListener -> {
            if (Mode.contains(eventListener.getId())) {
                eventListener.setDisabled(!mode.equals(eventListener.getId()));
            }
        });
    }

    public Mode getMode() {
        return mode;
    }

    public ArrayList<GraphFunction> getFunctions() {
        return functions;
    }

    /**
     * TODO debug, not yet functional!
     *
     * @param name         the name of the function to be tested for point over
     * @param screenX      coordinate-x on screen
     * @param screenY      coordinate-y on screen
     * @param allowed_diff allowed difference in pixels
     * @return boolean that indicates whether or not the point is over the function.
     */
    public boolean isOverFunction(String name, float screenX, float screenY, float allowed_diff) {
        GraphFunction function = this.getFunction(name);
        double graph_pos[] = this.convertToPointOnGraph(screenX, screenY);
        return function.containsPoint(graph_pos[0], graph_pos[1], allowed_diff / h * rangeX.getSpan(), 2);
    }

    public boolean isEvaluationOn() {
        return evaluationOn;
    }

    public void setEvaluationOn(boolean evaluationOn) {
        this.evaluationOn = evaluationOn;
    }

    public boolean isAxesVisible() {
        return axesVisible;
    }

    public void setAxesVisible(boolean axesVisible) {
        this.axesVisible = axesVisible;
    }

    public enum Mode {
        DRAG("DRAG"),
        ZOOM_OUT("ZOOM_OUT"),
        ZOOM_RECT("ZOOM_RECT"),
        ZOOM_IN("ZOOM_IN");

        private String name;
        private static String[] list;

        static {
            list = new String[]{"DRAG", "ZOOM_IN", "ZOOM_OUT", "ZOOM_RECT"};
        }

        Mode(String name) {
            this.name = name;
        }

        public boolean equals(Mode other) {
            return other.name.equals(name);
        }

        public boolean equals(String other) {
            return this.name.equals(other);
        }

        public static boolean contains(String name) {
            for (String s : list) if (s.equals(name)) return true;
            return false;
        }
    }

    @Override
    public Graph setId(String id) {
        super.setId(id);
        return this;
    }

    private void trace(double xPosOnGraph) {
        getParent().stroke(tracingLineColor);
        getParent().fill(tracingLineColor);

        float screenX = this.convertToCoordinateOnScreen(xPosOnGraph, 0)[0];
        if (!isInScope(screenX, y + h / 2)) return;
        getParent().strokeWeight(lineWidth);
        JNode.dashLine(screenX, y, screenX, y + h, new float[]{3});

        float[] offset = new float[]{3, 3}, dim = new float[]{w / 10, h / 25};
        ArrayList<Float> occupied = new ArrayList<>();
        ArrayList<Float> leftSideOccupied = new ArrayList<>();

        for (GraphFunction function : functions) {
            if (!function.isVisible() || !function.tracingEnabled()) continue;
            double yPosOnGraph = function.isDiscrete() ? function.getPlot().lookUp(xPosOnGraph, rangeX.getStep() / stepLength) : function.eval(xPosOnGraph);
            yPosOnGraph = yPosOnGraph == Double.MAX_VALUE ? function.eval(xPosOnGraph) : yPosOnGraph;
            float y = convertToCoordinateOnScreen(xPosOnGraph, yPosOnGraph)[1];
            if (!isInScope(x, y)) continue;
            if (function.isMatchAuxiliaryLinesColor()) getParent().stroke(function.getColor());
            getParent().strokeWeight(lineWidth); //is this necessary?
            if (!isEvaluationOn()) JNode.dashLine(x, y, x + w, y, new float[]{3});
            getParent().pushStyle();
            getParent().stroke(function.getColor());
            getParent().strokeWeight(function.getStrokeWeight() > 2 ? 6 : 5);
            getParent().point(screenX, y);
            getParent().popStyle();

            if (!isEvaluationOn()) continue;
            float[] pos = new float[]{screenX, y};
            int overlappedIndex = overlapped(occupied, pos[1], dim[1]);
            boolean insertedOnLeft = false;
            if (overlappedIndex != -1) {
                for (int i = 1; i < 5; i++) {
                    if (-1 == overlapped(leftSideOccupied, pos[1], dim[1]) && pos[0] - dim[0] - offset[0] > x) {
                        insertedOnLeft = true;
                        pos[0] -= dim[0] + offset[0];
                        pos[1] += offset[1];
                        break;
                    }
                    if (-1 == overlapped(occupied, pos[1] + dim[1] * i + offset[1] * i, dim[1])) {
                        if (pos[1] + dim[1] * i < this.y + h) {
                            pos[1] = pos[1] + dim[1] * i + offset[1] * (i + 1);
                            pos[0] += offset[0];
                            break;
                        }
                    }
                    if (-1 == overlapped(leftSideOccupied, pos[1] + dim[1] * i + offset[1] * i, dim[1]) && pos[0] - dim[0] - offset[0] > x) {
                        if (pos[1] + dim[1] * i < this.y + h) {
                            insertedOnLeft = true;
                            pos[1] = pos[1] + dim[1] * i + offset[1] * (i + 1);
                            pos[0] = pos[0] - dim[0] - offset[0];
                            break;
                        }
                    }
                    if (-1 == overlapped(occupied, pos[1] - dim[1] * i - offset[1] * i, dim[1])) {
                        if (pos[1] - dim[1] * i > this.y) {
                            pos[1] = pos[1] - dim[1] * i - offset[1] * (i);
                            pos[0] += offset[0];
                            break;
                        }
                    }
                    if (-1 == overlapped(leftSideOccupied, pos[1] - dim[1] * i - offset[1] * i, dim[1]) && pos[0] - dim[0] - offset[0] > x) {
                        if (pos[1] - dim[1] * i > this.y) {
                            insertedOnLeft = true;
                            pos[1] = pos[1] - dim[1] * i - offset[1] * (i);
                            pos[0] = pos[0] - dim[0] - offset[0];
                            break;
                        }
                    }
                }
            } else {
                pos[0] += offset[0];
                pos[1] += offset[1];
            }
            if (!insertedOnLeft && pos[0] + dim[0] > x + w) {
                pos[0] -= dim[0] + offset[0] * 2;
                insertedOnLeft = true;
            } else if (insertedOnLeft && pos[0] < x) {
                pos[0] += dim[0] + offset[0] * 2;
                insertedOnLeft = false;
            }

            if (pos[1] + dim[1] > this.y + h) {
                pos[1] -= dim[1] + offset[1] * 2;
            }

            new Label(pos[0], pos[1], dim[0], dim[1])
                    .setTextColor(function.getColor(), 255)
                    .setContent(function.getName() + (float) yPosOnGraph)
                    .setContourVisible(false)
                    .setBackgroundColor(function.getColor(), 50)
                    .display();
            if (insertedOnLeft) leftSideOccupied.add(pos[1]);
            else occupied.add(pos[1]);
        }
    }

    private int overlapped(ArrayList<Float> occupied, float currentPos, float height) {
        for (int i = 0; i < occupied.size(); i++) {
            Float f = occupied.get(i);
            if (Math.abs(f - currentPos) < height) {
                return i;
            }
        }
        return -1;
    }

    public boolean tracingIsOn() {
        return tracingOn;
    }
    public double getStepLength() {return stepLength;}

    public void setTracingOn(boolean tracingOn) {
        this.tracingOn = tracingOn;
        @SuppressWarnings("unchecked") ArrayList<Displayable> matching = (ArrayList<Displayable>) JNode.get("#functionInputWrapper");
        if (matching.size() == 0) return;
        HBox wrapper = (HBox) matching.get(0);
        if (tracingOn) {
            if (!wrapper.contains("#XVAL")) {
                TextInput input = (TextInput) new TextInput().setContent("x = ").setId("#XVAL");
                input.onFocus(() -> input.setContent("x = "));
                input.onKeyTyped(() -> input.setContent(input.getContent().contains("x = ") ? input.getContent() : "x = "));
                input.onEditing(() -> {
                    String temp = input.getContent().substring(4);
                    try {
                        this.trace(Double.valueOf(temp));
                    } catch (NumberFormatException ignore) {
                    }
                });
                wrapper.add(input);
                wrapper.setCollapseInvisible(true);
            } else {
                wrapper.get("#XVAL").setVisible(true);
            }
        } else {
            if (wrapper.contains("#XVAL")) {
                wrapper.get("#XVAL").setVisible(false);
            }
        }
    }
}
