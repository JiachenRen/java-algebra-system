package jas.core.operations;

import jas.core.JASException;
import jas.core.Mutating;
import jas.core.Nameable;
import jas.core.Node;
import jas.core.components.Fraction;
import jas.core.components.RawValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 16/05/2017.
 * Abstract parent of Binary and Unary Operation
 */
public abstract class Operation extends Node implements Nameable {
    private ArrayList<Node> operands;
    private boolean isOrdered = false;

    public Operation(ArrayList<Node> operands) {
        this.operands = operands;
    }

    public static Binary div(double a, double b) {
        return div(new RawValue(a), new RawValue(b));
    }

    public static Binary div(Node o1, Node o2) {
        return new Binary(o1.copy(), "/", o2.copy());
    }

    public static Binary mult(double a, double b) {
        return mult(new RawValue(a), new RawValue(b));
    }

    public static Binary mult(Node o1, Node o2) {
        return new Binary(o1.copy(), "*", o2.copy());
    }

    public static Binary mult(Node a, double b) {
        return mult(a, new RawValue(b));
    }

    public static Binary mult(double a, Node b) {
        return mult(new RawValue(a), b);
    }

    public static Binary add(Node o1, Node o2) {
        return new Binary(o1.copy(), "+", o2.copy());
    }

    public static Binary add(Node o1, double n) {
        return new Binary(o1.copy(), "+", new RawValue(n));
    }

    public static Binary add(double a, double b) {
        return new Binary(new RawValue(a), "+", new RawValue(b));
    }

    public static Binary sub(Node o1, Node o2) {
        return new Binary(o1.copy(), "-", o2.copy());
    }

    public static Binary exp(double a, double b) {
        return exp(new RawValue(a), new RawValue(b));
    }

    public static Binary exp(Node o1, Node o2) {
        return new Binary(o1.copy(), "^", o2.copy());
    }

    public static Binary exp(Node a, double b) {
        return exp(a, new RawValue(b));
    }

    public static Binary exp(double a, Node b) {
        return exp(new RawValue(a), b);
    }

    public static Binary sqrt(Node o) {
        return o.exp(new Fraction(1, 2));
    }

    static ArrayList<Node> wrap(Node... nodes) {
        ArrayList<Node> operands = new ArrayList<>();
        Collections.addAll(operands, nodes);
        return operands;
    }

    public abstract double eval(double x);

    /**
     * e.g. left hand of 2^x in a Binary is "2"
     * left hand of log<x> is "x"
     *
     * @return for Binary, the first arg is returned. For Unary, the only arg is returned.
     */
    public ArrayList<Node> getOperands() {
        return operands;
    }

    public Operation setOperands(ArrayList<Node> operands) {
        this.operands = operands;
        isOrdered = false;
        return this;
    }

    /**
     * updates the an operand in the list of operands with a new one.
     *
     * @param operand the new operand
     * @param idx     the index of the old operand to be replaced
     * @return this
     */
    public Operation setOperand(Node operand, int idx) {
        operands.set(idx, operand);
        isOrdered = false;
        return this;
    }

    /**
     * @param idx the index of the operand
     * @return operand at index idx
     */
    public Node getOperand(int idx) {
        return operands.get(idx);
    }

    public abstract Operation copy();

    /**
     * post operation: the operation itself is modified
     *
     * @return modified self.
     */
    @Mutating
    public Node simplify() {
        operands = operands.stream()
                .map(Node::simplify)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * basically reversing the effects of toAdditionalOnly and toExponentialForm
     * a*b^(-1) -> a/b,
     * a*(1/3) -> a/3,
     * a+(-1)*b -> a-b
     *
     * @return beautified version of the original
     */
    @Mutating
    public Node beautify() {
        operands = operands.stream()
                .map(Node::beautify)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * Note: modifies self.
     * Only delegates downward if it contains an operation.
     *
     * @return a new Node instance that is the addition only form of self.
     */
    @Mutating
    public Operation toAdditionOnly() {
        operands.forEach(Node::toAdditionOnly);
        return this;
    }

    public Node explicitNegativeForm() {
        Operation clone = this.copy();
        clone.setOperands(this.operands.stream()
                .map(Node::explicitNegativeForm)
                .collect(Collectors.toCollection(ArrayList::new)));
        return clone;
    }


    /**
     * Note: modifies self
     *
     * @return exponential form of self
     */
    @Mutating
    public Node toExponentialForm() {
        operands.forEach(Node::toExponentialForm);
        return this;
    }

    public int numNodes() {
        Optional<Integer> nodes = operands.stream()
                .map(Node::numNodes)
                .reduce((a, b) -> a + b);
        if (!nodes.isPresent()) throw new JASException("empty nodes");
        return 1 + nodes.get();
    }

    public int levelOf(Node o) {
        if (this.equals(o)) return 0;
        int minDepth = -1;
        for (Node operand : operands) {
            int lev = operand.levelOf(o);
            minDepth = lev > minDepth ? lev : minDepth;
        }
        if (minDepth == -1) return -1;
        return minDepth + 1;
    }

    @Mutating
    public Node expand() {
        operands = operands.stream()
                .map(Node::expand)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    public boolean isUndefined() {
        for (Node operand : operands) {
            if (operand.isUndefined())
                return true;
        }
        return false;
    }

    public Node replace(Node o, Node r) {
        if (this.equals(o)) return r;
        Operation clone = this.copy();
        clone.setOperands(operands.stream()
                .map(op -> op.replace(o, r))
                .collect(Collectors.toCollection(ArrayList::new)));
        return clone;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public int complexity() {
        return operands.stream()
                .map(Node::complexity)
                .reduce((a, b) -> a + b).get() + 1;
    }

    public boolean equals(Node other) {
        if (!(other instanceof Operation)) return false;
        Operation op = ((Operation) other);
        if (op.getOperands().size() != this.getOperands().size()) return false;
        ArrayList<Node> operands1 = op.getOperands();
        for (int i = 0; i < operands1.size(); i++) {
            Node node = operands1.get(i);
            if (!node.equals(getOperand(i)))
                return false;
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    void setOrdered(boolean b) {
        isOrdered = b;
    }

    boolean isOrdered() {
        return isOrdered;
    }

    /**
     * orders the expression according to lexicographic order. This saves some computational resource when trying to decide
     * whether a node within the binary tree the the canonical form of another.
     * e.g. c*b*3*a would be reordered to something like 3*a*c*b
     */
    @Mutating
    public void order() {
        operands.forEach(o -> {
            o.order();
            if (o instanceof Operation)
                ((Operation) o).isOrdered = true;
        });
        isOrdered = true;
    }
}
