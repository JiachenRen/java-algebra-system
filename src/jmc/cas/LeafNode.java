package jmc.cas;

/**
 * Created by Jiachen on 3/10/18.
 * Leaf Node: super class of Variable and RawValue
 */
abstract class LeafNode extends Operable implements BinLeafNode {
    public abstract String toString();

    public abstract boolean isUndefined();

    public abstract Operable copy();

    public abstract Operable explicitNegativeForm();

//    public abstract Operable firstDerivative();

    public int levelOf(Operable o) {
        return this.equals(o) ? 0 : -1;
    }

    public int numNodes() {
        return 1;
    }

    public abstract int complexity();

    public Operable beautify() {
        return this;
    }

    public Operable toAdditionOnly() {
        return this;
    }

    public Operable toExponentialForm() {
        return this;
    }

    public Operable expand() {
        return this;
    }

    public Operable replace(Operable o, Operable r) {
        return this.equals(o) ? r : this;
    }

    public Operable negate() {
        return Operation.mult(RawValue.ONE.negate(), this.copy());
    }
}
