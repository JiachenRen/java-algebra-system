package jmc.cas;

import jmc.MathContext;

import java.math.BigInteger;

/**
 * Created by Jiachen on 3/2/18.
 * Fraction
 */
public class Fraction extends RawValue {
    private long numerator;
    private long denominator;
    public static double TOLERANCE = 5E-7;

    public Fraction(long numerator, long denominator) {
        super(numerator / denominator);
        this.numerator = numerator;
        this.denominator = denominator;
        this.reduce();
    }

    public static RawValue convertToFraction(double val) {
        String s = String.valueOf(val);
        long digitsDec = s.length() - 1 - s.indexOf('.');
        long denominator = 1;
        for (int i = 0; i < digitsDec; i++) {

            val *= 10;
            denominator *= 10;
        }

        long numerator = Math.round(val);
        return new Fraction(numerator, denominator).reduce();
    }

    private long gcd() {
        return gcd(numerator, denominator);
    }

    public RawValue add(RawValue o) {
        if (!(o instanceof Fraction)) {
            if (o.isInteger()) o = new Fraction(o.intValue(), 1);
            else o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
        } else o = o.replicate();
        Fraction f = (Fraction) o;

        long lcm = lcm(denominator, f.denominator);
        this.simult(lcm / denominator);
        f.simult(lcm / f.denominator);
        this.numerator += f.numerator;
        return this.reduce();
    }

    public RawValue sub(RawValue o) {
        return this.add(o.replicate().negate());
    }

    public RawValue mult(RawValue o) {
        if (o instanceof Fraction) {
            numerator *= ((Fraction) o).numerator;
            denominator *= ((Fraction) o).denominator;
            return this.reduce();
        } else if (o.isInteger()) {
            numerator *= o.intValue();
            return this.reduce();
        } else {
            o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
            return mult(o);
        }
    }

    @Override
    public RawValue inverse() {
        long tmp = denominator;
        denominator = numerator;
        numerator = tmp;
        return this;
    }

    public RawValue div(RawValue o) {
        return mult(o.inverse());
    }

    public Fraction negate() {
        this.numerator *= -1;
        return this;
    }

    private void simult(long n) {
        denominator *= n;
        numerator *= n;
    }

    public RawValue reduce() {
        long gcd = gcd();
        this.numerator /= gcd;
        this.denominator /= gcd;
        if (denominator == 1) {
            return new RawValue(numerator);
        } else if (numerator == 0) {
            return new RawValue(0);
        } else if (numerator < 0 && denominator < 0) {
            this.numerator *= -1;
            this.denominator *= -1;
        }
        return this;
    }

    /**
     * @param a first number
     * @param b second number
     * @return greatest common divisor
     */
    private static long gcd(long a, long b) {
        return MathContext.gcd(new BigInteger(Long.toString(Math.abs(a))), new BigInteger(Long.toString(Math.abs(b)))).longValue();
    }

    private static long lcm(long a, long b) {
        return MathContext.lcm(new BigInteger(Long.toString(Math.abs(a))), new BigInteger(Long.toString(Math.abs(b)))).longValue();
    }

    /**
     * Works like a charm -- outputs the exact fraction as my TI-nspire CAS.
     *
     * @param val       the double to be converted to fraction
     * @param tolerance desired difference between fraction and the actual double value
     * @return Fraction derived from the provided double value
     */
    public static RawValue convertToFraction(double val, double tolerance) {
        if (val < 0) {
            RawValue raw = convertToFraction(-val);
            if (raw instanceof Fraction)
                ((Fraction) raw).numerator *= -1;
            return raw;
        }
        //TODO: there should be a maximum TOLERANCE for inputs like 0.1403508772
        long numerator = 1, h2 = 0, denominator = 0, k2 = 1;
        double b = val;
        do {
            long a = (long) Math.floor(b);
            long aux = numerator;
            numerator = a * numerator + h2;
            h2 = aux;
            aux = denominator;
            denominator = a * denominator + k2;
            k2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(val - numerator / (double) denominator) > val * tolerance);

        return new Fraction(numerator, denominator).reduce();
    }

    public String toString() {
        return "(" + numerator + "/" + denominator + ")";
    }

    public Fraction setNumerator(long n) {
        this.numerator = n;
        return this;
    }

    public Fraction setDenominator(long n) {
        this.denominator = n;
        return this;
    }

    public Fraction replicate() {
        return new Fraction(numerator, denominator);
    }

    @Override
    public double doubleValue() {
        return (double) numerator / (double) denominator;
    }
}
