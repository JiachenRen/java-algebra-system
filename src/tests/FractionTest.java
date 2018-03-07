package tests;

import jmc.cas.Fraction;
import jmc.cas.Operable;

/**
 * Created by Jiachen on 3/6/18.
 * Fraction Test class
 */
public class FractionTest {
    public static void main(String args[]) {
        Fraction f = new Fraction(4050800, 1);
        l(Fraction.extractRoot(350003000,2));
        l(Fraction.getFactors(35000));

        Fraction f1 = new Fraction(3,4);
        Fraction f2 = new Fraction(4, 3);
        Operable o = f1.exp(f2);
        l(o);
    }

    private static void l(Object... objects) {
        for (Object o : objects) {
            l(o);
        }
    }

    private static void l(Object o) {
        System.out.println(o);
    }
}
