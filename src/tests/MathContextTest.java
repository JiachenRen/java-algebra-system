package tests;

import jmc.MathContext;
import jmc.cas.Operation;

import java.math.BigInteger;
import java.util.ArrayList;

import static tests.TestPrint.l;
import static jmc.utils.ColorFormatter.*;

public class MathContextTest {

    public static void main(String[] args) {
        ArrayList<BigInteger> factors = MathContext.factor(new BigInteger("100000"));
        for (BigInteger b : factors)
            System.out.println(b);
//        l(MathContext.factorial(new BigInteger("300")));
        for (int i = 0; i < 10000; i++) {
            int finalI = i;
            MathContext.toBaseExponentPairs(i).stream()
                    .map(pair -> Operation.exp(pair[0], pair[1]))
                    .reduce(Operation::mult)
                    .ifPresent(o -> l(boldBlack(finalI + " -> ")
                            + lightBlue(o + " -> ")
                            + lightGreen(o.val())));
        }
    }


}
