package tests.cas;

import jmc.cas.Compiler;
import jmc.cas.components.Constants;
import jmc.cas.Operable;
import jmc.cas.operations.BinaryOperation;
import jmc.cas.operations.UnaryOperation;

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
            "a/3*2.5/n*b^2.5*a/4",
            "ln(-5)*x",
            "ln(5)*x % 3",
            "x^(-1)",
            "2.5/n",
            "1/4*(3/x)",
            "-a-b",
            "2x^-1",
            "a-b+c-2d^-2+3",
            "ln(sin(x*a + x*b))"
    };

    public static void main(String args[]) {
        BinaryOperation.define("%", 2, (a, b) -> a % b);
        l(Compiler.compile("x % 3").eval(5));

        UnaryOperation.define("digits", x -> Integer.toString((int) x).length());
        l(Compiler.compile("digits(x)^2").eval(1234));

        Constants.define("seed", Math::random);
        System.out.println(Compiler.compile("seed*2-1").val());

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Compiler::compile).collect(Collectors.toList());
        operables.forEach(operable -> l(operable
                + boldBlack("\t->\t")
                + lightGreen(operable.copy().simplify().toString())
                + boldBlack("\t->\t")
                + operable.copy().simplify().beautify()));
    }
}
