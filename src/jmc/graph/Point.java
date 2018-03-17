package jmc.graph;

/**
 * Created by Jiachen on 05/05/2017.
 * Point on the graph (graphing capabilities only)
 */
public class Point {
    private double x;
    private double y;
    private boolean isValidPoint;
    private boolean isAsymptoteAnchor;
    private boolean isAsymptoteTail;
    private boolean isOutOfScope;

    {
        isValidPoint = true;
    }

    public Point(double x, double y) {
        setX(x);
        setY(y);
    }

    Point(Point clone) {
        this.x = clone.x;
        this.y = clone.y;
        this.isValidPoint = clone.isValidPoint;
        this.isAsymptoteAnchor = clone.isAsymptoteAnchor;
        this.isAsymptoteTail = clone.isAsymptoteTail;
        this.isOutOfScope = clone.isOutOfScope;
    }

    public static double dist(Point a, Point b) {
        return Point.dist(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private static double dist(double x1, double y1, double x2, double y2) {
        return Math.pow(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2), 0.5);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        if (Double.isNaN(x) || Double.isInfinite(x))
            isValidPoint = false;
    }

    boolean isValid() {
        return isValidPoint;
    }

    public double dist(Point other) {
        return Math.abs(this.getY() - other.getY());
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        if (Double.isNaN(y) || Double.isInfinite(y) || y > 1E37)
            isValidPoint = false;
    }

    public String toString() {
        return "x: " + x + " y " + y;
    }

    Point setAsAnchor() {
        isAsymptoteAnchor = true;
        return this;
    }

    boolean isAsymptoteAnchor() {
        return isAsymptoteAnchor;
    }

    Point setAsTail() {
        isAsymptoteTail = true;
        return this;
    }

    boolean isAsymptoteTail() {
        return isAsymptoteTail;
    }

    public boolean isOutOfScope() {
        return isOutOfScope;
    }

    void setOutOfScope(boolean temp) {
        this.isOutOfScope = temp;
    }
}
