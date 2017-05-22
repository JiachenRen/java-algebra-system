package jmc_lib;

/**
 * Created by Jiachen on 03/05/2017.
 */
public class Raw implements Operable {
    private java.lang.Number raw;

    public Raw(java.lang.Number raw) {
        this.raw = raw;
    }

    public Raw(Raw raw) {
        this(raw.raw);
    }

    public double eval(double x) {
        return raw.doubleValue();
    }

    public String toString() {
        double extracted = raw.doubleValue();
        String formatted = Graph.formatForDisplay(extracted);
        return extracted >= 0 ? formatted : "(" + formatted + ")";
    }

    public Raw replicate() {
        return new Raw(raw);
    }
}
