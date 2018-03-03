package jmc.cas;

import jmc.MathContext;

import java.math.BigInteger;

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

    public static Fraction convertToFraction(double val) {
        String s = String.valueOf(val);
        int digitsDec = s.length() - 1 - s.indexOf('.');
        int denominator = 1;
        for (int i = 0; i < digitsDec; i++) {
            val *= 10;
            denominator *= 10;
        }
        int numerator = (int) Math.round(val);
        return new Fraction(numerator, denominator).reduce();
    }

    private int gcd() {
        return gcd(Math.abs(numerator), Math.abs(denominator));
    }

    public Fraction reduce() {
        int gcd = gcd();
        this.numerator /= gcd;
        this.denominator /= gcd;
        return this;
    }

    /**
     * @param a first number
     * @param b second number
     * @return greatest common divisor
     */
    private static int gcd(int a, int b) {
        return MathContext.gcd(new BigInteger(Integer.toString(a)), new BigInteger(Integer.toString(b))).intValue();
    }

    /**
     * Works like a charm -- outputs the exact fraction as my TI-nspire CAS.
     *
     * @param val       the double to be converted to fraction
     * @param tolerance desired difference between fraction and the actual double value
     * @return Fraction derived from the provided double value
     */
    public static Fraction convertToFraction(double val, double tolerance) {
        if (val < 0) {
            Fraction fraction = convertToFraction(-val);
            fraction.numerator *= -1;
            return fraction;
        }
        int numerator = 1, h2 = 0, denominator = 0, k2 = 1;
        double b = val;
        do {
            int a = (int) Math.floor(b);
            int aux = numerator;
            numerator = a * numerator + h2;
            h2 = aux;
            aux = denominator;
            denominator = a * denominator + k2;
            k2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(val - numerator / (double) denominator) > val * tolerance);

        return new Fraction(numerator, denominator);
    }

    public String toString() {
        return "(" + numerator + "/" + denominator + ")";
    }

    public Fraction setNumerator(int n) {
        this.numerator = n;
        return this;
    }

    public Fraction setDenominator(int n) {
        this.denominator = n;
        return this;
    }

    @Override
    public double doubleValue() {
        return (double) numerator / (double) denominator;
    }
}
