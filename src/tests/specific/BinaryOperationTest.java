package tests.specific;

import jmc.cas.Compiler;
import jmc.cas.components.RawValue;
import jmc.cas.operations.BinaryOperation;
import jmc.cas.operations.Operation;
import tests.TestPrint;

/**
 * Created by Jiachen on 3/7/18.
 * BinaryOperation Test
 */
public class BinaryOperationTest {
    public static void main(String args[]) {
        BinaryOperation binOp = new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE);
        l(binOp.getRight(), binOp.getLeft());
        binOp.setRight(RawValue.ONE);
        BinaryOperation.define("&", 3, (a, b) -> a + b);
        l(BinaryOperation.binaryOperations(), BinaryOperation.binaryOperations(3));
        l(BinaryOperation.getPriority("&"), BinaryOperation.getPriority("+"));
        l(new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE).getPriority());
        l(new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE).flattened());
        l(binOp.is("*"));
        l(Operation.mult(3, 5));
        l(Operation.exp(Math.random(), new RawValue(3)));
        l(Operation.exp(3, Math.random()));
        l(Compiler.compile("x+x*a").simplify());
        ((BinaryOperation) Compiler.compile("x^b")).flattened().forEach(TestPrint::l);
        l(Operation.div(17, 4).setOperand(binOp.getOperand(1), 1).setOperands(binOp.getOperands()));
        l(Operation.div(3, 4).setLeft(new RawValue(5)));
        l(((BinaryOperation) Compiler.compile("a+b")).isCommutative());
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
