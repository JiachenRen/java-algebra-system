package jas.core.components;

import jas.MathContext;
import jas.core.Operable;
import jas.core.operations.BinaryOperation;

import java.math.BigInteger;
import java.util.ArrayList;

import static jas.MathContext.*;
import static jas.core.Mode.FRACTION_COLOR;
import static jas.core.Mode.PARENTHESIS_COLOR;
import static jas.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 3/2/18.
 * Fraction
 */
public class Fraction extends RawValue {
    public static double TOLERANCE = 5E-7;
    public static Fraction UNDEF = new Fraction(0, 0);
    private long numerator;
    private long denominator;

    public Fraction(long numerator, long denominator) {
        super(denominator == 0 ? Double.NaN : numerator / denominator);
        this.numerator = numerator;
        this.denominator = denominator;
        this.reduce();
    }

    public RawValue reduce() {
        if (isUndefined()) return RawValue.UNDEF;
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

    private long gcd() {
        return gcd(numerator, denominator);
    }

    /**
     * @param a first number
     * @param b second number
     * @return greatest common divisor
     */
    private static long gcd(long a, long b) {
        return MathContext.gcd(new BigInteger(Long.toString(Math.abs(a))), new BigInteger(Long.toString(Math.abs(b)))).longValue();
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

    /**
     * extract root, n^(1/r) to a*b^(1/r)
     *
     * @param r root (square, cube, quad, etc)
     * @param n number within the root, n^(1/r)
     * @return irrational form Fraction or BinaryOperation
     */
    public static Operable extractRoot(long n, long r) {
        if (n == 0) return null;
        else if (n == 1 || n == -1) {
            BinaryOperation in = new BinaryOperation(RawValue.ONE, "^", RawValue.ZERO);
            return new BinaryOperation(new RawValue(n), "*", in);
        } else if (r == 0) throw new IllegalArgumentException("root cannot be negative");
        boolean isNegative = false;
        if (n < 0) {
            if (r % 2 == 0) return RawValue.UNDEF;
            isNegative = true;
            n *= -1;
        }
        ArrayList<Long> factors = getFactors(n);
        ArrayList<Long> uniqueFactors = getUniqueFactors(factors);
        int[] num = numOccurrences(uniqueFactors, factors);
        int ext = isNegative ? -1 : 1, n1 = 1;
        for (int i = 0; i < num.length; i++) {
            long k = num[i] / r;
            long u = uniqueFactors.get(i);
            ext *= Math.pow(u, k);
            long q = num[i] - r * k;
            n1 *= Math.pow(u, q);
        }

        BinaryOperation exp = new BinaryOperation(new RawValue(1), "/", new RawValue(r));
        BinaryOperation irr = new BinaryOperation(new RawValue(n1), "^", exp);
        return new BinaryOperation(new RawValue(ext), "*", irr);
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

    public RawValue add(RawValue o) {
        if (o.isUndefined() || this.isUndefined()) return RawValue.UNDEF;
        if (!(o instanceof Fraction)) {
            if (o.isInteger()) o = new Fraction(o.longValue(), 1);
            else o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
        } else o = o.copy();
        Fraction f = (Fraction) o;

        long lcm = lcm(denominator, f.denominator);
        this.simult(lcm / denominator);
        f.simult(lcm / f.denominator);
        this.numerator += f.numerator;
        return this.reduce();
    }

    public Operable exp(RawValue o) {
        if (o.isUndefined() || this.isUndefined()) return RawValue.UNDEF;
        if (o instanceof Fraction) {
            Fraction f = ((Fraction) o);
            this.exp(f.numerator);
            this.reduce();
            Operable o1 = extractRoot(this.numerator, f.denominator);
            Operable o2 = extractRoot(this.denominator, f.denominator);
            if (o1 == null || o1.isUndefined() || o2 == null || o2.isUndefined()) return RawValue.UNDEF;
            BinaryOperation nu = (BinaryOperation) o1;
            BinaryOperation de = (BinaryOperation) o2;
            BinaryOperation irr = (BinaryOperation) de.getRight();
            Operable a = new BinaryOperation(de.getLeft(), "*", irr.getLeft());
            Operable c = new BinaryOperation(new RawValue(1), "-", irr.getRight());
            BinaryOperation conjugate = new BinaryOperation(irr.getLeft(), "^", c);
            Operable d = new BinaryOperation(nu.getLeft(), "/", a);
            BinaryOperation e = new BinaryOperation(nu.getRight(), "*", conjugate);
            return new BinaryOperation(d, "*", e).simplify();
        } else if (o.isInteger()) {
            this.exp(o.longValue());
            this.reduce();
            return this;
        } else {
            o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
            return exp(o);
        }
    }

    public RawValue exp(long i) {
        if (i < 0) {
            i *= -1;
            this.inverse();
        }
        this.numerator = (long) Math.pow(this.numerator, i);
        this.denominator = (long) Math.pow(this.denominator, i);
        return this.reduce();
    }

    public RawValue sub(RawValue o) {
        return this.add(o.copy().negate());
    }

    public RawValue mult(RawValue o) {
        if (o.isUndefined() || this.isUndefined()) return RawValue.UNDEF;
        if (o instanceof Fraction) {
            numerator *= ((Fraction) o).numerator;
            denominator *= ((Fraction) o).denominator;
            return this.reduce();
        } else if (o.isInteger()) {
            numerator *= o.longValue();
            return this.reduce();
        } else {
            o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
            return mult(o);
        }
    }

    public RawValue div(RawValue o) {
        return mult(o.inverse());
    }

    private void simult(long n) {
        denominator *= n;
        numerator *= n;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public double doubleValue() {
        if (isUndefined()) return Double.NaN;
        return (double) numerator / (double) denominator;
    }

    @Override
    public Fraction inverse() {
        if (isUndefined()) return Fraction.UNDEF;
        long tmp = denominator;
        denominator = numerator;
        numerator = tmp;
        return this;
    }

    @Override
    public boolean isZero() {
        return !isUndefined() && denominator == 0;
    }

    public String toString() {
        return "(" + numerator + "/" + denominator + ")";
    }

    /**
     * @return string representation of the operable coded with Ansi color codes.
     */
    @Override
    public String coloredString() {
        return color("(", PARENTHESIS_COLOR) + color(numerator + "/" + denominator, FRACTION_COLOR) + color(")", PARENTHESIS_COLOR);
    }

    @Override
    public boolean isPositive() {
        return !isUndefined() && numerator / denominator > 0;
    }

    @Override
    public boolean isUndefined() {
        return super.isUndefined() || denominator == 0;
    }

    public Fraction copy() {
        return new Fraction(numerator, denominator);
    }

    public Operable explicitNegativeForm() {
        if (doubleValue() > 0) return this;
        else return new BinaryOperation(RawValue.ONE.negate(), "*", this.copy().negate());
    }

    public Fraction negate() {
        Fraction clone = this.copy();
        clone.numerator *= -1;
        clone.reduce();
        return clone;
    }

    public Operable beautify() {
        return this;
    }

    public long getNumerator() {
        return numerator;
    }

    public Fraction setNumerator(long n) {
        this.numerator = n;
        return this;
    }

    public long getDenominator() {
        return denominator;
    }

    public Fraction setDenominator(long n) {
        this.denominator = n;
        return this;
    }
}
