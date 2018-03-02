package jmc.cas;

/**
 * Created by Jiachen on 3/2/18.
 * Fraction
 */
public class Fraction extends RawValue {
    private int numerator;
    private int denominator;

    public Fraction(int numerator, int denominator) {
        super(numerator / denominator);
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Fraction(double val) {
        super(val);
    }

    public Fraction convertToFraction(double val, double precision) {

    }

    @Override
    public double doubleValue() {
        return (double) numerator / (double) denominator;
    }
}
