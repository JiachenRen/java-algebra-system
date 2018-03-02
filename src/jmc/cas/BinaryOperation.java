package jmc.cas;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jiachen on 16/05/2017.
 * added the ability to expand parenthesis and constant expressions. Accomplished May 20th.
 * add the ability to mark unavailable domain.
 * code refactored May 20th. Added ADT for operations.
 */
public class BinaryOperation extends Operation {
    private Operable rightHand;
    private boolean omitParenthesis;
    private RegisteredBinaryOperation operation;

    public BinaryOperation(Operable leftHand, String operation, Operable rightHand) {
        this(leftHand, RegisteredBinaryOperation.extract(operation), rightHand);
    }

    public BinaryOperation(Operable leftHand, RegisteredBinaryOperation operation, Operable rightHand) {
        super(leftHand);
        this.operation = operation;
        this.rightHand = rightHand;
        omitParenthesis = true;
        processParentheticalNotation(getLeftHand(), false);
        processParentheticalNotation(this.rightHand, true);
    }

    /**
     * Remove unnecessary parenthesis
     *
     * @since May 19th, 2017
     */
    private void processParentheticalNotation(Operable operable, boolean isRightHand) {
        if (operable instanceof BinaryOperation) {
            BinaryOperation op = ((BinaryOperation) operable);
            if (op.getPriority() < this.getPriority()) {
                op.setOmitParenthesis(true);
            } else if (op.getPriority() == this.getPriority()) {
                op.setOmitParenthesis(op.operation.equals(operation) || !isRightHand);
            } else {
                op.setOmitParenthesis(false);
            }
        }
    }

    /**
     * @param name      name of the new binary operation
     * @param priority  1 is the most prioritized
     * @param evaluable (a,b) -> a [operation] b
     */
    public static void define(String name, int priority, BinEvaluable evaluable) {
        RegisteredBinaryOperation.define(name, priority, evaluable);
    }

    public double eval(double x) {
        double leftVal = getLeftHand().eval(x);
        double rightVal = rightHand.eval(x);
        return operation.eval(leftVal, rightVal);
    }

    public String toString() {
        String temp = getLeftHand().toString() + operation.name + rightHand.toString();
        return omitParenthesis ? temp : "(" + temp + ")";
    }

    void setOmitParenthesis(boolean temp) {
        omitParenthesis = temp;
    }

    public int getPriority() {
        return operation.priority;
    }

    public static int getPriority(String operation) {
        return RegisteredBinaryOperation.extract(operation).priority;
    }

    /**
     * @return a complete list of binary operations (with corresponding priority)
     */
    public static String binaryOperations(int priority) {
        return RegisteredBinaryOperation.listAsString(priority);
    }

    public static String binaryOperations() {
        return binaryOperations(1) + binaryOperations(2) + binaryOperations(3);
    }

    /**
     * for example, "(x-4)(x-5)/(x-3/(x-6))" would first transformed into
     * "(x-4)(x-5)(x-3/(x-6))^-1", and then recursively reduced to
     * "(x-4)(x-5)(x-3*(x-6)^-1)^-1"
     * basic CAS capabilities. Implementation began: May 19th.
     * Note: does not change self.
     */
    @Override
    public Operable toExponentialForm() {
        if (getLeftHand() instanceof Operation) ((Operation) getLeftHand()).toExponentialForm();
        if (rightHand instanceof Operation) ((Operation) rightHand).toExponentialForm();
        if (!this.operation.equals("/")) return this;
        if (rightHand.equals(new RawValue(0))) throw new ArithmeticException("division by zero: jmc");
        if (rightHand instanceof BinaryOperation && ((BinaryOperation) rightHand).operation.equals("*")) {
            BinaryOperation enclosed = ((BinaryOperation) rightHand);
            enclosed.setLeftHand(new BinaryOperation(enclosed.getLeftHand(), "^", new RawValue(-1)));
            enclosed.rightHand = new BinaryOperation(enclosed.rightHand, "^", new RawValue(-1));
        } else this.rightHand = new BinaryOperation(rightHand, "^", new RawValue(-1));
        operation = RegisteredBinaryOperation.extract("*");
        processParentheticalNotation(getLeftHand(), false);
        processParentheticalNotation(rightHand, true);
        return this;
    }

