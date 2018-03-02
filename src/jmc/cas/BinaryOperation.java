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
        super(leftHand.replicate());
        this.operation = operation;
        this.rightHand = rightHand.replicate();
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
        Operation newInstance = this.replicate();
        if (getLeftHand() instanceof Operation) {
            newInstance.setLeftHand(((Operation) getLeftHand()).toExponentialForm());
        }
        if (rightHand instanceof Operation) {
            ((BinaryOperation) newInstance).rightHand = (((Operation) rightHand).toExponentialForm());
        }
        if (!this.operation.equals("/")) return newInstance;
        BinaryOperation binOp = (BinaryOperation) newInstance;
        if (binOp.rightHand.equals(new RawValue(0))) throw new ArithmeticException("division by zero: jmc");
        if (binOp.rightHand instanceof BinaryOperation && ((BinaryOperation) binOp.rightHand).operation.equals("*")) {
            BinaryOperation enclosed = ((BinaryOperation) binOp.rightHand);
            enclosed.setLeftHand(new BinaryOperation(enclosed.getLeftHand(), "^", new RawValue(-1)));
            enclosed.rightHand = new BinaryOperation(enclosed.rightHand, "^", new RawValue(-1));
        } else {
            binOp.rightHand = new BinaryOperation(binOp.rightHand, "^", new RawValue(-1));
        }
        binOp.operation = RegisteredBinaryOperation.extract("*");
        processParentheticalNotation(binOp.getLeftHand(), false);
        processParentheticalNotation(binOp.rightHand, true);
        return binOp;
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

        //process the exponential operator separately, because it is unique!
        if (this.operation.equals("^")) {
            if (this.rightHand.equals(new RawValue(0)))
                return new RawValue(1);
            else if (this.rightHand.equals(new RawValue(1)))
                return getLeftHand();
            else {
                //should (x(x+3))^3 be expanded to x^3*(x+3)^3?
                //should (x+1)^3 be expanded to (x+1)(x+1)(x+1)?
                return new BinaryOperation(getLeftHand(), "^", rightHand); //temporary solution, does not expand as intended
            }
        }

        if (this.operation.equals("*") && (this.getLeftHand().equals(new RawValue(1)) || this.rightHand.equals(new RawValue(1)))) {
            //System.out.println(this); May 26 bug found!
            return getLeftHand().equals(new RawValue(1)) ? rightHand : getLeftHand();
        }

        if (getLeftHand().equals(new RawValue(0)) || rightHand.equals(new RawValue(0))) {
            if (operation.name.equals("*")) return new RawValue(0);
            if (getLeftHand().equals(new RawValue(0)) && rightHand.equals(new RawValue(0))) {
                return new RawValue(0);
            } else if (getLeftHand().equals(new RawValue(0))) switch (operation.name) {
                case "+":
                    return rightHand;
                case "*":
                    return new RawValue(0);
            }
            else switch (operation.name) {
                    case "*":
                        return new RawValue(0);
                    case "+":
                        return getLeftHand(); //add expand?
                }
        }


        if (getLeftHand().equals(rightHand)) switch (operation.name) {
            //should this clause even exist?
            case "+":
                return new BinaryOperation(new RawValue(2), "*", getLeftHand()).simplify(); //May cause stack overflow?
            case "*":
                return new BinaryOperation(getLeftHand(), "^", new RawValue(2)).simplify();
        }
        else if (!(getLeftHand() instanceof Operation) && !(rightHand instanceof Operation)) {
            return new BinaryOperation(getLeftHand(), operation, rightHand);
        } else if (getLeftHand() instanceof BinaryOperation || rightHand instanceof BinaryOperation) {
            //stack overflow because a*x^b loops itself
            switch (operation.name) {
                case "+":
                    return add(getLeftHand(), rightHand);
                case "*":
                    //TODO: cause stack overflow
                    return multiply(getLeftHand(), rightHand);
            }
        }

        return this;
    }

    /**
     * cross simplification for multiplication
     * NOTE: the operable object returned by this method is simplified!
     * A Sample Execution of this method:
     * input (x*3), (x^2*3), (x*(-3)), the terms inside parenthesis are considered as one subject.
     * by using operator "+", it is implied that the original expression is:
     * x*3 + x^2*x + x*(-3), otherwise (x*3)*(x^2*3)*(x*(-3)) if using "*"
     *
     * @param operator  can only be "+" or "*"
     * @param operables the operables to be cross simplified
     * @return the simplified operables combined.
     */
    private static Operable crossSimplify(ArrayList<Operable> operables, String operator) {
        System.out.println((char) 27 + "[31;1m" + "applying commutative property: " + operator + (char) 27 + "[0m");
        for (Operable operable : operables)
            System.out.print(Expression.colorMathSymbols(operable.toString()) + ", ");
        System.out.println();
        for (int i = 0; i < operables.size(); i++) {
            for (int q = i + 1; q < operables.size(); q++) {
                BinaryOperation pending = new BinaryOperation(operables.get(i), operator, operables.get(q));
                System.out.println((char) 27 + "[34;1m" + "checking: " + (char) 27 + "[0m" + Expression.colorMathSymbols(pending.toString()));
                if (pending.simplifiable()) {
                    System.out.println(" -> qualified");
                    operables.set(i, pending.simplify());
                    String temp = Expression.colorMathSymbols(operables.get(i).toString());
                    System.out.println((char) 27 + "[1;32m" + "simplified: " + (char) 27 + "[0m" + temp);
                    operables.remove(q);
                    i = 0;
                } else System.out.println(" -> false");
            }
        }
        return reconstruct(operables, operator);
    }

    /**
     * reconstructs a chain of binary operations from an uniform operator.
     * method call for (a,b,c,d) and an operator of + would return ((a+b)+c)+d.
     *
     * @param ops      the operables to be chained up
     * @param operator the operator used to reconstruct the bin operation
     * @return reconstructed chain of binary operations.
     */
    private static Operable reconstruct(ArrayList<Operable> ops, String operator) {
        ArrayList<Operable> operables = new ArrayList<>(ops);
        if (operables.size() > 1) {
            BinaryOperation base = new BinaryOperation(operables.remove(0), operator, operables.remove(0));
            while (operables.size() > 0) base = new BinaryOperation(base, operator, operables.remove(0));
            return base;
        }
        return operables.get(0);
    }

    private static Operable multiplyPriority3(BinaryOperation binOp, Operable op) {
        /*
        Operable term1 = new BinaryOperation(binOp.getLeftHand(), "*", op).simplify();
        Operable term2 = new BinaryOperation(binOp.rightHand, "*", op).simplify();
        return new BinaryOperation(term1, binOp.operation, term2); //removed .expand May 26th confirmed removal. CONFIRMED!!

        */
        ArrayList<Operable> binOps = extractBinOp(binOp, binOp.operation.name);
        for (int i = 0; i < binOps.size(); i++) {
            Operable temp = binOps.get(i);
            Operable reconstructed = new BinaryOperation(temp, "*", op);
            binOps.set(i, reconstructed);
        }
        return reconstruct(binOps, "+");
    }

    private static ArrayList<Operable> extractBinOp(Operable operable, String operator) {
        if (!(operable instanceof BinaryOperation)) {
            ArrayList<Operable> temp = new ArrayList<>();
            temp.add(operable);
            return temp;
        }
        ArrayList<Operable> binOps = new ArrayList<>();
        BinaryOperation operation = (BinaryOperation) operable;
        if (operation.operation.equals(operator)) {
            ArrayList<Operable> left = extractBinOp(operation.getLeftHand(), operator);
            ArrayList<Operable> right = extractBinOp(operation.rightHand, operator);
            binOps.addAll(left);
            binOps.addAll(right);
        } else binOps.add(operable);
        //System.out.println(binOps);
        return binOps;
    }


    private static Operable multiply(Operable left, Operable right) {
        if (left instanceof BinaryOperation && right instanceof BinaryOperation) {
            BinaryOperation l = (BinaryOperation) left, r = (BinaryOperation) right;
            if (l.operation.equals("+") && r.getPriority() < 3) {
                return multiplyPriority3(l, r);
            } else if (l.getPriority() < 3 && r.operation.equals("+")) {
                return multiplyPriority3(r, l);
            }
            if (l.getPriority() == r.getPriority() && l.getPriority() == 3) { //"+","-", (a+b)(c+d) = ac+ad+bc+bd = ...
                ArrayList<Operable> terms = new ArrayList<>();
                terms.add(new BinaryOperation(l.getLeftHand(), "*", r.getLeftHand()).simplify());
                terms.add(new BinaryOperation(l.getLeftHand(), "*", r.rightHand).simplify());
                terms.add(new BinaryOperation(l.rightHand, "*", r.getLeftHand()).simplify());
                terms.add(new BinaryOperation(l.rightHand, "*", r.rightHand).simplify());
                return crossSimplify(terms, "+");
            } else if (l.getPriority() == r.getPriority() && l.getPriority() == 2) {
                ArrayList<Operable> extracted = new ArrayList<>();
                extracted.add(l.getLeftHand());
                extracted.add(l.rightHand);
                extracted.add(r.getLeftHand());
                extracted.add(r.rightHand);
                return crossSimplify(extracted, "*");
            } else if (l.getPriority() == r.getPriority() && l.getPriority() == 1) {
                if (l.getLeftHand().equals(r.getLeftHand())) { // x^a * x^b = x^(a+b)
                    Operable temp = new BinaryOperation(l.rightHand, "+", r.rightHand).simplify();
                    //the following line causes an error for now because of .expand -> will return x^a+b instead of x^(a+b)
                    return new BinaryOperation(l.getLeftHand(), "^", temp).simplify();
                }
            }
            //TODO: recursiveSimplify, x*a*b*c*d*x, this should be checked first
        } else if (left instanceof BinaryOperation || right instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) (left instanceof BinaryOperation ? left : right);
            Operable op = left == binOp ? right : left;
            if (binOp.getPriority() == 3) {
                return multiplyPriority3(binOp, op);
            } else if (binOp.operation.equals("*")) {
                return crossSimplify(toArrayList(binOp.getLeftHand(), binOp.rightHand, op), "*");
            } else if (binOp.operation.equals("^") && binOp.getLeftHand().equals(op)) {
                Operable temp = new BinaryOperation(binOp.rightHand, "+", new RawValue(1)).simplify();
                return new BinaryOperation(op, "^", temp).simplify();
            }
        }
        return new BinaryOperation(left, "*", right);
    }

    private static Operable add(Operable left, Operable right) {
        if (left instanceof BinaryOperation && right instanceof BinaryOperation) {
            BinaryOperation l = (BinaryOperation) left, r = (BinaryOperation) right;
            if (l.operation.equals(r.operation) && l.operation.equals("*")) {
                return crossFactor("*", l.getLeftHand(), l.rightHand, r.getLeftHand(), r.rightHand); //TODO: debug
            }
            /*
            ArrayList<Operable> temp = extractBinOp(l, "+");
            for (int i = 0; i < temp.size(); i++) {
                BinaryOperation binOp = new BinaryOperation(temp.get(i),"+",r);
                if (binOp.simplifiable()){
                    temp.set(i,binOp.simplify());
                    break;
                }
            }
            return reconstruct(temp, "+");
            */
            else if (l.operation.equals(r.operation) && l.operation.equals("+")) {
                return crossSimplify(toArrayList(l.getLeftHand(), l.rightHand, r.getLeftHand(), r.rightHand), "+");
            }
        } else if (left instanceof BinaryOperation || right instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) (left instanceof BinaryOperation ? left : right);
            Operable op = left == binOp ? right : left;
            if (binOp.operation.equals("+")) {
                return crossSimplify(toArrayList(binOp.getLeftHand(), binOp.rightHand, op), "+");
            } else if (binOp.operation.equals("*")) {
                if (op.equals(binOp.getLeftHand())) {
                    //assert !(binOp.rightHand instanceof Operation);
                    Operable temp = new BinaryOperation(binOp.rightHand, "+", new RawValue(1)).simplify();
                    //System.out.println(temp);
                    return new BinaryOperation(op, "*", temp).simplify();
                } else if (op.equals(binOp.rightHand)) {
                    //System.out.println(binOp.getLeftHand());
                    Operable temp = new BinaryOperation(binOp.getLeftHand(), "+", new RawValue(1)).simplify();
                    //System.out.println(temp);
                    return new BinaryOperation(op, "*", temp).simplify();
                }
            }
        }
        return new BinaryOperation(left, "+", right);
    }

    /**
     * factor terms if applicable
     * NOTE: the operable object returned by this method is simplified!
     * NOTE: works for a*b + b*c, but does not work for a^b * a*c, because the property is essentially different!
     * for input "*", "a*b + a" would return
     * NOTE: only works for exactly 4 inputs!, Do not use more!
     *
     * @param ops the operations to be factored.
     * @return a factored Operable object
     */
    private static Operable crossFactor(String operator, Operable... ops) {
        System.out.println("factoring...");
        ArrayList<Operable> operables = new ArrayList<>();
        operables.addAll(Arrays.asList(ops));
        for (int i = 0; i < operables.size() - 1; i++) {
            for (int q = i + 1; q < operables.size(); q++) {
                Operable left = operables.get(i);
                Operable right = operables.get(q);
                if (left.equals(right)) {
                    ArrayList<Operable> subjects = new ArrayList<>();
                    for (int t = 0; t < operables.size(); t++) {
                        if (t == i || t == q) continue;
                        subjects.add(operables.get(t));
                    }
                    Operable reconstructed = reconstruct(subjects, "+");
                    Operable simplified = crossSimplify(subjects, "+");
                    if (!reconstructed.equals(simplified)) {//check to see if it is actually simplified
                        //succeeded!!! May 26th
                        return new BinaryOperation(left, operator, simplified);
                    }
                }
                //System.out.println("identical: " + GraphFunction.colorMathSymbols(left + "; " + right));
            }
        }
        //bug identified May 26th
        BinaryOperation left = new BinaryOperation(ops[0], operator, ops[1]);
        BinaryOperation right = new BinaryOperation(ops[2], operator, ops[3]);
        return new BinaryOperation(left, "+", right);
    }

    @Override
    public BinaryOperation replicate() {
        return new BinaryOperation(getLeftHand().replicate(), operation, rightHand.replicate());
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

    public boolean equals(Operable other) {
        return other instanceof BinaryOperation && ((BinaryOperation) other).getLeftHand().equals(this.getLeftHand()) && ((BinaryOperation) other).rightHand.equals(this.rightHand) && this.operation.equals(((BinaryOperation) other).operation);
    }

    public Operable toAdditionOnly() {
        BinaryOperation newInstance = this.replicate();
        if (getLeftHand() instanceof Operation) newInstance.setLeftHand(((Operation) getLeftHand()).toAdditionOnly());
        newInstance.rightHand = rightHand instanceof Operation ? ((Operation) newInstance.rightHand).toAdditionOnly() : newInstance.rightHand;
        if (newInstance.operation.name.equals("-")) {
            newInstance.operation = RegisteredBinaryOperation.extract("+");
            newInstance.rightHand = UnaryOperation.negate(newInstance.rightHand);
        }
        return newInstance;
    }

    private static ArrayList<Operable> toArrayList(Operable... operables) {
        ArrayList<Operable> created = new ArrayList<>();
        created.addAll(Arrays.asList(operables));
        return created;
    }

    public Operable plugIn(Operable nested) {
        return new BinaryOperation(getLeftHand().plugIn(nested), operation, rightHand.plugIn(nested));
    }

    public Operable getRightHand() {
        return rightHand;
    }

    public void setRightHand(Operable operable) {
        this.rightHand = operable;
    }
}
