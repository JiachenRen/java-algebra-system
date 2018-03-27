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
    private static final BigInteger BIG_ZERO = BigInteger.ZERO; //wow this is such a weird bug!
    private static final BigInteger BIG_ONE = BigInteger.ONE;
    public static final Fraction UNDEF = new Fraction(0, 0);
    public static double TOLERANCE = 5E-7;
    private BigInteger numerator;
    private BigInteger denominator;


    private Fraction(BigInteger numerator, BigInteger denominator) {
        super(Double.NaN);
        this.numerator = numerator;
        this.denominator = denominator;
        this.reduce();
    }

    public Fraction(long numerator, long denominator) {
        this(new BigInteger(Long.toString(numerator)), new BigInteger(Long.toString(denominator)));
    }

    public RawValue reduce() {
        if (isUndefined()) return UNDEF;
        BigInteger gcd = gcd();
        setNumerator(numerator.divide(gcd));
        setDenominator(this.denominator.divide(gcd));
        if (denominator.equals(BIG_ONE)) {
            return new RawValue(numerator.doubleValue());
        } else if (numerator.equals(BIG_ZERO)) {
            return new RawValue(0);
        } else if (numerator.compareTo(BIG_ZERO) < 0 && denominator.compareTo(BIG_ZERO) < 0) {
            setNumerator(this.numerator.multiply(BIG_ONE.negate()));
            setDenominator(this.denominator.multiply(BIG_ONE.negate()));
        }
        return this;
    }

    private BigInteger gcd() {
        return MathContext.gcd(numerator.abs(), denominator.abs());
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
    public static Operable extractRoot(BigInteger n, BigInteger r) {
        if (n.equals(BIG_ZERO)) return null;
        else if (n.abs().equals(BIG_ONE)) {
            BinaryOperation in = new BinaryOperation(ONE, "^", ZERO);
            return new BinaryOperation(new RawValue(n.doubleValue()), "*", in);
        } else if (r.equals(BIG_ZERO)) throw new IllegalArgumentException("root cannot be negative");
        boolean isNegative = false;
        if (n.compareTo(BIG_ZERO) < 0) {
            if (r.mod(BigInteger.valueOf(2)).equals(BIG_ZERO)) return UNDEF;
            isNegative = true;
            n = n.multiply(BIG_ONE.negate());
        }
        ArrayList<BigInteger> factors = factor(n);
        ArrayList<BigInteger> uniqueFactors = getUniqueFactors(factors);
        int[] num = numOccurrences(uniqueFactors, factors);
        long ext = isNegative ? -1 : 1, n1 = 1;
        for (int i = 0; i < num.length; i++) {
            long k = num[i] / r.intValueExact();
            BigInteger u = uniqueFactors.get(i);
            ext *= Math.pow(u.longValueExact(), k);
            long q = num[i] - r.longValueExact() * k;
            n1 *= Math.pow(u.longValueExact(), q);
        }

        BinaryOperation exp = new BinaryOperation(new RawValue(1), "/", new RawValue(r.doubleValue()));
        BinaryOperation irr = new BinaryOperation(new RawValue(n1), "^", exp);
        return new BinaryOperation(new RawValue(ext), "*", irr);
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
                ((Fraction) raw).mult(ONE.negate());
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
        if (o.isUndefined() || this.isUndefined()) return UNDEF;
        if (!(o instanceof Fraction)) {
            if (o.isInteger()) o = new Fraction(o.toBigInteger(), BIG_ONE);
            else o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
        } else o = o.copy();
        Fraction f = (Fraction) o;

        BigInteger lcm = lcm(denominator, f.denominator);
        this.simult(lcm.divide(denominator));
        f.simult(lcm.divide(f.denominator));
        setNumerator(this.numerator.add(f.numerator));
        return this.reduce();
    }

    public Operable exp(RawValue o) {
        if (o.isUndefined() || this.isUndefined()) return UNDEF;
        if (o instanceof Fraction) {
            Fraction f = ((Fraction) o);
            this.exp(f.numerator.intValueExact());
            this.reduce();
            Operable o1 = extractRoot(this.numerator, f.denominator);
            Operable o2 = extractRoot(this.denominator, f.denominator);
            if (o1 == null || o1.isUndefined() || o2 == null || o2.isUndefined()) return UNDEF;
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
            this.exp(o.toBigInteger().intValueExact());
            this.reduce();
            return this;
        } else {
            o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
            return exp(o);
        }
    }

    public RawValue exp(int i) {
        if (i < 0) {
            i *= -1;
            this.inverse();
        }
        this.numerator = this.numerator.pow(i);
        this.denominator = this.denominator.pow(i);
        return this.reduce();
    }

    public RawValue sub(RawValue o) {
        return this.add(o.copy().negate());
    }

    public RawValue mult(RawValue o) {
        if (o.isUndefined() || this.isUndefined()) return UNDEF;
        if (o instanceof Fraction) {
            setNumerator(numerator.multiply(((Fraction) o).numerator));
            setDenominator(denominator.multiply(((Fraction) o).denominator));
            return this.reduce();
        } else if (o.isInteger()) {
            setNumerator(numerator.multiply(o.toBigInteger()));
            return this.reduce();
        } else {
            o = Fraction.convertToFraction(o.doubleValue(), TOLERANCE);
            return mult(o);
        }
    }

    public RawValue div(RawValue o) {
        return mult(o.inverse());
    }

    private void simult(BigInteger n) {
        setDenominator(denominator.multiply(n));
        setNumerator(numerator.multiply(n));
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public double doubleValue() {
        if (isUndefined()) return Double.NaN;
        return numerator.doubleValue() / denominator.doubleValue();
    }

    @Override
    public Fraction inverse() {
        if (isUndefined()) return Fraction.UNDEF;
        BigInteger tmp = denominator;
        denominator = numerator;
        numerator = tmp;
        return this;
    }

    @Override
    public boolean isZero() {
        return !isUndefined() && denominator.equals(BIG_ZERO);
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
        return !isUndefined() && numerator.divide(denominator).compareTo(BIG_ZERO) > 0;
    }

    @Override
    public boolean isUndefined() {
        return denominator.equals(BIG_ZERO);
    }

    public Fraction copy() {
        return new Fraction(numerator, denominator);
    }

    public Operable explicitNegativeForm() {
        if (doubleValue() > 0) return this;
        else return new BinaryOperation(ONE.negate(), "*", this.copy().negate());
    }

    public Fraction negate() {
        Fraction clone = this.copy();
        clone.setNumerator(clone.numerator.multiply(BIG_ONE.negate()));
        clone.reduce();
        return clone;
    }

    public Operable beautify() {
        return this;
    }

    public BigInteger getNumerator() {
        return numerator;
    }

    public Fraction setNumerator(BigInteger n) {
        this.numerator = n;
        return this;
    }

    public BigInteger getDenominator() {
        return denominator;
    }

    public Fraction setDenominator(BigInteger n) {
        this.denominator = n;
        return this;
    }
}