    /**
     * TODO: should perform all simplifications using exponential notation
     *
     * @return
     */
    public Operable simplify() {
        if (getLeftHand() instanceof RawValue && rightHand instanceof RawValue) {
            System.out.print((char) 27 + "[1m" + "primitive calc:" + (char) 27 + "[0m");
            System.out.println(Expression.colorMathSymbols(this.toString()));
            RawValue calculated = new RawValue(operation.eval(((RawValue) getLeftHand()).doubleValue(), ((RawValue) rightHand).doubleValue()));
            //TODO: add a fraction class.
            return calculated;
        }
        if (getLeftHand() instanceof Operation) {
            setLeftHand(((Operation) getLeftHand()).simplify());
        }
        if (rightHand instanceof Operation) {
            rightHand = ((Operation) rightHand).simplify();
        }
        return this;
    }

    @Override
    public BinaryOperation replicate() {
        return new BinaryOperation(getLeftHand().replicate(), operation, rightHand.replicate());
    }

    public boolean equals(Operable other) {
        return other instanceof BinaryOperation && ((BinaryOperation) other).getLeftHand().equals(this.getLeftHand()) && ((BinaryOperation) other).rightHand.equals(this.rightHand) && this.operation.equals(((BinaryOperation) other).operation);
    }

    public Operable toAdditionOnly() {
        if (getLeftHand() instanceof Operation) this.setLeftHand(((Operation) getLeftHand()).toAdditionOnly());
        this.rightHand = rightHand instanceof Operation ? ((Operation) rightHand).toAdditionOnly() : rightHand;
        if (operation.name.equals("-")) {
            operation = RegisteredBinaryOperation.extract("+");
            rightHand = UnaryOperation.negate(rightHand);
        }
        return this;
    }

    public Operable plugIn(Variable var, Operable replacement) {
        if (this.getLeftHand().equals(var))
            this.setLeftHand(replacement);
        else this.getLeftHand().plugIn(var, replacement);
        if (this.rightHand.equals(var))
            this.setRightHand(replacement);
        else this.getRightHand().plugIn(var, replacement);
        return this;
    }

    public Operable getRightHand() {
        return rightHand;
    }

    public void setRightHand(Operable operable) {
        this.rightHand = operable;
    }

    public interface BinEvaluable {
        double eval(double a, double b);
    }

    private static class RegisteredBinaryOperation implements BinEvaluable {
        private static ArrayList<RegisteredBinaryOperation> registeredBinOps;
        private BinEvaluable binEvaluable;
        private String name;
        private int priority; //1 is the most prioritized

        static {
            registeredBinOps = new ArrayList<>();
            define("+", 3, (a, b) -> a + b);
            define("-", 3, (a, b) -> a - b);
            define("*", 2, (a, b) -> a * b);
            define("/", 2, (a, b) -> a / b);
            define("^", 1, Math::pow);
            System.out.println("# reserved binary operations declared");
        }

        private RegisteredBinaryOperation(String name, int priority, BinEvaluable evaluable) {
            this.name = name;
            this.binEvaluable = evaluable;
            this.priority = priority;
        }

        public double eval(double a, double b) {
            return binEvaluable.eval(a, b);
        }

        public static void define(String name, int priority, BinEvaluable evaluable) {
            for (int i = 0; i < registeredBinOps.size(); i++) {
                RegisteredBinaryOperation function = registeredBinOps.get(i);
                if (function.name.equals(name))
                    registeredBinOps.remove(i);
            }
            registeredBinOps.add(new RegisteredBinaryOperation(name, priority, evaluable));
        }

        private static String listAsString(int priority) {
            String incrementer = "";
            for (RegisteredBinaryOperation operation : registeredBinOps) {
                if (operation.priority == priority)
                    incrementer += operation.name;
            }
            return incrementer;
        }

        private static RegisteredBinaryOperation extract(String name) {
            for (RegisteredBinaryOperation binaryOperation : registeredBinOps)
                if (binaryOperation.name.equals(name))
                    return binaryOperation;
            throw new RuntimeException("undefined binary operator \"" + name + "\"");
        }

        public boolean equals(RegisteredBinaryOperation other) {
            return this.name.equals(other.name);
        }

        public boolean equals(String s) {
            return this.name.equals(s);
        }
    }
}
