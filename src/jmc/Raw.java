package jmc;

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
        String formatted = /*(char) 27 + "[32m" + */Graph.formatForDisplay(extracted)/* + (char) 27 + "[0m"*/;
        if (formatted.endsWith(".0")) formatted = formatted.substring(0, formatted.length() - 2);
        return extracted >= 0 ? formatted : "(" + formatted + ")";
    }

    public Raw replicate() {
        return new Raw(raw);
    }

    public double doubleValue() {
        return raw.doubleValue();
    }

    public boolean equals(Operable other) {
        return other instanceof Raw && ((Raw) other).doubleValue() == this.doubleValue();
    }

    public Operable plugIn(Operable nested) {
        return new Raw(this);
    }
}
