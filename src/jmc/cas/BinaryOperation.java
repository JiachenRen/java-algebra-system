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
        boolean k = getLeftHand() instanceof RawValue && getLeftHand().val() < 0;
        boolean q = getRightHand() instanceof RawValue && getRightHand().val() < 0;
        String left = k ? "(" + getLeftHand().toString() + ")" : getLeftHand().toString();
        String right = q ? "(" + getRightHand().toString() + ")" : getRightHand().toString();
        String temp = left + operation.name + right;
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
     * HELPER METHOD
     *
     * @param r1 RawValue #1
     * @param r2 RawValue #2
     * @return simplified r1 [RegisteredBinaryOperation] r2
     */
    private Operable simplify(RawValue r1, RawValue r2) {
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
                return new Fraction(r1.intValue(), r2.intValue()).reduce();
            } else return new RawValue(operation.eval(r1.intValue(), r2.intValue()));
        } else if (!r1.isInteger() && !(r1 instanceof Fraction)) {
            RawValue f1 = Fraction.convertToFraction(r1.doubleValue(), Fraction.TOLERANCE);
            return new BinaryOperation(f1, operation, r2).simplify();
        } else if (!r2.isInteger() && !(r2 instanceof Fraction)) {
            RawValue f2 = Fraction.convertToFraction(r2.doubleValue(), Fraction.TOLERANCE);
            return new BinaryOperation(r1, operation, f2).simplify();
        }
        return null;
    }

    /**
     * HELPER METHOD
     * <p>
     * handles special cases where the right hand of the binary operation is a certain integer.
     *
     * @param i the integer that represents the value of the right operand.
     * @return simplified Operable
     */
    private Operable simplifyRightHand(int i) {
        if (i == 0) {
            switch (operation.name) {
                case "+":
                    return getLeftHand();
                case "-":
                    return getLeftHand();
                case "*":
                    return RawValue.ZERO;
                case "/":
                    return RawValue.UNDEF;
                case "^":
                    return RawValue.ONE;
            }
        } else if (i == 1) {
            switch (operation.name) {
                case "+":
                    break;
                case "-":
                    break;
                case "*":
                    return getLeftHand();
                case "/":
                    return getLeftHand();
                case "^":
                    return getLeftHand();
            }
        }
        return null;
    }

    /**
     * HELPER METHOD
     * handles a^b^c = a^(b*c), (a*b)^# = a^#*b^#
     *
     * @param o right hand operand of the binary operation
     * @return simplified self of type Operable
     */
    private Operable simplifyRightHand(Operable o) {
        if (o instanceof RawValue && ((RawValue) o).isInteger()) {
            RawValue r = (RawValue) o;
            Operable simplified = simplifyRightHand(r.intValue());
            if (simplified != null) return simplified;
        }
        if (getLeftHand() instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) getLeftHand();
            switch (operation.name) {
                case "^":
                    switch (binOp.operation.name) {
                        case "*": // (a*b)^# = a^#*b^#
                            if (o instanceof RawValue) {
                                RawValue r = (RawValue) o;
                                BinaryOperation left = new BinaryOperation(binOp.getLeftHand(), "^", r);
                                BinaryOperation right = new BinaryOperation(binOp.getRightHand(), "^", r);
                                return new BinaryOperation(left, "*", right).simplify();
                            }
                        case "^": // (a^b^c) = a^(b*c)
                            BinaryOperation exp = new BinaryOperation(binOp.getRightHand(), "*", o);
                            return new BinaryOperation(binOp.getLeftHand(), "^", exp).simplify();
                    }
            }
        }
        return null;
    }

    /**
     * HELPER METHOD
     * handles 0*x, x*0, x*1, 1*x, 0^x, x^0, 1^x, x^1
     *
     * @return simplified self
     */
    private Operable simplifyZeroOne() {
        for (int i = 1; i <= 2; i++) {
            if (get(i).val() == 0) {
                switch (operation.name) {
                    case "+":
                        return getOther(i); //should this call .simplify()?
                    case "*":
                        return new RawValue(0);
                    case "^":
                        return i == 1 ? RawValue.ZERO : RawValue.ONE;
                }
            } else if (get(i).equals(RawValue.ONE)) {
                switch (operation.name) {
                    case "*":
                        return getOther(i);
                    case "^":
                        return i == 1 ? RawValue.ONE : getLeftHand();
                }
            }
        }
        return null;
    }

    /**
     * HELPER METHOD
     *
     * @param binOp1 left hand operand of type BinaryOperation
     * @param binOp2 right hand operand of type BinaryOperation
     * @return simplified self of type Operable
     */
    private Operable simplify(BinaryOperation binOp1, BinaryOperation binOp2) {
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
        return null;
    }

    /**
     * HELPER METHOD
     * handles a*a^b = a^(b+1)
     *
     * @param op       generic Operation
     * @param expBinOp BinaryOperation with operation == "^"
     * @return simplified Operable
     */
    private Operable simplify(Operable op, BinaryOperation expBinOp) {
        if (!operation.name.equals("*")) return null;
        if (op.equals(expBinOp.getLeftHand()) && expBinOp.operation.equals("^")) {
            BinaryOperation exp = new BinaryOperation(expBinOp.getRightHand(), "+", RawValue.ONE);
            return new BinaryOperation(op, "^", exp).simplify();
        }
        return null;
    }

    /**
     * Note: modifies self, but may not
     *
     * @return the simplified version of self
     */
    public Operable simplify() {

        simplifySubNodes();
        simplifyParenthesis();

        if (!operation.isStandard())
            return this; // nothing could be done with non-standard operations
        if (isUndefined()) return RawValue.UNDEF;

        if (getLeftHand() instanceof RawValue && rightHand instanceof RawValue) {
            Operable simplified = simplify((RawValue) getLeftHand(), (RawValue) rightHand);
            if (simplified != null) return simplified;
        }

        //at this point neither left hand nor right hand is undefined.

        Operable simplified1 = simplifyRightHand(getRightHand());
        if (simplified1 != null) return simplified1;


        if (getLeftHand().equals(getRightHand())) {
            switch (operation.name) {
                case "+":
                    return new BinaryOperation(new RawValue(2), "*", getLeftHand());
                case "-":
                    return RawValue.ZERO;
                case "*":
                    return new BinaryOperation(getLeftHand(), "^", new RawValue(2));
                case "/":
                    return RawValue.ONE;
            }
        }

        //converting to exponential form and additional only, allowing further simplification.
        this.toAdditionOnly().toExponentialForm().simplifySubNodes();

        //handle special cases
        Operable simplified2 = simplifyZeroOne();
        if (simplified2 != null) return simplified2;

        if (getLeftHand() instanceof BinaryOperation && getRightHand() instanceof BinaryOperation) {
            BinaryOperation binOp1 = (BinaryOperation) getLeftHand();
            BinaryOperation binOp2 = (BinaryOperation) getRightHand();
            Operable simplified = simplify(binOp1, binOp2);
            if (simplified != null) return simplified;
        }

        if (getLeftHand() instanceof BinaryOperation) {
            Operable simplified = simplify(getRightHand(), (BinaryOperation) getLeftHand());
            if (simplified != null) return simplified;
        }
        if (getRightHand() instanceof BinaryOperation) {
            Operable simplified = simplify(getLeftHand(), (BinaryOperation) getRightHand());
            if (simplified != null) return simplified;
        }


        if (getPriority() == 1) return this.beautify(); //up to this point the ^ operator cannot be simplified.

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

    /**
     * HELPER METHOD
     *
     * @return self
     */
    private Operable simplifySubNodes() {
        setLeftHand(getLeftHand().simplify());
        setRightHand(rightHand.simplify());
        return this;
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
        if (!(operation.equals("*") || operation.equals("+"))) return;
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
        if (operation.priority == 1 || !operation.isStandard())
            return pool; //if the operation is ^, then no commutative property applies.
        BinaryOperation clone = this.clone().toAdditionOnly().toExponentialForm();
        clone.flat(pool, clone.getLeftHand());
        clone.flat(pool, clone.getRightHand());
        return pool;
    }

    /**
     * this method is specific to binary operation because it tears down the binary tree
     * and extracts nodes of the same binary operation priority, making applying commutative properties
     * of + and * possible.
     *
     * @param pool     pool of flattened binary tree nodes.
     * @param operable the binary tree to be flattened
     */
    private void flat(ArrayList<Operable> pool, Operable operable) {
        if (operable instanceof LeafNode) {
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
                && binOp.getRightHand().equals(this.getLeftHand())
                && (binOp.operation.equals("*") || binOp.operation.equals("+"))));
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

    /**
     * basically reversing the effects of toAdditionalOnly and toExponentialForm
     * a*b^(-1) -> a/b,
     * a*(1/3) -> a/3,
     * a+(-1)*b -> a-b
     *
     * @return beautified version of the original
     */
    public Operable beautify() {
        Operable left = getLeftHand().beautify();
        Operable right = getRightHand().beautify();
        switch (operation.name) {
            case "*":
                ArrayList<Operable> numerators = new ArrayList<>();
                ArrayList<Operable> denominators = new ArrayList<>();
                separate(left, denominators, numerators);
                separate(right, denominators, numerators);
                Operable numerator = reconstruct(numerators);
                Operable denominator = reconstruct(denominators);
                if (denominator == null || denominator.equals(RawValue.ONE))
                    return numerator;
                else return new BinaryOperation(numerator, "/", denominator);
            case "+":
                if (isVirtuallyNegative(left) && !isVirtuallyNegative(right)) {
                    Operable right1 = new BinaryOperation(left, "*", RawValue.ONE.negate()).simplify();
                    return new BinaryOperation(right, "-", right1).beautify();
                } else if (isVirtuallyNegative(right) && !isVirtuallyNegative(left)) {
                    Operable right1 = new BinaryOperation(right, "*", RawValue.ONE.negate()).simplify();
                    return new BinaryOperation(left, "-", right1).beautify();
                }
            case "^":
                ArrayList<Operable> ns = new ArrayList<>();
                ArrayList<Operable> ds = new ArrayList<>();
                this.setLeftHand(left);
                this.setRightHand(right);

                separate(this, ds, ns);
                Operable n = reconstruct(ns);
                Operable d = reconstruct(ds);
                if (n == null) return this;
                if (d == null || d.equals(RawValue.ONE))
                    return n;
                else return new BinaryOperation(n, "/", d);

        }
        this.setLeftHand(left);
        this.setRightHand(right);
        return this;
    }

    private boolean isVirtuallyNegative(Operable binOp) {
        return binOp.val() < 0 || binOp instanceof BinaryOperation && Operable.contains(((BinaryOperation) binOp.explicitNegativeForm()).flattened(), RawValue.ONE.negate());
    }

    private Operable reconstruct(ArrayList<Operable> operables) {
        if (operables.size() == 0) return null;
        return operables.size() >= 2 ? reconstructBinTree(operables) : operables.get(0);
    }

    /**
     * separates denominator and numerator
     *
     * @param o            the Operable to be separated
     * @param denominators ArrayList containing denominators
     * @param numerators   ArrayList containing numerators
     */
    private void separate(Operable o, ArrayList<Operable> denominators, ArrayList<Operable> numerators) {
        if (o instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) o;
            if (isDivision(binOp)) {
                numerators.add(binOp.getLeftHand());
                denominators.add(binOp.getRightHand());
                return;
            }

            int idx = expFormIdx(binOp);
            switch (idx) {
                case 0:
                    numerators.add(binOp);
                    break;
                case 1:
                    Fraction exp = ((Fraction) binOp.getRightHand()).negate();
                    BinaryOperation b = new BinaryOperation(binOp.getLeftHand(), "^", exp);
                    denominators.add(b); //TODO: rationalize irrational denominator
                    break;
                case 2:
                    RawValue r = ((RawValue) binOp.getRightHand()).negate();
                    if (r.equals(RawValue.ONE)) { //1/n^1 -> 1/n
                        denominators.add(binOp.getLeftHand());
                        break;
                    }
                    BinaryOperation b1 = new BinaryOperation(binOp.getLeftHand(), "^", r);
                    denominators.add(b1);
                    break;
                case 3:
                    Operable exp1 = new BinaryOperation(binOp.getRightHand(), "*", RawValue.ONE.negate()).simplify();
                    BinaryOperation b2 = new BinaryOperation(binOp.getLeftHand(), "^", exp1);
                    denominators.add(b2);
                    break;
            }
        } else {
            if (o instanceof Fraction) {
                Fraction f = (Fraction) o;
                if (f.getNumerator() != 1)
                    numerators.add(new RawValue(f.getNumerator()));
                if (f.getDenominator() != 1)
                    denominators.add(new RawValue(f.getDenominator()));
            } else if (o.val() != 1) {
                numerators.add(o);
            }
        }
    }

    /**
     * HELPER METHOD
     * "a/b" returns true
     *
     * @return whether operation of o is "/"
     */
    private boolean isDivision(Operable o) {
        return o instanceof BinaryOperation && ((BinaryOperation) o).operation.equals("/");
    }

    /**
     * HELPER METHOD
     * detects form a^(-[...])
     * <p>
     * 0 -> not exponential form
     * 1 -> form x^(-a/b)
     * 2 -> form x^-a
     * 3 -> form x^([...]*-a)
     *
     * @return idx that represents the kind of exponential form
     */
    public static int expFormIdx(Operable o) {
        if (!(o instanceof BinaryOperation)) return 0;
        BinaryOperation binOp = (BinaryOperation) o;
        if (!binOp.operation.equals("^")) return 0;
        if (binOp.rightHand instanceof RawValue && binOp.rightHand.val() < 0)
            return binOp.rightHand instanceof Fraction ? 1 : 2;
        if (binOp.rightHand instanceof BinaryOperation) {
            BinaryOperation binOp1 = ((BinaryOperation) binOp.rightHand);
            if (binOp1.operation.equals("*")) {
                ArrayList<Operable> pool = ((BinaryOperation) binOp1.explicitNegativeForm()).flattened();
                if (Operable.contains(pool, RawValue.ONE.negate()))
                    return 3;
            }
        }
        return 0;
    }

    public Operable explicitNegativeForm() {
        BinaryOperation clone = this.clone();
        clone.setLeftHand(getLeftHand().explicitNegativeForm());
        clone.setRightHand(getRightHand().explicitNegativeForm());
        return clone;
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
        simplifyParenthesis();
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

    @Override
    public Operable setLeftHand(Operable o) {
        super.setLeftHand(o);
        simplifyParenthesis();
        return this;
    }

    public boolean is(String s) {
        return operation.equals(s);
    }

    public interface BinEvaluable {
        double eval(double a, double b);
    }

    private static class RegisteredBinaryOperation implements BinEvaluable {
        private static ArrayList<RegisteredBinaryOperation> registeredBinOps;
        private BinEvaluable binEvaluable;
        private String name;
        private int priority; //1 is the most prioritized
        private String standardOperations = "+-*/^";

        static {
            registeredBinOps = new ArrayList<>();
            define("+", 3, (a, b) -> a + b);
            define("-", 3, (a, b) -> a - b);
            define("*", 2, (a, b) -> a * b);
            define("/", 2, (a, b) -> a / b);
            define("^", 1, Math::pow);
            System.out.println("# reserved binary operations declared");
        }

        private boolean isStandard() {
            return standardOperations.contains(name);
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
