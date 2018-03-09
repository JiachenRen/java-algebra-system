package tests;

import jmc.cas.BinaryOperation;
import jmc.cas.RawValue;

/**
 * Created by Jiachen on 3/7/18.
 * BinaryOperation Test
 */
public class BinaryOperationTest {
    public static void main(String args[]) {
        BinaryOperation binOp = new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE);
        l(binOp.getRightHand(), binOp.getLeftHand());
        binOp.setRightHand(RawValue.ONE);
        BinaryOperation.define("&", 3, (a,b) -> a + b);
        l(BinaryOperation.binaryOperations(), BinaryOperation.binaryOperations(3));
        l(BinaryOperation.getPriority("&"),BinaryOperation.getPriority("+"));
        l(new BinaryOperation(RawValue.ZERO,"*", RawValue.ONE).getPriority());
        l(new BinaryOperation(RawValue.ZERO, "*", RawValue.ONE).flattened());
        l(binOp.is("*"));

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
