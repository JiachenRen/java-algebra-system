package tests.specific;

import jas.core.Compiler;
import jas.core.Node;
import jas.core.operations.Operation;
import jas.core.components.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/7/18.
 * Node Test
 */
public class NodeTest {
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
            "x^(x*3)^(1/3)"
    };

    public static void main(String args[]) {
        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Node> nodes;
        nodes = (ArrayList<Node>) raw.stream().map(Compiler::compile).collect(Collectors.toList());
        l(Node.contains(nodes, Compiler.compile("0-x")));
        l(Node.commonTerms(Compiler.compile("x*2*b*a*b*x^2/x"), Compiler.compile("b*x^2*b*x*1")));
        l(Node.commonTerms(Compiler.compile("x"), Compiler.compile("b*x^2*b*x*1")));
        l(Node.commonTerms(Compiler.compile("x"), Compiler.compile("b")));
        l(new Variable("x").div(new Variable("p")));
        l(new Variable("x").add(new Variable("p")));
        l(new Variable("x").sub(new Variable("p")));
        l(new Variable("x").mult(new Variable("p")));
        l(new Variable("x").exp(new Variable("p")));
        l(new Variable("x").negate());
        l(Compiler.compile("(-1)*((2x^2)*a^(-1))").simplify().beautify());
        Node o = Operation.div(12, 3);
        l(o.div(Math.random()).simplify());
        l(Operation.div(new Variable("x"), new Variable("x")));
        l(o.isNaN());
    }
}
