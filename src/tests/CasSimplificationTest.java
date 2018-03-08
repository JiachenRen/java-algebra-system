package tests;

import jmc.cas.BinaryOperation;
import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.cas.RawValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static jmc.utils.ColorFormatter.*;
import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/7/18.
 * Simplification Test
 */
public class CasSimplificationTest {
    private static String ops[] = new String[]{
            "1*x",
            "0*x",
            "0^x",
            "x^0",
            "0^0",
            "1^0",
            "0/0",
            "x/0",
            "x/x",
            "x-0",
            "0+0",
            "0/1",
            "x+1",
            "x-1",
            "1^x",
            "0^1",
            "x^3^a",
            "(a*b)^3",
            "x^(x*3)^(1/3)",
            "a*2*x^2 - a*x^2",
            "a*2*b*x^2 - a*x^2",
            "a^3*a",
            "(2/3)^(3/4)",
            "a/3+2.5/n+b^2.5",
            "ln(-5)*x",
            "x^(-1)",
            "2.5/n",
            "1/4*(3/x)",
            "-a-b",
            "2x^-1",
            "a-b+c-2d^-2+3",
            "ln(sin(x*a + x*b))"
    };

    public static void main(String args[]) {
        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Expression::interpret).collect(Collectors.toList());
        operables.forEach(operable -> l(operable
                + boldBlack("\t->\t")
                + lightGreen(operable.clone().simplify().toString())
                + boldBlack("\t->\t")
                + operable.clone().simplify().beautify()));
    }
}
