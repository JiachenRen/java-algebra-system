package jmc;

/**
 * Created by Jiachen on 16/05/2017.
 */
public abstract class Operation implements Operable {
    private Operable leftHand;

    public Operation(Operable leftHand) {
        this.leftHand = leftHand;
    }

    public abstract double eval(double x);

    public Operable getLeftHand() {
        return leftHand;
    }

    public abstract void toExponentialForm();

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
