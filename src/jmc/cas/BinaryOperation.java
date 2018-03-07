package jmc.cas;


import java.util.ArrayList;

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

    private BinaryOperation(Operable leftHand, RegisteredBinaryOperation operation, Operable rightHand) {
        super(leftHand);
        this.operation = operation;
        this.rightHand = rightHand;
        omitParenthesis = true;
        simplifyParenthesis();
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

    private BinaryOperation simplifyParenthesis() {
        processParentheticalNotation(getLeftHand(), false);
        processParentheticalNotation(this.rightHand, true);
        return this;
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

    public double val() {
        return operation.eval(getLeftHand().val(), getRightHand().val());
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
     * NOTE: modifies self.
     */
    @Override
    public BinaryOperation toExponentialForm() {
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
     * HELPER METHOD
     *
     * @param i i == 1 -> getLeftHand(), i == 2 -> getRightHand()
     * @return operand Operable
     */
    private Operable get(int i) {
        switch (i) {
            case 1:
                return getLeftHand();
            case 2:
                return getRightHand();
        }
        throw new RuntimeException("invalid index");
    }

    /**
     * HELPER METHOD
     *
     * @param i i == 1 -> getRightHand(), i == 2 -> getLeftHand()
     * @return operand Operable
     */
    private Operable getOther(int i) {
        if (i != 1 && i != 2) throw new RuntimeException("invalid index");
        return i == 1 ? getRightHand() : getLeftHand();
    }

    /**
     * HELPER METHOD
     * whether this BinaryOperation contain the operand as an immediate child
     *
     * @param o Operable operand
     * @return 1 if at left side operand, 2 if at right side operand, 0 if !levelOf.
     */
    private int contains(Operable o) {
        if (getLeftHand().equals(o)) return 1;
        else if (getRightHand().equals(o)) return 2;
        return 0;
    }

    /**
     * Note: modifies self, but may not
     *
     * @return the simplified version of self
     */
    public Operable simplify() {

        setLeftHand(getLeftHand().simplify());
        rightHand = rightHand.simplify();
        this.simplifyParenthesis();

        if (isUndefined()) return RawValue.UNDEF;

        if (getLeftHand() instanceof RawValue && rightHand instanceof RawValue) {
            RawValue r1 = (RawValue) getLeftHand();
            RawValue r2 = (RawValue) rightHand;
            if (getLeftHand() instanceof Fraction && 是加减乘除()) {
                Fraction f = (Fraction) getLeftHand().clone();
                RawValue r = (RawValue) rightHand.clone();
                switch (operation.name) {
                    case "+":
                        return f.add(r);
                    case "-":
                        return f.sub(r);
                    case "*":
                        return f.mult(r);
                    case "/":
                        return f.div(r);
                }
            } else if (rightHand instanceof Fraction && 是加减乘除()) {
                Fraction f = (Fraction) rightHand.clone();
                RawValue r = (RawValue) getLeftHand().clone();
                switch (operation.name) {
                    case "+":
                        return f.add(r);
                    case "-":
                        return f.negate().add(r);
                    case "*":
                        return f.mult(r);
                    case "/":
                        return f.inverse().mult(r);
                }
            } else if (operation.name.equals("^")) { //fractional mode
                if (r1 instanceof Fraction) {
                    return ((Fraction) r1).exp(r2);
                } else if (r2.val() == 0) { // 0^0
                    return r1.val() == 0 ? RawValue.UNDEF : new RawValue(1);
                } else if (r2.val() < 0) { // x^-b = (1/x)^b
                    return new BinaryOperation(r1.inverse(), "^", r2.negate()).simplify();
                }
            }


            if (r1.isInteger() && r2.isInteger()) {
                if (operation.name.equals("/")) {
                    return new Fraction(r1.intValue(), r2.intValue());
                } else return new RawValue(operation.eval(r1.intValue(), r2.intValue()));
            } else if (!r1.isInteger() && !(r1 instanceof Fraction)) {
                RawValue f1 = Fraction.convertToFraction(r1.doubleValue(), Fraction.TOLERANCE);
                return new BinaryOperation(f1, operation, r2).simplify();
            } else if (!r2.isInteger() && !(r2 instanceof Fraction)) {
                RawValue f2 = Fraction.convertToFraction(r2.doubleValue(), Fraction.TOLERANCE);
                return new BinaryOperation(r1, operation, f2).simplify();
            }
        }


        if (getLeftHand().equals(getRightHand())) {
            switch (operation.name) {
                case "+":
                    return new BinaryOperation(new RawValue(2), "*", getLeftHand());
                case "-":
                    return new RawValue(0);
                case "*":
                    return new BinaryOperation(getLeftHand(), "^", new RawValue(2));
                case "/":
                    return new RawValue(1);
            }
        }

        this.toAdditionOnly().toExponentialForm();

        for (int i = 1; i <= 2; i++) {
            if (get(i).val() == 0) {
                switch (operation.name) {
                    case "+":
                        return getOther(i);
                    case "*":
                        return new RawValue(0);
                    case "^":
                        return i == 1 ? new RawValue(0) : new RawValue(1);
                }
            }
        }

        if (getLeftHand() instanceof BinaryOperation && getRightHand() instanceof BinaryOperation) {
            BinaryOperation binOp1 = (BinaryOperation) getLeftHand();
            BinaryOperation binOp2 = (BinaryOperation) getRightHand();
            if (binOp1.operation.equals(binOp2.operation)) //e.g. x*a + x*b, "*" == "*"
                switch (operation.name) {
                    case "+":
                        switch (binOp1.operation.name) {
                            case "*":
                                /*
                                1. for the form x*(a+b) + x*c, should it be simplified to x*(a+b+c)?
                                2. for the form x*(a+b) + x*(b-a), it should definitely be simplified to 2*b*x.
                                right now it does both 1 and 2.
                                */
                                for (int i = 1; i <= 2; i++) {
                                    Operable o1 = binOp1.get(i);
                                    int idx = binOp2.contains(o1);
                                    if (idx != 0) {
                                        Operable add = new BinaryOperation(binOp1.getOther(i), "+", binOp2.getOther(idx)).simplify();
                                        return new BinaryOperation(o1, "*", add).simplify();
                                    }
                                }
                                break;
                            case "^":
                                break;

                        }
                        break;
                    case "*":
                        switch (binOp1.operation.name) {
                            case "^":
                                /*
                                1. for the form x^(a+b) + x^c, should it be simplified to x^(a+b+c)?
                                2. for the form x^(a+b) + x^(-a), it should definitely be simplified to 2*b*x.
                                 */
                                Operable op1Left = binOp1.getLeftHand();
                                Operable op2Left = binOp2.getLeftHand();
                                if (op1Left.equals(op2Left)) {
                                    Operable add = new BinaryOperation(binOp1.getRightHand(), "+", binOp2.getRightHand()).simplify();
                                    return new BinaryOperation(op1Left, "^", add);
                                }
                                break;
                        }
                        break;
                }
        }


        if (getPriority() == 1) return this; //up to this point the ^ operator cannot be simplified.

        //up to this point all simplifications should have been tried, except the recursive cross-simplification
        if (getLeftHand() instanceof LeafNode //e.g. ln(x) * x is not simplifiable
                && rightHand instanceof LeafNode) {
            return this; //no more could be done.
        } else if (getLeftHand() instanceof LeafNode //e.g. x * (3.5x + 4) is not simplifiable
                && rightHand instanceof BinaryOperation
                && ((BinaryOperation) rightHand).getPriority() != getPriority()) {
            return this;
        } else if (getRightHand() instanceof LeafNode //e.g. (3.5x + 4) * x  is not simplifiable
                && getLeftHand() instanceof BinaryOperation
                && ((BinaryOperation) getLeftHand()).getPriority() != getPriority()) {
            return this;
        } else if (getLeftHand() instanceof BinaryOperation //e.g. (3.5x + 4) * (4x + 3) is not simplifiable
                && ((BinaryOperation) getLeftHand()).getPriority() != getPriority()
                && rightHand instanceof BinaryOperation
                && ((BinaryOperation) rightHand).getPriority() != getPriority()) {
            return this;
        } else { //perform cross-simplification
            ArrayList<Operable> flattened = this.flattened(); //make operations of the same priority throughout the binary tree visible at the same level.
            crossSimplify(flattened);
            if (flattened.size() < 2) return flattened.get(0);
            else return reconstructBinTree(flattened);
        }
    }

    public int numNodes() {
        return getLeftHand().numNodes() + getRightHand().numNodes() + 1;
    }

    /**
     * This method shouldn't be used when priority == 1, or with the operation ^, since it is not commutative.
     *
     * @param pool ArrayList containing flattened operables
     */
    private void crossSimplify(ArrayList<Operable> pool) {
        if (getPriority() == 1) return;
        for (int i = 0; i < pool.size() - 1; i++) {
            Operable operable = pool.get(i);
            for (int k = i + 1; k < pool.size(); k++) {
                Operable other = pool.get(k);
                String operation = getPriority() == 2 ? "*" : "+";
                BinaryOperation binOp = new BinaryOperation(operable, operation, other);
                int n1 = binOp.numNodes();
                Operable op = binOp.simplify(); //be careful, avoid stack overflow
                if (op.numNodes() < n1) { //simplifiable
                    pool.remove(i);
                    pool.remove(k - 1);
                    pool.add(op);
                    crossSimplify(pool); //result maybe bin tree or just Operable.
                } else if (pool.size() <= 2) return;
            }
        }
    }

    /**
     * reconstruct binary operation tree from flattened ArrayList of operations.
     *
     * @return reconstructed BinaryOperation tree.
     */
    private BinaryOperation reconstructBinTree(ArrayList<Operable> flattened) {
        if (flattened.size() < 2) return null;
        String op = getPriority() == 2 ? "*" : "+";
        BinaryOperation root = new BinaryOperation(flattened.remove(0), op, flattened.remove(0));
        while (flattened.size() > 0) root = new BinaryOperation(root, op, flattened.remove(0));
        return root;
    }

    /**
     * e.g. input: "(3 + 4.5) * ln(5.3 + 4) / 2.7 / (x + 1) * x / 3"
     * e.g. output: [(3+4.5), ln(5.3+4), 2.7^(-1), (x+1)^(-1), x, 3^(-1)]
     * <p>
     * e.g. input: "3 - 2x + 4x - 4 + 7z"
     * e.g. output: [3, (-1)*2*x, 4*x, (-1)*4, 7*z]
     *
     * @return an ArrayList containing all terms at the same priority level
     */
    public ArrayList<Operable> flattened() {
        ArrayList<Operable> pool = new ArrayList<>();
        if (operation.priority == 1) return pool; //if the operation is ^, then no commutative property applies.
        BinaryOperation clone = this.clone().toAdditionOnly().toExponentialForm();
        clone.flat(pool, clone.getLeftHand());
        clone.flat(pool, clone.getRightHand());
        return pool;
    }

    private void flat(ArrayList<Operable> pool, Operable operable) {
        if (operable instanceof UnaryOperation || operable instanceof RawValue || operable instanceof Variable) {
            pool.add(operable);
        } else if (operable instanceof BinaryOperation) {
            BinaryOperation binOp = ((BinaryOperation) operable);
            if (binOp.getPriority() == this.getPriority()) {
                pool.addAll(binOp.flattened());
            } else {
                pool.add(binOp);
            }
        }
    }

    private boolean 是加减乘除() {
        return "+-*/".contains(operation.name);
    }

    @Override
    public BinaryOperation clone() {
        return new BinaryOperation(getLeftHand().clone(), operation, rightHand.clone());
    }

    public boolean equals(Operable other) {
        if (!(other instanceof BinaryOperation)) return false;
        BinaryOperation binOp = (BinaryOperation) other;
        return binOp.operation.equals(operation)
                && ((binOp.getLeftHand().equals(this.getLeftHand())
                && binOp.getRightHand().equals(this.getRightHand()))
                || (binOp.getLeftHand().equals(this.getRightHand())
                && binOp.getRightHand().equals(this.getLeftHand())));
    }

    public BinaryOperation toAdditionOnly() {
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

    public boolean isUndefined() {
        if (getLeftHand().isUndefined() || getRightHand().isUndefined()) return true;
        if (rightHand instanceof RawValue) {
            RawValue r = ((RawValue) rightHand);
            switch (operation.name) {
                case "/":
                    return r.isZero();
                case "^":
                    return getLeftHand() instanceof RawValue && ((RawValue) getLeftHand()).isZero() && !r.isPositive();
            }
        }
        //TODO: how about BinaryOperations like 3 - 3 which is essentially 0?
        return false;
    }

    public int levelOf(Operable o) {
        if (this.equals(o)) return 0;
        int left = getLeftHand().levelOf(o);
        int right = getRightHand().levelOf(o);
        if (left == -1 && right == -1) return -1;
        if (left == -1 || right == -1) return left == -1 ? right + 1 : left + 1;
        return left > right ? right + 1 : left + 1;
    }

    public Operable replace(Operable o, Operable r) {
        if (this.equals(o)) return r;
        BinaryOperation clone = this.clone();
        clone.setLeftHand(clone.getLeftHand().replace(o, r));
        clone.setRightHand(clone.getRightHand().replace(o, r));
        return clone;
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

        private static void define(String name, int priority, BinEvaluable evaluable) {
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
