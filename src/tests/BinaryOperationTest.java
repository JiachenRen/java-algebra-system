package tests;

import jmc.cas.BinaryOperation;
import jmc.cas.Expression;
import jmc.cas.Operation;
import jmc.cas.RawValue;

/**
 * Created by Jiachen on 3/7/18.
 * BinaryOperation Test
 */
public class BinaryOperationTest {
    public static void main(String args[]) {
        BinaryOperation binOp = new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE);
        l(binOp.getRight(), binOp.getOperand());
        binOp.setRight(RawValue.ONE);
        BinaryOperation.define("&", 3, (a, b) -> a + b);
        l(BinaryOperation.binaryOperations(), BinaryOperation.binaryOperations(3));
        l(BinaryOperation.getPriority("&"), BinaryOperation.getPriority("+"));
        l(new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE).getPriority());
        l(new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE).flattened());
        l(binOp.is("*"));
        l(Operation.mult(3, 5));
        l(Operation.exp(Math.random(), new RawValue(3)));
        l(Operation.exp(new RawValue(3), Math.random()));
        l(Expression.interpret("x+x*a").simplify());
        ((BinaryOperation) Expression.interpret("x^b")).flattened().forEach(TestPrint::l);
        Operation.div(17,4);
        l(Operation.div(3,4).setLeft(new RawValue(5)));
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
