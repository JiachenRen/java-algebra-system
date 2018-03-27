package tests.cas;

import jas.core.Compiler;
import jas.core.Node;
import jas.core.components.Constants;
import jas.core.operations.Binary;
import jas.core.operations.Unary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static jas.utils.ColorFormatter.*;
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
        Binary.define("%", 2, (a, b) -> a % b);
        l(Compiler.compile("x % 3").eval(5));

        Unary.define("digits", x -> Integer.toString((int) x).length());
        l(Compiler.compile("digits(x)^2").eval(1234));

        Constants.define("seed", Math::random);
        System.out.println(Compiler.compile("seed*2-1").val());

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Node> nodes;
        nodes = (ArrayList<Node>) raw.stream().map(Compiler::compile).collect(Collectors.toList());
        nodes.forEach(node -> l(node
                + boldBlack("\t->\t")
                + lightGreen(node.copy().simplify().toString())
                + boldBlack("\t->\t")
                + node.copy().simplify().beautify()));
    }
}
