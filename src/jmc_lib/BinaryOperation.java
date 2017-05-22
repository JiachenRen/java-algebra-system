package jmc_lib;

import java.util.ArrayList;

/**
 * Created by Jiachen on 16/05/2017.
 * added the ability to simplify parenthesis and constant expressions. Accomplished May 20th.
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
        processParentheticalNotation(leftHand, false);
        processParentheticalNotation(rightHand, true);
    }

    /**
     * breakthrough.
     *
     * @since May 19th
     */
    private void processParentheticalNotation(Operable operable, boolean isRightHand) {
        if (operable instanceof BinaryOperation) {
            BinaryOperation other = ((BinaryOperation) operable);
            if (other.getPriority() < this.getPriority()) {
                other.setOmitParenthesis(true);
            } else if (other.getPriority() == this.getPriority()) {
                other.setOmitParenthesis(other.operation.equals(operation) || !isRightHand);
            } else {
                other.setOmitParenthesis(false);
            }
        }
    }

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

    public void setOmitParenthesis(boolean temp) {
        omitParenthesis = temp;
    }

    public int getPriority() {
        return operation.priority;
    }

    public static int getPriority(String operation) {
        return RegisteredBinaryOperation.extract(operation).priority;
    }

    /**
     * @return the list of operations (with corresponding symbols)
     */
    public static String binaryOperations(int priority) {
        return RegisteredBinaryOperation.list(priority);
    }

    public static String binaryOperations() {
        return binaryOperations(1) + binaryOperations(2) + binaryOperations(3);
    }

    public static boolean containsBinaryOperation(String exp) {
        for (int i = 0; i < exp.length(); i++) {
            if (binaryOperations().contains(exp.substring(i, i + 1)))
                return true;
        }
        return false;
    }

    /**
     * for example, "(x-4)(x-5)/(x-3/(x-6))" would first transformed into
     * "(x-4)(x-5)(x-3/(x-6))^-1", and then recursively reduced to
     * "(x-4)(x-5)(x-3*(x-6)^-1)^-1"
     * basic CAS capabilities. Implementation began: May 19th.
     */
    @Override
    public void toExponentialForm() {
        if (getLeftHand() instanceof Operation) ((Operation) getLeftHand()).toExponentialForm();
        if (rightHand instanceof Operation) ((Operation) rightHand).toExponentialForm();
        if (!this.operation.equals("/")) return;
        if (rightHand instanceof BinaryOperation && ((BinaryOperation) rightHand).operation.equals("*")) {
            BinaryOperation enclosed = ((BinaryOperation) rightHand);
            enclosed.setLeftHand(new BinaryOperation(enclosed.getLeftHand(), "^", new Raw(-1)));
            enclosed.rightHand = new BinaryOperation(enclosed.rightHand, "^", new Raw(-1));
        } else this.rightHand = new BinaryOperation(rightHand, "^", new Raw(-1));
        operation = RegisteredBinaryOperation.extract("*");
        processParentheticalNotation(getLeftHand(), false);
        processParentheticalNotation(rightHand, true);
    }

    /**
     * TODO
     *
     * @return
     */
    public Operable simplify() {
        /*
        if (getLeftHand() instanceof Operation){
            ((Operation) getLeftHand()).simplify();
        }
        if (rightHand instanceof Operation){
            ((Operation) rightHand).simplify();
        }
        if (getLeftHand() instanceof Variable && rightHand instanceof Variable){
            if (((Variable) getLeftHand()).getName().equals(((Variable) rightHand).getName())){
                setLeftHand(new Variable(((Variable) getLeftHand()).getName()));
                operation = "^";
                rightHand = new Raw(2);
            }
        }
        if (getLeftHand() instanceof Raw && rightHand instanceof Raw){

        }
        */
        return null;
    }

    @Override
    public BinaryOperation replicate() {
        return new BinaryOperation(getLeftHand().replicate(), operation, rightHand.replicate());
    }

    public interface BinEvaluable {
        double eval(double a, double b);
    }

    private static class RegisteredBinaryOperation implements BinEvaluable {
        private static ArrayList<RegisteredBinaryOperation> reservedBinOps;
        private BinEvaluable binEvaluable;
        private String name;
        private int priority; //1 being the most prioritized

        static {
            reservedBinOps = new ArrayList<>();
            define("+", 3, (a, b) -> a + b);
            define("-", 3, (a, b) -> a - b);
            define("*", 2, (a, b) -> a * b);
            define("/", 2, (a, b) -> a / b);
            define("^", 1, Math::pow);
            System.out.println("reserved binary operations declared");
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
            for (int i = 0; i < reservedBinOps.size(); i++) {
                RegisteredBinaryOperation function = reservedBinOps.get(i);
                if (function.name.equals(name))
                    reservedBinOps.remove(i);
            }
            reservedBinOps.add(new RegisteredBinaryOperation(name, priority, evaluable));
        }

        private static String list(int priority) {
            String incrementer = "";
            for (RegisteredBinaryOperation operation : reservedBinOps) {
                if (operation.priority == priority)
                    incrementer += operation.name;
            }
            return incrementer;
        }

        private static RegisteredBinaryOperation extract(String name) {
            for (RegisteredBinaryOperation binaryOperation : reservedBinOps)
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
