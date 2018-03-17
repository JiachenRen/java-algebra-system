package jmc.cas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 16/05/2017.
 * Abstract parent of BinaryOperation and Unary Operation
 */
public abstract class Operation extends Operable implements Nameable {
    private ArrayList<Operable> operands;

    public Operation(ArrayList<Operable> operands) {
        this.operands = operands;
    }

    public static BinaryOperation div(Number a, Number b) {
        return div(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation div(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "/", o2.copy());
    }

    public static BinaryOperation mult(Number a, Number b) {
        return mult(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation mult(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "*", o2.copy());
    }

    public static BinaryOperation mult(Operable a, Number b) {
        return mult(a, new RawValue(b));
    }

    public static BinaryOperation mult(Number a, Operable b) {
        return mult(new RawValue(a), b);
    }

    public static BinaryOperation add(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "+", o2.copy());
    }

    public static BinaryOperation add(Operable o1, Number n) {
        return new BinaryOperation(o1.copy(), "+", new RawValue(n));
    }

    public static BinaryOperation sub(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "-", o2.copy());
    }

    public static BinaryOperation exp(Number a, Number b) {
        return exp(new RawValue(a), new RawValue(b));
    }

    public static BinaryOperation exp(Operable o1, Operable o2) {
        return new BinaryOperation(o1.copy(), "^", o2.copy());
    }

    public static BinaryOperation exp(Operable a, Number b) {
        return exp(a, new RawValue(b));
    }

    public static BinaryOperation exp(Number a, Operable b) {
        return exp(new RawValue(a), b);
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
        return this;
    }

    /**
     * @param idx the index of the operand
     * @return operand at index idx
     */
    public Operable getOperand(int idx) {
        return operands.get(idx);
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

    public abstract Operation copy();

    public boolean isUndefined() {
        for (Operable operand : operands) {
            if (operand.isUndefined())
                return true;
        }
        return false;
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
}
