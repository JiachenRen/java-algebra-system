package tests.specific;

import jas.core.Compiler;
import jas.core.Node;
import jas.core.components.RawValue;
import jas.core.components.Variable;
import jas.core.operations.Unary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static jas.utils.ColorFormatter.boldBlack;
import static jas.utils.ColorFormatter.lightGreen;
import static tests.TestPrint.*;

/**
 * Created by Jiachen on 3/8/18.
 * Unary Test
 */
public class UnaryOperationTest {
    private static String ops[] = new String[]{
            "tan(3pi/2)",
            "tan(3pi/2*x)",
            "atan(tan(x))", //throw warnings after simplification!
            "asin(sin(x))",
            "acos(cos(2*ln(x)))",
            "atan(tan(5pi/2))",
            "tan(-3pi/2)",
            "sec(-3pi/2)",
            "cot(pi)",
            "cot(0)",
            "csc(0)",
            "csc(100pi)",
            "csc(pi/2)",
            "log(10)",
            "log(100)",
            "ln(225)", //should it be changed to 2ln(15)?
            "ln(e)",
            "int(3.5)",
            "ln(e^3)",
            "ln(e^3+x)",
            "ln(e^3*x)",
            "ln(x)",
            "ln(e^(3*x))",
            "cos(pi)",
            "sin(pi)",
            "tan(pi)"

    };

    public static void main(String args[]) {
        l(Compiler.compile("tan(3pi/2)").isUndefined());

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Node> nodes;
        nodes = (ArrayList<Node>) raw.stream().map(Compiler::compile).collect(Collectors.toList());
        nodes.forEach(node -> l(node
                + boldBlack("\t->\t")
                + lightGreen(node.copy().simplify().toString())
                + boldBlack("\t->\t")
                + node.copy().simplify().beautify()));

        l(Math.cos(Math.PI / 2)); // this is why we need CAS!
        l(RawValue.isInteger(3.0));

        l(Compiler.compile("ln(x)").isUndefined());

        Unary.registeredOperations().forEach(o -> l(o.getName()));

        Unary.define("$", "x*1000");
        Unary u = new Unary(new Variable("x"), "log");
        l(u.getOperand());
        u.setOperand(new Variable("a"));
        l(u.getOperand());
        l(Compiler.compile("$(x)").eval(3));
    }


}
