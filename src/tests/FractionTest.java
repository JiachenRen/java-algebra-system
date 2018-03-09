package tests;

import jmc.cas.Expression;
import jmc.cas.Fraction;
import jmc.cas.Operable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

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
        Fraction f = new Fraction(4050800, 1);
        l(Fraction.extractRoot(350003000, 2));
        l(Fraction.getFactors(35000));

        Fraction f1 = new Fraction(3, 4);
        Fraction f2 = new Fraction(4, 3);
        Operable o = f1.exp(f2);
        l(o);

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Expression::interpret).collect(Collectors.toList());
        operables.forEach(operable -> l(operable + " -> "+operable.simplify()));

//        l(Fraction.extractRoot(-2,3));
    }

}
