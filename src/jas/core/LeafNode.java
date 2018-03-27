package jas.core;

/**
 * Created by Jiachen on 3/10/18.
 * Leaf Node: super class of Variable and RawValue
 */
public abstract class LeafNode extends Node implements BinLeafNode {
    public abstract Node copy();

    public int levelOf(Node o) {
        return this.equals(o) ? 0 : -1;
    }

    public abstract String toString();

    public int numNodes() {
        return 1;
    }

//    public abstract Node firstDerivative();

    public abstract int complexity();

    @Mutating
    public Node beautify() {
        return this;
    }

    public abstract Node explicitNegativeForm();

    @Mutating
    public Node toAdditionOnly() {
        return this;
    }

    @Mutating
    public Node toExponentialForm() {
        return this;
    }

    @Mutating
    public Node expand() {
        return this;
    }

    public Node replace(Node o, Node r) {
        return this.equals(o) ? r : this;
    }

    public abstract boolean isUndefined();

    /**
     * LeafNode, nothing could be done for reordering.
     */
    @Mutating
    public void order() {
    }

}
