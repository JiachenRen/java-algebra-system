package tests;

import jmc.MathContext;

import java.math.BigInteger;
import java.util.ArrayList;

public class BigIntegerFactorTest {

    public static void main(String[] args) {
        ArrayList<BigInteger> factors = MathContext.factor(new BigInteger(args[0]));
        for (BigInteger b : factors)
            System.out.println(b);

    }

    public static void l(String s) {
        System.out.println(s);
    }


}
