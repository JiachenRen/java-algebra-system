package tests.specific;

import jas.MathContext;
import jas.core.Compiler;
import jas.core.Operable;
import jas.core.components.Fraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static jas.utils.ColorFormatter.*;
import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/6/18.
 * Fraction Test class
 */
public class FractionTest {
    private static String ops[] = new String[]{
            "3^-3",
            "(2/3)^(-1/3)",
            "3^(-1/3)",
            "(3/2)^(-2/3)",
            "7/8*2",
            "2/5+3/7",
            "2*5/7",
            "(3/4)*(5/7)",
            "3.5/4.7^2",
            "10^2.5"
    };

    public static void main(String args[]) {
        l(Fraction.extractRoot(BigInteger.valueOf(350003000), BigInteger.valueOf(2)));
        l(MathContext.getFactors(35000));

        Fraction f1 = new Fraction(3, 4);
        Fraction f2 = new Fraction(4, 3);
        f1.setNumerator(BigInteger.valueOf(100))
                .setDenominator(BigInteger.valueOf(3))
                .setDenominator(BigInteger.valueOf(5))
                .setNumerator(BigInteger.valueOf(7));
        l(f1.getNumerator(), f1.getDenominator());


        Operable o = f1.exp(f2);
        l(o);

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Compiler::compile).collect(Collectors.toList());
        operables.forEach(operable -> l(operable + " -> " + operable.copy().simplify() + ", "
                + boldBlack("status: ")
                + ((operable.val() - operable.copy().simplify().val()) < 1E-10 ? lightGreen("PASSED") : lightRed("FAILED"))));

//        l(Fraction.extractRoot(-2,3));
        l(((Fraction) Compiler.compile("3/4").simplify()).exp(-3));
    }

}
