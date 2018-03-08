package tests;

import jmc.MathContext;

import java.math.BigInteger;
import java.util.ArrayList;

import static tests.TestPrint.l;

public class JMathContextTest {

    public static void main(String[] args) {
        ArrayList<BigInteger> factors = MathContext.factor(new BigInteger("146580900"));
        for (BigInteger b : factors)
            System.out.println(b);
        l(MathContext.factorial(new BigInteger("300")));

    }



}
