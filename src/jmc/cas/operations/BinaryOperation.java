package jmc.cas.operations;


import jmc.MathContext;
import jmc.cas.*;
import jmc.cas.components.Fraction;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Jiachen on 16/05/2017.
 * added the ability to expand parenthesis and constant expressions. Accomplished May 20th.
 * add the ability to mark unavailable domain.
 * code refactored May 20th. Added ADT for operations.
 */
public class BinaryOperation extends Operation {
    private boolean omitParenthesis;
    private RegisteredBinaryOperation operation;

    public BinaryOperation(Operable leftHand, String operation, Operable rightHand) {
        this(leftHand, RegisteredBinaryOperation.extract(operation), rightHand);
    }

    private BinaryOperation(Operable leftHand, RegisteredBinaryOperation operation, Operable rightHand) {
        super(wrap(leftHand, rightHand));
        this.operation = operation;
        omitParenthesis = true;
        simplifyParenthesis();
    }

    /**
     * in ((a+b)+c), the parenthesis around (a+b) is not necessary.
     */
    private BinaryOperation simplifyParenthesis() {
        processParentheticalNotation(getLeft(), false);
        processParentheticalNotation(getRight(), true);
        return this;
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
                if (op.operation.equals(operation) && op.is("^"))
                    op.setOmitParenthesis(false);
                else op.setOmitParenthesis(op.operation.equals(operation) || !isRightHand);
            } else {
                op.setOmitParenthesis(false);
            }
        }
    }

    public Operable getLeft() {
        return getOperand(0);
    }

    public Operable getRight() {
        return getOperand(1);
    }

    public int getPriority() {
        return operation.priority;
    }

    public void setOmitParenthesis(boolean temp) {
        omitParenthesis = temp;
    }

    public void setRight(Operable operable) {
        setOperand(operable, 1);
        simplifyParenthesis();
    }

    /**
     * @param name      name of the new binary operation
     * @param priority  1 is the most prioritized
     * @param evaluable (a,b) -> a [operation] b
     */
    public static void define(String name, int priority, BinEvaluable evaluable) {
        RegisteredBinaryOperation.define(name, priority, evaluable);
    }

    public static int getPriority(String operation) {
        return RegisteredBinaryOperation.extract(operation).priority;
    }

    public static String binaryOperations() {
        return binaryOperations(0) + binaryOperations(1) + binaryOperations(2) + binaryOperations(3);
    }

    /**
     * @return a complete list of binary operations (with corresponding priority)
     */
    public static String binaryOperations(int priority) {
        return RegisteredBinaryOperation.listAsString(priority);
    }

    /**
     * HELPER METHOD
     * this's operation should be "*" to invoke this method
     * (a+b)*(a+ln(c)+d) -> a*a + a*ln(c) + a*d + b*a + b*ln(c) + b*d
     *
     * @param o1 binary operation "+"
     * @param o2 binary operation "+"
     * @return o1*o2 expanded
     */
    private static Operable crossExpand(BinaryOperation o1, BinaryOperation o2) {
        ArrayList<Operable> f1 = o1.flattened(), f2 = o2.flattened();
        final Operable[] result = new Operable[1];
        f1.stream().map(o -> f2.stream()
                .map(p -> Operation.mult(p, o))
                .reduce(Operable::add).get())
                .reduce(Operable::add)
                .ifPresent(o -> result[0] = o);
        return result[0];
    }

    private static boolean isAddition(Operable op) {
        return op instanceof BinaryOperation && ((BinaryOperation) op).is("+");
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
        if (!binOp.is("^")) return 0;
        if (binOp.getRight() instanceof RawValue && binOp.getRight().val() < 0)
            return binOp.getRight() instanceof Fraction ? 1 : 2;
        if (binOp.getRight() instanceof BinaryOperation) {
            BinaryOperation binOp1 = ((BinaryOperation) binOp.getRight());
            if (binOp1.is("*")) {
                ArrayList<Operable> pool = ((BinaryOperation) binOp1.explicitNegativeForm()).flattened();
                if (Operable.contains(pool, RawValue.ONE.negate()))
                    return 3;
            }
        }
        return 0;
    }

    public double eval(double x) {
        double leftVal = getLeft().eval(x);
        double rightVal = getRight().eval(x);
        return operation.eval(leftVal, rightVal);
    }

    @Override
    public BinaryOperation copy() {
        return new BinaryOperation(getLeft().copy(), operation, getRight().copy());
    }

    /**
     * Note: modifies self, but may not
     *
     * @return the simplified version of self
     */
    public Operable simplify() {
        super.simplify();
        simplifyParenthesis();

        if (!operation.isStandard())
            return this; // nothing could be done with non-standard operations
        if (isUndefined()) return RawValue.UNDEF;

        if (getLeft() instanceof RawValue && getRight() instanceof RawValue) {
            Operable simplified = simplify((RawValue) getLeft(), (RawValue) getRight());
            if (simplified != null) return simplified;
        }

        if (getLeft() instanceof UnaryOperation && getRight() instanceof UnaryOperation) {
            Operable simplified = simplify(((UnaryOperation) getLeft()), ((UnaryOperation) getRight()));
            if (simplified != null) return simplified;
        }

        //at this point neither left hand nor right hand is undefined.
        //simplifyRightHand() -> for subtraction and exponentiation, the position of left and right hand is not interchangeable.
        Operable simplified1 = simplifyRightHand(getRight());
        if (simplified1 != null) return simplified1;


        if (getLeft().equals(getRight())) {
            switch (operation.name) {
                case "+":
                    return new BinaryOperation(new RawValue(2), "*", getLeft());
                case "-":
                    return RawValue.ZERO;
                case "*":
                    return new BinaryOperation(getLeft(), "^", new RawValue(2));
                case "/":
                    return RawValue.ONE;
            }
        }

        //converting to exponential form and additional only, allowing further simplification.
        this.toAdditionOnly().toExponentialForm();
        super.simplify();

        //handle special cases
        Operable simplified2 = simplifyZeroOne();
        if (simplified2 != null) return simplified2;

        if (getLeft() instanceof BinaryOperation && getRight() instanceof BinaryOperation) {
            BinaryOperation binOp1 = (BinaryOperation) getLeft();
            BinaryOperation binOp2 = (BinaryOperation) getRight();
            Operable simplified = simplify(binOp1, binOp2);
            if (simplified != null) return simplified;
        }

        if (getLeft() instanceof BinaryOperation) {
            Operable simplified = simplify(getRight(), (BinaryOperation) getLeft());
            if (simplified != null) return simplified;
        }
        if (getRight() instanceof BinaryOperation) {
            Operable simplified = simplify(getLeft(), (BinaryOperation) getRight());
            if (simplified != null) return simplified;
        }


        if (getPriority() == 1) {
            simplifyParenthesis();
            return this; //up to this point the ^ operator cannot be simplified.
        }

        //up to this point all simplifications should have been tried, except the recursive cross-simplification
        if (getLeft() instanceof BinLeafNode //e.g. ln(x) * x is not simplifiable
                && getRight() instanceof BinLeafNode) {
            return this.simplifyParenthesis(); //no more could be done.
        } else if (getLeft() instanceof BinLeafNode //e.g. x * (3.5x + 4) is not simplifiable
                && getRight() instanceof BinaryOperation
                && ((BinaryOperation) getRight()).getPriority() != getPriority()) {
            return this.simplifyParenthesis();
        } else if (getRight() instanceof BinLeafNode //e.g. (3.5x + 4) * x  is not simplifiable
                && getLeft() instanceof BinaryOperation
                && ((BinaryOperation) getLeft()).getPriority() != getPriority()) {
            return this.simplifyParenthesis();
        } else if (getLeft() instanceof BinaryOperation //e.g. (3.5x + 4) * (4x + 3) is not simplifiable
                && ((BinaryOperation) getLeft()).getPriority() != getPriority()
                && getRight() instanceof BinaryOperation
                && ((BinaryOperation) getRight()).getPriority() != getPriority()) {
            return this.simplifyParenthesis();
        } else { //perform cross-simplification
            ArrayList<Operable> flattened = this.flattened(); //make operations of the same priority throughout the binary tree visible at the same level.
            crossSimplify(flattened);
            return reconstructBinTree(flattened);
        }
    }

    /**
     * basically reversing the effects of toAdditionalOnly and toExponentialForm
     * a*b^(-1) -> a/b,
     * a*(1/3) -> a/3,
     * a+(-1)*b -> a-b
     *
     * @return beautified version of the original
     */
    @Override
    public Operable beautify() {
        Operable left = getLeft().beautify();
        Operable right = getRight().beautify();
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
                this.setLeft(left);
                this.setRight(right);

                separate(this, ds, ns);
                Operable n = reconstruct(ns);
                Operable d = reconstruct(ds);
                if (n == null) return this;
                if (d == null || d.equals(RawValue.ONE))
                    return n;
                else return new BinaryOperation(n, "/", d);

        }
        this.setLeft(left);
        this.setRight(right);
        return this;
    }

    public BinaryOperation toAdditionOnly() {
        super.toAdditionOnly();
        if (operation.name.equals("-")) {
            operation = RegisteredBinaryOperation.extract("+");
            setRight(getRight().negate());
        }
        return this;
    }

    public double val() {
        return operation.eval(getLeft().val(), getRight().val());
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
        super.toExponentialForm();
        if (!this.is("/")) return this;
        if (getRight().equals(new RawValue(0))) return this;
        if (getRight() instanceof BinaryOperation && ((BinaryOperation) getRight()).is("*")) {
            BinaryOperation enclosed = ((BinaryOperation) getRight());
            enclosed.setLeft(new BinaryOperation(enclosed.getLeft(), "^", new RawValue(-1)));
            enclosed.setRight(new BinaryOperation(enclosed.getRight(), "^", new RawValue(-1)));
        } else this.setRight(new BinaryOperation(getRight(), "^", new RawValue(-1)));
        operation = RegisteredBinaryOperation.extract("*");
        processParentheticalNotation(getLeft(), false);
        processParentheticalNotation(getRight(), true);
        return this;
    }

    @Override
    public Operable firstDerivative(Variable v) {
        switch (operation.name) {
            case "+": // d/dx [f(x) + g(x)] = d/dx(g(x)) - d/dx(g(x))
                return getLeft().firstDerivative(v).add(getRight().firstDerivative(v));
            case "-": // d/dx [f(x) - g(x)] = d/dx(g(x)) - d/dx(g(x))
                return getLeft().firstDerivative(v).sub(getRight().firstDerivative(v));
            case "*": // apply product rule
                // d/dx[f(x)*g(x)] = d/dx(f(x))*g(x) + d/dx(g(x))*f(x)
                return getLeft().firstDerivative(v).mult(getRight())
                        .add(getRight().firstDerivative(v).mult(getLeft()));
            case "/": // apply quotient rule
                // [f(x)/g(x)]' = [g(x)*f'(x) - f(x)*g'(x)]/g(x)^2
                return getRight().mult(getLeft().firstDerivative(v))
                        .sub(getLeft().mult(getRight().firstDerivative(v)))
                        .div(getRight().sq());
            case "^":
                if (getRight().contains(v)) { // x^(x...) use logarithmic differentiation
                    // y = f(x)^g(x)
                    // ln(y) = g(x)*ln(f(x)) --apply implicit differentiation
                    // dy/dx * 1/y = d/dx[g(x)*ln(f(x))]
                    // dy/dx = d/dx[g(x)*ln(f(x))] * y
                    return new UnaryOperation(getLeft(), "ln").mult(getRight())
                            .firstDerivative(v)
                            .mult(this);
                } else {
                    // the exponent part does not contain Variable v
                    // apply power rule -> d/dx(f(x)^n) = n*f(x)^(n-1)
                    return getRight().mult(getLeft().exp(getRight().sub(1)))
                            .mult(getLeft().firstDerivative(v));
                }

        }
        //the derivative cannot be calculated, return symbolic representation indeed
        return new CompositeOperation(Calculus.DERIVATIVE, this.copy(), v);
    }

    public boolean isUndefined() {
        if (super.isUndefined()) return true;
        if (getRight() instanceof RawValue) {
            RawValue r = ((RawValue) getRight());
            switch (operation.name) {
                case "/":
                    return r.isZero();
                case "^":
                    return getLeft() instanceof RawValue && ((RawValue) getLeft()).isZero() && !r.isPositive();
            }
        }
        return false;
    }

    /**
     * HELPER METHOD
     *
     * @param i i == 1 -> getLeft(), i == 2 -> getRight()
     * @return operand Operable
     */
    private Operable get(int i) {
        switch (i) {
            case 1:
                return getLeft();
            case 2:
                return getRight();
        }
        throw new RuntimeException("invalid index");
    }

    /**
     * HELPER METHOD
     *
     * @param i i == 1 -> getRight(), i == 2 -> getLeft()
     * @return operand Operable
     */
    private Operable getOther(int i) {
        if (i != 1 && i != 2) throw new RuntimeException("invalid index");
        return i == 1 ? getRight() : getLeft();
    }

    /**
     * HELPER METHOD
     * whether this BinaryOperation contain the operand as an immediate child
     *
     * @param o Operable operand
     * @return 1 if at left side operand, 2 if at right side operand, 0 if !levelOf.
     */
    private int has(Operable o) {
        if (getLeft().equals(o)) return 1;
        else if (getRight().equals(o)) return 2;
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
        if (getLeft() instanceof Fraction && "+-*/".contains(operation.name)) {
            Fraction f = (Fraction) getLeft().copy();
            RawValue r = (RawValue) getRight().copy();
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
        } else if (getRight() instanceof Fraction && "+-*/".contains(operation.name)) {
            Fraction f = (Fraction) getRight().copy();
            RawValue r = (RawValue) getLeft().copy();
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
            } else if (r1.isInteger() && r2 instanceof Fraction) {
                boolean r1Negative = false;
                if (!r1.isPositive()) {
                    r1Negative = true;
                    r1 = r1.negate();
                }
                ArrayList<int[]> pairs = MathContext.toBaseExponentPairs(r1.intValue());
                Optional<BinaryOperation> reduced = pairs.stream()
                        .filter(p -> p[1] > 1)
                        .map(p -> Operation.exp(p[0], Operation.mult(p[1], r2)))
                        .reduce(Operable::mult);
                if (reduced.isPresent()) {
                    Optional<Integer> retained = pairs.stream()
                            .filter(p -> p[1] == 1)
                            .map(p -> p[0])
                            .reduce((a, b) -> a * b);
                    if (retained.isPresent()) { //TODO: debug negative
                        return Operation.mult(Operation.exp(retained.get() * (r1Negative ? -1 : 1), r2), reduced.get()).simplify();
                    } else {
                        return reduced.get().simplify();
                    }
                }
            }

            if (r2.val() == 0) { // 0^0
                return r1.val() == 0 ? RawValue.UNDEF : new RawValue(1);
            } else if (r2.val() < 0) { // x^-b = (1/x)^b
                return new BinaryOperation(r1.inverse(), "^", r2.negate()).simplify();
            }

            if (r2 instanceof Fraction) {
                double v2 = r2.val();
                if (v2 > 1) {
                    int t = (int) r2.val();
                    Operation o1 = Operation.exp(r1, new RawValue(t));
                    Operation o2 = Operation.exp(r1, Operation.sub(r2, new RawValue(t)));
                    return Operation.mult(o1, o2).simplify();
                }
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

    private Operable simplify(UnaryOperation u1, UnaryOperation u2) {
        if (!u1.getOperand().isNaN() && !u2.getOperand().isNaN()) {
            if (u1.getName().equals(u2.getName()))
                switch (operation.name) {
                    case "+":
                        switch (u1.getName()) {
                            case "log":
                            case "ln":
                                return new UnaryOperation(u1.getOperand().mult(u2.getOperand()), u1.getName()).simplify();
                        }
                    case "-":
                        switch (u1.getName()) {
                            case "log":
                            case "ln":
                                return new UnaryOperation(u1.getOperand().div(u2.getOperand()), u1.getName()).simplify();
                        }
                }
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
                    return getLeft();
                case "-":
                    return getLeft();
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
                    return getLeft();
                case "/":
                    return getLeft();
                case "^":
                    return getLeft();
            }
        }
        return null;
    }

    public String toString() {
        boolean k = getLeft() instanceof RawValue && getLeft().val() < 0;
        boolean q = getRight() instanceof RawValue && getRight().val() < 0;
        String left = k ? "(" + getLeft().toString() + ")" : getLeft().toString();
        String right = q ? "(" + getRight().toString() + ")" : getRight().toString();
        String temp = left + operation.name + right;
        return omitParenthesis ? temp : "(" + temp + ")";
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
        } else if (o instanceof UnaryOperation) {
            UnaryOperation uop = (UnaryOperation) o;
            switch (operation.name) {
                case "^":
                    switch (uop.getName()) {
                        case "log": // (10^n)^log(b) = b^n
                            if (getLeft() instanceof RawValue) {
                                RawValue r = (RawValue) getLeft();
                                double n = new UnaryOperation(r, "log").val();
                                if (r.isInteger() && RawValue.isInteger(n)) {
                                    return Operation.exp(uop.getOperand(), new RawValue(n)).simplify();
                                }
                            }
                        case "ln": // (e^a)^ln(b) = b^a
                            Operable k = new UnaryOperation(getLeft(), "ln").simplify();
                            if (k instanceof RawValue) {
                                RawValue r1 = (RawValue) k;
                                if (r1.isInteger()) {
                                    return Operation.exp(uop.getOperand(), r1).simplify();
                                }
                            }
                    }
            }
        }
        if (getLeft() instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) getLeft();
            switch (operation.name) {
                case "^":
                    switch (binOp.operation.name) {
                        case "*": // (a*b)^# = a^#*b^#
                            if (o instanceof RawValue) {
                                RawValue r = (RawValue) o;
                                BinaryOperation left = new BinaryOperation(binOp.getLeft(), "^", r);
                                BinaryOperation right = new BinaryOperation(binOp.getRight(), "^", r);
                                return new BinaryOperation(left, "*", right).simplify();
                            }
                        case "^": // (a^b^c) = a^(b*c)
                            BinaryOperation exp = new BinaryOperation(binOp.getRight(), "*", o);
                            return new BinaryOperation(binOp.getLeft(), "^", exp).simplify();
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
                        return i == 1 ? RawValue.ONE : getLeft();
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
                                right now it does only 2.
                                */
                            for (int i = 1; i <= 2; i++) {
                                Operable o1 = binOp1.get(i);
                                int idx = binOp2.has(o1);
                                if (idx != 0) {
                                    Operable add = new BinaryOperation(binOp1.getOther(i), "+", binOp2.getOther(idx));
                                    Operable simplified = add.copy().simplify();
                                    if (simplified.complexity() < add.complexity())
                                        return new BinaryOperation(o1, "*", simplified).simplify();
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
                                1. for the form x^(a+b) * x^c, should it be simplified to x^(a+b+c)?
                                2. for the form x^(a+b) * x^(-a), it should definitely be simplified to 2*b*x.
                                right now it does only 2.
                                 */
                            Operable op1Left = binOp1.getLeft();
                            Operable op2Left = binOp2.getLeft();
                            if (op1Left.equals(op2Left)) {
                                Operable add = Operation.add(binOp1.getRight(), binOp2.getRight());
                                Operable simplified = add.copy().simplify();
                                if (simplified.complexity() < add.complexity())
                                    return new BinaryOperation(op1Left, "^", simplified).simplify();
                            }
                            break;
                    }
                    break;
            }
        return null;
    }

    /**
     * HELPER METHOD
     * handles a*a^b = a^(b+1) when a is not a number
     *
     * @param op    generic Operation
     * @param binOp BinaryOperation with operation == "^"
     * @return simplified Operable
     */
    private Operable simplify(Operable op, BinaryOperation binOp) {
        switch (operation.name) {
            case "*":
                if (op.equals(binOp.getLeft()) && binOp.is("^")) {
                    if (!(op instanceof RawValue)) { //only when a is a variable or expression, a*a^b = a^(b+1) applies
                        BinaryOperation exp = new BinaryOperation(binOp.getRight(), "+", RawValue.ONE);
                        return new BinaryOperation(op, "^", exp).simplify();
                    }
                }
                break; // I always forget to put break!!!! So many bugs were born simply because I forgot to put this statement!!!!
            case "+":
                return binOp.ambiguousIteration((o1, o2, operator) -> {
                    if (o1.equals(op) && operator.equals("*")) {
                        return Operation.mult(op, Operation.add(o2, 1)).simplify();
                    }
                    return null;
                });
        }
        return null;
    }

    /**
     * perform ambiguous iteration of left-hand and right-hand. The order does not matter.
     *
     * @param ao an ambiguous operation that does something with left-hand and right-hand
     */
    private Operable ambiguousIteration(AmbiguousOperation ao) {
        for (int i = 1; i <= 2; i++) {
            Operable o = ao.operate(get(i), getOther(i), operation);
            if (o != null) return o;
        }
        return null;
    }

    /**
     * This method shouldn't be used when priority == 1, or with the operation ^, since it is not commutative.
     *
     * @param pool ArrayList containing flattened operables
     */
    private void crossSimplify(ArrayList<Operable> pool) {
        if (!(is("*") || is("+"))) return;
        for (int i = 0; i < pool.size() - 1; i++) {
            Operable operable = pool.get(i);
            for (int k = i + 1; k < pool.size(); k++) {
                Operable other = pool.get(k);
                String operation = getPriority() == 2 ? "*" : "+";
                BinaryOperation binOp = new BinaryOperation(operable, operation, other);
                int n1 = binOp.complexity();
                Operable op = binOp.simplify(); //be careful, avoid stack overflow
                if (op.complexity() < n1) { //simplifiable
                    pool.remove(i);
                    pool.remove(k - 1);
                    pool.add(op);
                    crossSimplify(pool);//result maybe bin tree or just Operable.
                    break;
                } else if (pool.size() <= 2) return;
            }
        }
    }

    /**
     * reconstruct binary operation tree from flattened ArrayList of operations.
     *
     * @return reconstructed BinaryOperation tree.
     */
    private Operable reconstructBinTree(ArrayList<Operable> flattened) {
        if (flattened.size() == 0) throw new JMCException("internal error");
        else if (flattened.size() == 1) return flattened.get(0);
        String op = getPriority() == 2 ? "*" : "+";
        BinaryOperation root = new BinaryOperation(flattened.remove(0), op, flattened.remove(0));
        while (flattened.size() > 0) root = new BinaryOperation(root, op, flattened.remove(0));
        return root.simplifyParenthesis();
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
        BinaryOperation clone = this.copy().toAdditionOnly().toExponentialForm();
        clone.flat(pool, clone.getLeft());
        clone.flat(pool, clone.getRight());
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
        if (operable instanceof BinLeafNode) {
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

    /**
     * HELPER METHOD
     * base method of the recursively defined expand()
     *
     * @return expanded expression of type Operable
     */
    private Operable expandBase() {
        switch (operation.name) {
            case "*": // (a+b)*c || (a+b+...)*(c+d+...) = a*c + a*d + ...
                Operable result = this.ambiguousIteration((o1, o2, operation) -> {
                    if (isAddition(o1) && isAddition(o2)) {
                        BinaryOperation b1 = ((BinaryOperation) o1);
                        BinaryOperation b2 = ((BinaryOperation) o2);
                        return crossExpand(b1, b2);
                    } else if (isAddition(o1)) {
                        return ((BinaryOperation) o1).flattened().stream()
                                .map(o -> Operation.mult(o, o2))
                                .reduce(Operable::add).get();
                    }
                    return null;
                });
                if (result != null) return result;
                break;
            case "^":
                if (getRight() instanceof RawValue && ((RawValue) getRight()).isInteger()) {
                    int num = ((RawValue) getRight()).intValue();
                    if (getLeft() instanceof BinaryOperation) {
                        BinaryOperation binOp = ((BinaryOperation) getLeft());
                        switch (binOp.operation.name) {
                            case "*": //(a*b)^# = a^#*b^#
                                Optional<BinaryOperation> op1 = binOp.flattened().stream()
                                        .map(o -> Operation.exp(o, num))
                                        .reduce(Operable::mult);
                                if (op1.isPresent()) return op1.get();
                                break;
                            case "+": // (a+b)^# = ...
                                ArrayList<Operable> pool = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                    pool.add(binOp.copy());
                                }
                                Optional<Operable> op = pool.stream().reduce(Operation::mult);
                                if (op.isPresent()) return op.get().expand();
                                break;
                        }
                    }
                }
        }
        return this;
    }

    private boolean isVirtuallyNegative(Operable binOp) {
        return binOp.val() < 0 || binOp instanceof BinaryOperation && Operable.contains(((BinaryOperation) binOp.explicitNegativeForm()).flattened(), RawValue.ONE.negate());
    }

    private Operable reconstruct(ArrayList<Operable> operables) {
        try {
            return reconstructBinTree(operables);
        } catch (JMCException e) {
            return null;
        }
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
                numerators.add(binOp.getLeft());
                denominators.add(binOp.getRight());
                return;
            }

            int idx = expFormIdx(binOp);
            switch (idx) {
                case 0:
                    numerators.add(binOp);
                    break;
                case 1:
                    Fraction exp = ((Fraction) binOp.getRight()).negate();
                    BinaryOperation b = new BinaryOperation(binOp.getLeft(), "^", exp);
                    denominators.add(b); //TODO: rationalize irrational denominator
                    break;
                case 2:
                    RawValue r = ((RawValue) binOp.getRight()).negate();
                    if (r.equals(RawValue.ONE)) { //1/n^1 -> 1/n
                        denominators.add(binOp.getLeft());
                        break;
                    }
                    BinaryOperation b1 = new BinaryOperation(binOp.getLeft(), "^", r);
                    denominators.add(b1);
                    break;
                case 3:
                    Operable exp1 = new BinaryOperation(binOp.getRight(), "*", RawValue.ONE.negate()).simplify();
                    BinaryOperation b2 = new BinaryOperation(binOp.getLeft(), "^", exp1);
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
        return o instanceof BinaryOperation && ((BinaryOperation) o).is("/");
    }

    public String getName() {
        return operation.name;
    }

    public boolean is(String s) {
        return operation.equals(s);
    }

    public Operable setLeft(Operable operable) {
        super.setOperand(operable, 0);
        this.simplifyParenthesis();
        return this;
    }

    /**
     * a series of operations in which the order of left, right hands is unimportant.
     */
    private interface AmbiguousOperation {
        Operable operate(Operable o1, Operable o2, RegisteredBinaryOperation operator);
    }

    public interface BinEvaluable {
        double eval(double a, double b);
    }

    private static class RegisteredBinaryOperation implements BinEvaluable, Nameable {
        private static ArrayList<RegisteredBinaryOperation> registeredBinOps;

        static {
            registeredBinOps = new ArrayList<>();
            define("+", 3, (a, b) -> a + b);
            define("-", 3, (a, b) -> a - b);
            define("*", 2, (a, b) -> a * b);
            define("/", 2, (a, b) -> a / b);
            define("^", 1, Math::pow);
            define(",", -1, (a, b) -> Double.NaN);
            if (Mode.DEBUG) System.out.println("# reserved binary operations declared");
        }

        private BinEvaluable binEvaluable;
        private String name;
        private int priority; //1 is the most prioritized
        private String standardOperations = "+-*/^";

        private RegisteredBinaryOperation(String name, int priority, BinEvaluable evaluable) {
            this.name = name;
            this.binEvaluable = evaluable;
            this.priority = priority;
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
            StringBuilder incrementer = new StringBuilder();
            for (RegisteredBinaryOperation operation : registeredBinOps) {
                if (operation.priority == priority)
                    incrementer.append(operation.name);
            }
            return incrementer.toString();
        }

        private static RegisteredBinaryOperation extract(String name) {
            for (RegisteredBinaryOperation binaryOperation : registeredBinOps)
                if (binaryOperation.name.equals(name))
                    return binaryOperation;
            throw new RuntimeException("undefined binary operator \"" + name + "\"");
        }

        private boolean isStandard() {
            return standardOperations.contains(name);
        }

        public double eval(double a, double b) {
            return binEvaluable.eval(a, b);
        }

        public boolean equals(RegisteredBinaryOperation other) {
            return this.name.equals(other.name);
        }

        public boolean equals(String s) {
            return this.name.equals(s);
        }

        public String getName() {
            return name;
        }

    }


    @Override
    public Operable expand() {
        this.toAdditionOnly().toExponentialForm();
        super.expand();
        return expandBase();
    }


    @Override
    public boolean equals(Operable other) {
        if (!(other instanceof BinaryOperation)) return false;
        BinaryOperation binOp = (BinaryOperation) other;
        if (("+-".contains(operation.name) && "+-".contains(binOp.operation.name)) || ("*/".contains(operation.name) && "*/".contains(binOp.operation.name))) {
            ArrayList<Operable> pool1 = this.flattened();
            ArrayList<Operable> pool2 = binOp.flattened();
            if (pool1.size() != pool2.size()) return false;
            return pool1.stream().map(o -> Operable.contains(pool2, o)).reduce((a, b) -> a && b).get();
        } else if (operation.equals(binOp.operation)) {
            return binOp.getLeft().equals(getLeft()) && binOp.getRight().equals(getRight());
        }
        return false;
    }

    @Override
    public Operable replace(Operable o, Operable r) {
        Operable clone = super.replace(o, r);
        if (clone instanceof BinaryOperation) ((BinaryOperation) clone).simplifyParenthesis();
        return clone;
    }

}
