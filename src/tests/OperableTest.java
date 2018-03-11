package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.cas.Operation;
import jmc.cas.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/7/18.
 * Operable Test
 */
public class OperableTest {
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
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Expression::interpret).collect(Collectors.toList());
        l(Operable.contains(operables, Expression.interpret("0-x")));
        l(Operable.commonTerms(Expression.interpret("x*2*b*a*b*x^2/x"), Expression.interpret("b*x^2*b*x*1")));
        l(Operable.commonTerms(Expression.interpret("x"), Expression.interpret("b*x^2*b*x*1")));
        l(Operable.commonTerms(Expression.interpret("x"), Expression.interpret("b")));
        l(Operation.div(new Variable("x"), new Variable("p")));
        l(Operation.add(new Variable("x"), new Variable("p")));
        l(Operation.sub(new Variable("x"), new Variable("p")));
        l(Operation.mult(new Variable("x"), new Variable("p")));
        l(Operation.exp(new Variable("x"), new Variable("p")));
        l(new Variable("x").negate());
        l(Expression.interpret("(-1)*((2x^2)*a^(-1))").simplify().beautify());
    }
}
