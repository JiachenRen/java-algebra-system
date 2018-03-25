package jmc.cas.operations;

import jmc.cas.JMCException;
import jmc.cas.Nameable;
import jmc.cas.Operable;
import jmc.cas.components.Fraction;
import jmc.cas.components.RawValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 16/05/2017.
 * Abstract parent of BinaryOperation and Unary Operation
 */
public abstract class Operation extends Operable implements Nameable {
    private ArrayList<Operable> operands;
    private boolean isOrdered = false;

    public Operation(ArrayList<Operable> operands) {
        this.operands = operands;
    }

    public static BinaryOperation div(double a, double b) {
        return div(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation div(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "/", o2.copy());
    }

    public static BinaryOperation mult(double a, double b) {
        return mult(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation mult(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "*", o2.copy());
    }

    public static BinaryOperation mult(Operable a, double b) {
        return mult(a, new RawValue(b));
    }

    public static BinaryOperation mult(double a, Operable b) {
        return mult(new RawValue(a), b);
    }

    public static BinaryOperation add(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "+", o2.copy());
    }

    public static BinaryOperation add(Operable o1, double n) {
        return new BinaryOperation(o1.copy(), "+", new RawValue(n));
    }

    public static BinaryOperation add(double a, double b) {
        return new BinaryOperation(new RawValue(a), "+", new RawValue(b));
    }

    public static BinaryOperation sub(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "-", o2.copy());
    }

    public static BinaryOperation exp(double a, double b) {
        return exp(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation exp(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "^", o2.copy());
    }

    public static BinaryOperation exp(Operable a, double b) {
        return exp(a, new RawValue(b));
    }

    public static BinaryOperation exp(double a, Operable b) {
        return exp(new RawValue(a), b);
    }

    public static BinaryOperation sqrt(Operable o) {
        return o.exp(new Fraction(1, 2));
    }

    static ArrayList<Operable> wrap(Operable... operables) {
        ArrayList<Operable> operands = new ArrayList<>();
        Collections.addAll(operands, operables);
        return operands;
    }

    public abstract double eval(double x);

    /**
     * e.g. left hand of 2^x in a BinaryOperation is "2"
     * left hand of log<x> is "x"
     *
     * @return for BinaryOperation, the first arg is returned. For UnaryOperation, the only arg is returned.
     */
    public ArrayList<Operable> getOperands() {
        return operands;
    }

    public Operation setOperands(ArrayList<Operable> operands) {
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
    public Operation setOperand(Operable operand, int idx) {
        operands.set(idx, operand);
        isOrdered = false;
        return this;
    }

    /**
     * @param idx the index of the operand
     * @return operand at index idx
     */
    public Operable getOperand(int idx) {
        return operands.get(idx);
    }

    public abstract Operation copy();

    /**
     * post operation: the operation itself is modified
     *
     * @return modified self.
     */
    public Operable simplify() {
        operands = operands.stream()
                .map(Operable::simplify)
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
    public Operable beautify() {
        operands = operands.stream()
                .map(Operable::beautify)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * Note: modifies self.
     * Only delegates downward if it contains an operation.
     *
     * @return a new Operable instance that is the addition only form of self.
     */
    public Operation toAdditionOnly() {
        operands.forEach(Operable::toAdditionOnly);
        return this;
    }

    public Operable explicitNegativeForm() {
        Operation clone = this.copy();
        clone.setOperands(this.operands.stream()
                .map(Operable::explicitNegativeForm)
                .collect(Collectors.toCollection(ArrayList::new)));
        return clone;
    }


    /**
     * Note: modifies self
     *
     * @return exponential form of self
     */
    public Operable toExponentialForm() {
        operands.forEach(Operable::toExponentialForm);
        return this;
    }

    public int numNodes() {
        Optional<Integer> nodes = operands.stream()
                .map(Operable::numNodes)
                .reduce((a, b) -> a + b);
        if (!nodes.isPresent()) throw new JMCException("empty nodes");
        return 1 + nodes.get();
    }

    public int levelOf(Operable o) {
        if (this.equals(o)) return 0;
        int minDepth = -1;
        for (Operable operand : operands) {
            int lev = operand.levelOf(o);
            minDepth = lev > minDepth ? lev : minDepth;
        }
        if (minDepth == -1) return -1;
        return minDepth + 1;
    }

    public Operable expand() {
        operands = operands.stream()
                .map(Operable::expand)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    public boolean isUndefined() {
        for (Operable operand : operands) {
            if (operand.isUndefined())
                return true;
        }
        return false;
    }

    public Operable replace(Operable o, Operable r) {
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
                .map(Operable::complexity)
                .reduce((a, b) -> a + b).get() + 1;
    }

    public boolean equals(Operable other) {
        if (!(other instanceof Operation)) return false;
        Operation op = ((Operation) other);
        if (op.getOperands().size() != this.getOperands().size()) return false;
        ArrayList<Operable> operands1 = op.getOperands();
        for (int i = 0; i < operands1.size(); i++) {
            Operable operable = operands1.get(i);
            if (!operable.equals(getOperand(i)))
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
    public void order() {
        operands.forEach(o -> {
            o.order();
            if (o instanceof Operation)
                ((Operation) o).isOrdered = true;
        });
        isOrdered = true;
    }
}
