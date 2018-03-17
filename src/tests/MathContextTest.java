package tests;

import jmc.MathContext;
import jmc.cas.Operable;
import jmc.cas.RawValue;

import java.math.BigInteger;
import java.util.ArrayList;

import static jmc.utils.ColorFormatter.*;
import static tests.TestPrint.l;

public class MathContextTest {

    public static void main(String[] args) {
        ArrayList<BigInteger> factors = MathContext.factor(new BigInteger("100000"));
        for (BigInteger b : factors)
            System.out.println(b);
//        l(MathContext.factorial(new BigInteger("300")));
        for (int i = 0; i < 10000; i++) {
            int finalI = i;
            MathContext.toBaseExponentPairs(i).stream()
                    .map(pair -> new RawValue(pair[0]).exp(pair[1]))
                    .reduce(Operable::mult)
                    .ifPresent(o -> l(boldBlack(finalI + " -> ")
                            + lightBlue(o + " -> ")
                            + lightGreen(o.val())));
        }
    }


}
