package jmc;

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

    public void setLeftHand(Operable operable) {
        this.leftHand = operable;
    }

    public abstract Operation replicate();

    public boolean simplifiable() {
        Operable duplicate = this.replicate().simplify();
        return !duplicate.equals(this);
    }

    public abstract Operable toAdditionOnly();
}
