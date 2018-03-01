package jmc;

/**
 * Created by Jiachen on 05/05/2017.
 */
public class Range {
    private double low;
    private double high;
    private double step;
    private boolean hasNextStep;
    private double current;

    public Range(double low, double high, double step) {
        this.low = low;
        this.high = high;
        this.step = step;
        current = low;
        hasNextStep = true;
        assertValidity();
    }

    public Range(Range clone) {
        this(clone.getLow(), clone.getHigh(), clone.getStep());
        this.hasNextStep = clone.hasNextStep;
        this.current = clone.current;
    }

    public Range(double low, double high) {
        this(low, high, 0);
    }

    public double getLow() {
        return this.low;
    }

    public double getHigh() {
        return this.high;
    }

    public double getCurStep() {
        return current;
    }

    public void reset() {
        current = low;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public void next() {
        if (current < high) current += step;
        else hasNextStep = false;
    }

    public double getSpan() {
        return high - low;
    }

    public boolean hasNextStep() {
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

    public boolean isInScope(double val) {
        return val <= high && val >= low;
    }

    public String toString() {
        return "low: " + low + " high: " + high + " step: " + step;
    }

    public int numSteps() {
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
    public void rescale(double scale) {
        double original_span = this.getSpan();
        double mid = (getLow() + getHigh()) / 2;
        original_span *= scale;
        this.low = mid - original_span/2;
        this.high = mid + original_span/2;
        current = low;
        hasNextStep = true;
    }
}
