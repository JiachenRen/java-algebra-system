package jmc.cas;

/**
 * Created by Jiachen on 3/10/18.
 * Leaf Node: super class of Variable and RawValue
 */
abstract class LeafNode implements BinLeafNode, Operable {
    public abstract String toString();

    public abstract boolean isUndefined();

    public abstract Operable copy();

    public abstract Operable explicitNegativeForm();

    public int levelOf(Operable o) {
        return this.equals(o) ? 0 : -1;
    }

    public int numNodes() {
        return 1;
    }

    public Operable beautify() {
        return this;
    }

    public Operable replace(Operable o, Operable r) {
        return this.equals(o) ? r : this;
    }

    public Operable negate() {
        return Operation.mult(RawValue.ONE.negate(), this.copy());
    }
}
