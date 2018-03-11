package jmc.cas;

/**
 * Created by Jiachen on 16/05/2017.
 * Abstract parent of BinaryOperation and Unary Operation
 */
public abstract class Operation implements Operable {
    private Operable leftHand;

    public Operation(Operable leftHand) {
        this.leftHand = leftHand;
    }

    public abstract double eval(double x);

    /**
     * e.g. left hand of 2^x in a BinaryOperation is "2"
     * left hand of log<x> is "x"
     *
     * @return for BinaryOperation, the first arg is returned. For UnaryOperation, the only arg is returned.
     */
    public Operable getLeftHand() {
        return leftHand;
    }

    public abstract Operable toExponentialForm();

    /**
     * post operation: the operation itself is modified
     *
     * @return modified self.
     */
    public abstract Operable simplify();

    public Operable setLeftHand(Operable operable) {
        this.leftHand = operable;
        return this;
    }

    public abstract Operation clone();

    public abstract Operable toAdditionOnly();

    public static Operation div(Operable o1, Operable o2) {
        return new BinaryOperation(o1.clone(), "/", o2.clone());
    }

    public static Operation mult(Operable o1, Operable o2) {
        return new BinaryOperation(o1.clone(), "*", o2.clone());
    }

    public static Operation add(Operable o1, Operable o2) {
        return new BinaryOperation(o1.clone(), "+", o2.clone());
    }

    public static Operation sub(Operable o1, Operable o2) {
        return new BinaryOperation(o1.clone(), "-", o2.clone());
    }

    public static Operation exp(Operable o1, Operable o2) {
        return new BinaryOperation(o1.clone(), "^", o2.clone());
    }
}
