package jmc.cas;

/**
 * Created by Jiachen on 3/10/18.
 * Leaf Node: super class of Variable and RawValue
 */
public abstract class LeafNode extends Operable implements BinLeafNode {
    public abstract Operable copy();

    public int levelOf(Operable o) {
        return this.equals(o) ? 0 : -1;
    }

    public abstract String toString();

    public int numNodes() {
        return 1;
    }

//    public abstract Operable firstDerivative();

    public abstract int complexity();

    public Operable beautify() {
        return this;
    }

    public abstract Operable explicitNegativeForm();

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

    public abstract boolean isUndefined();

    /**
     * LeafNode, nothing could be done for reordering.
     */
    public void order() {
    }

}
