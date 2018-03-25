package jas;

import jas.core.JMCException;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static BigInteger factorial(BigInteger a) {
        if (a.equals(ONE) || a.equals(ZERO)) return a;
        else return a.multiply(factorial(a.subtract(ONE)));
    }

    public static long factorial(long a) {
        return factorial(new BigInteger(Long.toString(a))).longValue();
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
     * 1 is not returned as a factor.
     * e.g. factor(12) returns [2,2,3]
     *
     * @param N the number to be factored
     * @return an ArrayList containing the factors of N.
     */
    public static ArrayList<BigInteger> factor(BigInteger N) {
        ArrayList<BigInteger> factors = new ArrayList<>();
        recursiveFactor(N, factors);
        return factors;
    }

    /**
     * @param n number
     * @return factors of n in an ArrayList sorted from smallest to largest.
     */
    public static ArrayList<Long> getFactors(long n) {
        BigInteger i = new BigInteger(Long.toString(n));
        List<Long> factors = MathContext.factor(i).stream()
                .map(BigInteger::longValue)
                .sorted((a, b) -> a <= b ? -1 : 1)
                .collect(Collectors.toList());
        return (ArrayList<Long>) factors;
    }

    public static ArrayList<Long> getUniqueFactors(ArrayList<Long> factors) {
        ArrayList<Long> uniqueFactors = new ArrayList<>();
        long cur = 1;
        for (Long f : factors) {
            if (cur != f) {
                uniqueFactors.add(f);
                cur = f;
            }
        }
        return uniqueFactors;
    }

    /**
     * e.g.
     * targets: [3,5,7]
     * list:    [3,5,3,7,3,3,5,5]
     * return:  [4,3,1]
     *
     * @param targets ArrayList containing unique numbers
     * @param pool    ArrayList containing repeated numbers
     * @return an array having the same dimension as "targets" with each item
     * being the num occurrences of # in targets at the corresponding index.
     */
    public static int[] numOccurrences(ArrayList<Long> targets, ArrayList<Long> pool) {
        int[] num = new int[targets.size()];
        int max = targets.get(targets.size() - 1).intValue();
        int[] map = new int[max + 1];
        for (int i = 0; i < targets.size(); i++) {
            map[targets.get(i).intValue()] = i;
        }
        pool.forEach(f -> num[map[f.intValue()]] += 1);
        return num;
    }

    public static boolean allTheSame(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] != arr[i + 1])
                return false;
        }
        return true;
    }

    public static long mult(ArrayList<Long> longs) {
        final long[] out = {1};
        longs.forEach(l -> out[0] *= l);
        return out[0];
    }

    public static ArrayList<int[]> toBaseExponentPairs(long n) {
        ArrayList<int[]> pairs = new ArrayList<>();
        if (n < 0) throw new JMCException("input must be positive");
        else if (n == 0 || n == 1) return pairs;
        ArrayList<Long> factors = getFactors(n);
        ArrayList<Long> uFactors = getUniqueFactors(factors);
        int[] num = numOccurrences(uFactors, factors);
        for (int i = 0; i < uFactors.size(); i++) {
            Long factor = uFactors.get(i);
            pairs.add(new int[]{factor.intValue(), num[i]});
        }
        return pairs;
    }

}
