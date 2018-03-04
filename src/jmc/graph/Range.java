package jmc.graph;

/**
 * Created by Jiachen on 05/05/2017.
 * Range class (for graphing capabilities only)
 */
public class Range {
    private double low;
    private double high;
    private double step;
    private boolean hasNextStep;
    private double current;

    Range(double low, double high, double step) {
        this.low = low;
        this.high = high;
        this.step = step;
        current = low;
        hasNextStep = true;
        assertValidity();
    }

    Range(Range clone) {
        this(clone.getLow(), clone.getHigh(), clone.getStep());
        this.hasNextStep = clone.hasNextStep;
        this.current = clone.current;
    }

    Range(double low, double high) {
        this(low, high, 0);
    }

    double getLow() {
        return this.low;
    }

    double getHigh() {
        return this.high;
    }

    double getCurStep() {
        return current;
    }

    double getStep() {
        return step;
    }

    void setStep(double step) {
        this.step = step;
    }

    void next() {
        if (current < high) current += step;
        else hasNextStep = false;
    }

    double getSpan() {
        return high - low;
    }

    boolean hasNextStep() {
        return hasNextStep;
    }

    private void assertValidity() {
        if (high < low) {
            double temp = low;
            low = high;
            high = temp;
        }

        while (numSteps() > 2000 && step != 0.0) {
            step *= 1.05;
        }

    }

    boolean isInScope(double val) {
        return val <= high && val >= low;
    }

    public String toString() {
        return "low: " + low + " high: " + high + " step: " + step;
    }

    private int numSteps() {
        return (int) (getSpan() / getStep());
    }

    /**
     * rescale the range according to the scaling factor
     *
     * @param scale the scale to be applied on the range. 1.5
     *              would make the span 1.5 times the original
     *              while 0.5 would make the span 0.5 times
     *              the original.
     */
    void rescale(double scale) {
        double original_span = this.getSpan();
        double mid = (getLow() + getHigh()) / 2;
        original_span *= scale;
        this.low = mid - original_span / 2;
        this.high = mid + original_span / 2;
        current = low;
        hasNextStep = true;
    }
}
