package jmc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by Jiachen on 03/05/2017.
 * Math Context
 */
public class MathContext {
    private final static BigInteger ZERO = new BigInteger("0");
    private final static BigInteger ONE = new BigInteger("1");
    private final static BigInteger TWO = new BigInteger("2");
    private final static SecureRandom random = new SecureRandom();

    /**
     * Find Greatest Common Divisor using The Euclidean Algorithm
     *
     * @param a,b the integers in which a gcd is going to be derived. No negative
     *            inputs would be accepted, or a "modulation by 0" ArithmeticException
     *            would be thrown
     * @return the greatest common divisor
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        if (a.equals(ZERO) || b.equals(ZERO)) return a.equals(ZERO) ? b : a;
        if (a.compareTo(b) > 0) return gcd(b, a.mod(b));
        else return gcd(a, b.mod(a));
    }

    public static BigInteger f(BigInteger a) {
        if (a.equals(ONE) || a.equals(ZERO)) return a;
        else return a.multiply(f(a.subtract(ONE)));
    }

    public static long f(long a) {
        return f(new BigInteger(Long.toString(a))).longValue();
    }

    /**
     * The least common multiple of a and b is the product divided by the greatest common divisor. I.e. lcm(a, b) = ab/gcd(a, b).
     * --source: http://stackoverflow.com/questions/3154454
     * lowest common multiple method
     *
     * @param a,b the integers in which a lcm is going to be computed for
     * @return the lowest common multiple
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        BigInteger gcd = gcd(a, b);
        if (gcd.equals(ZERO))
            throw new ArithmeticException("cannot find lcm of 0");
        return a.multiply(b).divide(gcd).abs();
    }

    private static BigInteger rho(BigInteger N) {
        BigInteger divisor;
        BigInteger c = new BigInteger(N.bitLength(), random);
        BigInteger x = new BigInteger(N.bitLength(), random);
        BigInteger xx = x;

        // check divisibility by 2
        if (N.mod(TWO).compareTo(ZERO) == 0) return TWO;

        do {
            x = x.multiply(x).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            divisor = MathContext.gcd(x.subtract(xx).abs(), N.abs());
        } while ((divisor.compareTo(ONE)) == 0);

        return divisor;
    }

    /**
     * Recursively search for factors of integer N and appends the factors into the provided
     * BigInteger ArrayList.
     *
     * @param N       the BigInteger object that is going to be factored.
     * @param factors a BigInteger ArrayList instance in which the factors are going to be
     *                stored in.
     */
    private static void recursiveFactor(BigInteger N, ArrayList<BigInteger> factors) {
        if (N.compareTo(ONE) == 0) return;
        if (N.isProbablePrime(20)) {
            factors.add(N);
            return;
        }
        BigInteger divisor = rho(N);
        recursiveFactor(divisor, factors);
        recursiveFactor(N.divide(divisor), factors);
    }

    /**
     * This method wraps the recursiveFactor(BigInteger N, ArrayList<BigInteger> factors) method.
     *
     * @param N
     * @return
     */
    public static ArrayList<BigInteger> factor(BigInteger N) {
        ArrayList<BigInteger> factors = new ArrayList<>();
        recursiveFactor(N, factors);
        return factors;
    }

}
