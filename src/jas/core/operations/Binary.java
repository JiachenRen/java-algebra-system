package jas.core.operations;


import jas.MathContext;
import jas.core.*;
import jas.core.components.Fraction;
import jas.core.components.List;
import jas.core.components.RawValue;
import jas.core.components.Variable;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static jas.core.Mode.*;
import static jas.core.components.RawValue.*;
import static jas.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 16/05/2017.
 * added the ability to expand parenthesis and constant expressions. Accomplished May 20th.
 * add the ability to mark unavailable domain.
 * code refactored May 20th. Added ADT for operations.
 */
public class Binary extends Operation {
    private boolean omitParenthesis;
    private Operator operator;

    public Binary(Node leftHand, String operator, Node rightHand) {
        this(leftHand, Operator.extract(operator), rightHand);
    }

    private Binary(Node leftHand, Operator operator, Node rightHand) {
        super(wrap(leftHand, rightHand));
        this.operator = operator;
        omitParenthesis = true;
        simplifyParenthesis();
    }

    /**
     * @param name      name of the new binary operator
     * @param priority  1 is the most prioritized
     * @param evaluable (a,b) -> a [operator] b
     */
    public static void define(String name, int priority, BinEvaluable evaluable) {
        Operator.define(name, priority, evaluable);
    }

    public static int getPriority(String operation) {
        return Operator.extract(operation).priority;
    }

    public static String operators() {
        return operators(0) + operators(1) + operators(2) + operators(3);
    }

    /**
     * @return a complete list of binary operations (with corresponding priority)
     */
    public static String operators(int priority) {
        return Operator.listAsString(priority);
    }

    /**
     * HELPER METHOD
     * this's operator should be "*" to invoke this method
     * (a+b)*(a+ln(c)+d) -> a*a + a*ln(c) + a*d + b*a + b*ln(c) + b*d
     *
     * @param o1 binary operator "+"
     * @param o2 binary operator "+"
     * @return o1*o2 expanded
     */
    private static Node crossExpand(Binary o1, Binary o2) {
        ArrayList<Node> f1 = o1.flattened(), f2 = o2.flattened();
        final Node[] result = new Node[1];
        f1.stream().map(o -> f2.stream()
                .map(p -> Operation.mult(p, o))
                .reduce(Node::add).get())
                .reduce(Node::add)
                .ifPresent(o -> result[0] = o);
        return result[0];
    }

    private static boolean isAddition(Node op) {
        return op instanceof Binary && ((Binary) op).is("+");
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
    public static int expFormIdx(Node o) {
        if (!(o instanceof Binary)) return 0;
        Binary bin = (Binary) o;
        if (!bin.is("^")) return 0;
        if (bin.getRight() instanceof RawValue && bin.getRight().val() < 0)
            return bin.getRight() instanceof Fraction ? 1 : 2;
        if (bin.getRight() instanceof Binary) {
            Binary bin1 = ((Binary) bin.getRight());
            if (bin1.is("*")) {
                ArrayList<Node> pool = ((Binary) bin1.explicitNegativeForm()).flattened();
                if (Node.contains(pool, ONE.negate()))
                    return 3;
            }
        }
        return 0;
    }

    private static boolean needsParenthesis(Node o) {
        return o instanceof RawValue && o.val() < 0;
    }

    /**
     * in ((a+b)+c), the parenthesis around (a+b) is not necessary.
     */
    private Binary simplifyParenthesis() {
        processParentheticalNotation(getLeft(), false);
        processParentheticalNotation(getRight(), true);
        return this;
    }

    /**
     * Remove unnecessary parenthesis
     *
     * @since May 19th, 2017
     */
    private void processParentheticalNotation(Node node, boolean isRightHand) {
        if (node instanceof Binary) {
            Binary op = ((Binary) node);
            if (op.getPriority() < this.getPriority()) {
                op.setOmitParenthesis(true);
            } else if (op.getPriority() == this.getPriority()) {
                if (op.operator.equals(operator) && op.is("^"))
                    op.setOmitParenthesis(false);
                else op.setOmitParenthesis(op.operator.equals(operator) || !isRightHand);
            } else {
                op.setOmitParenthesis(false);
            }
        }
    }

    @Override
    public void order() {
        if (is("*") || is("+")) {
            ArrayList<Node> flattened = flattened();
            flattened.forEach(Node::order);
            flattened = flattened.stream().sorted(Comparator.comparing(Node::toString))
                    .collect(Collectors.toCollection(ArrayList::new));
            Binary ordered = (Binary) reconstructBinTree(flattened);
            setLeft(ordered.getLeft());
            setRight(ordered.getRight());
        }
        setOrdered(true);
    }

    @Override
    public Operation setOperand(Node operand, int i) {
        super.setOperand(operand, i);
        return this;
    }

    public Node getLeft() {
        return getOperand(0);
    }

    public Node getRight() {
        return getOperand(1);
    }

    public void setRight(Node node) {
        setOperand(node, 1);
        simplifyParenthesis();
    }

    public int getPriority() {
        return operator.priority;
    }

    public void setOmitParenthesis(boolean temp) {
        omitParenthesis = temp;
    }

    public double eval(double x) {
        double leftVal = getLeft().eval(x);
        double rightVal = getRight().eval(x);
        return operator.eval(leftVal, rightVal);
    }

    @Override
    public Binary copy() {
        return new Binary(getLeft().copy(), operator, getRight().copy());
    }

    private Node simplifyCommutative() {
        //make operations of the same priority throughout the binary tree visible at the same level.
        //for example, (a+c)+c becomes a+b+c; (a*b)*c becomes a*b*c
        this.toAdditionOnly().toExponentialForm();
        ArrayList<Node> flattened = this.flattened();
        crossSimplify(flattened);
        return reconstructBinTree(flattened);
    }

    /**
     * e.g. (a+b)+c is commutable to a+(b+c)
     *
     * @return whether or not the Binary should be flattened for further simplification.
     */
    private boolean isCommutable() {
        return (is("*") || is("+")) && (((getLeft() instanceof Binary) && ((Binary) getLeft()).is(operator.name))
                || ((getRight() instanceof Binary) && ((Binary) getRight()).is(operator.name)));
    }

    /**
     * Note: modifies self, but may not
     *
     * @return the simplified version of self
     */
    public Node simplify() {
        if (isCommutable())
            return simplifyCommutative();
        super.simplify();

        // delegate simplification to List wherever possible
        // this is after super.simplify to prevent redundant work?
        if (getLeft() instanceof List && getRight() instanceof List) {
            List left = ((List) getLeft());
            List right = ((List) getRight());
            return left.binOp(this, right).simplify();
        } else if (getLeft() instanceof List) {
            return ((List) getLeft()).binOp(this, getRight(), true).simplify();
        } else if (getRight() instanceof List) {
            return ((List) getRight()).binOp(this, getLeft(), false).simplify();
        }


        if (!operator.isStandard())
            // nothing could be done with non-standard operations, except when it reduces to a RawValue.
            return Double.isNaN(val()) ? this : new RawValue(val());
        if (isUndefined()) return UNDEF;

        if (getLeft() instanceof RawValue && getRight() instanceof RawValue) {
            Node simplified = simplify((RawValue) getLeft(), (RawValue) getRight());
            if (simplified != null) return simplified;
        }

        if (getLeft() instanceof Unary && getRight() instanceof Unary) {
            Node simplified = simplify(((Unary) getLeft()), ((Unary) getRight()));
            if (simplified != null) return simplified;
        }

        //at this point neither left hand nor right hand is undefined.
        //simplifyRightHand() -> for subtraction and exponentiation, the position of left and right hand is not interchangeable.
        Node simplified1 = simplifyRightHand(getRight());
        if (simplified1 != null) return simplified1;


        if (getLeft().equals(getRight())) {
            switch (operator.name) {
                case "+":
                    return new Binary(new RawValue(2), "*", getLeft());
                case "-":
                    return ZERO;
                case "*":
                    return new Binary(getLeft(), "^", new RawValue(2));
                case "/":
                    return ONE;
            }
        }

        //converting to exponential form and additional only, allowing further simplification.
        this.toAdditionOnly().toExponentialForm();

        //handle special cases
        Node simplified2 = simplifyZeroOne();
        if (simplified2 != null) return simplified2;

        if (getLeft() instanceof Binary && getRight() instanceof Binary) {
            Binary bin1 = (Binary) getLeft();
            Binary bin2 = (Binary) getRight();
            Node simplified = simplify(bin1, bin2);
            if (simplified != null) return simplified;
        }

        if (getLeft() instanceof Binary) {
            Node simplified = simplify(getRight(), (Binary) getLeft());
            if (simplified != null) return simplified;
        }
        if (getRight() instanceof Binary) {
            Node simplified = simplify(getLeft(), (Binary) getRight());
            if (simplified != null) return simplified;
        }

        if (isCommutable())
            return simplifyCommutative();

        return simplifyParenthesis();
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
    public Node beautify() {
        if (getRight() instanceof RawValue && is("*")) {
            //keeps numbers to the left side of the operator
            flip(); // there is space for improvement...
        }
        Node left = getLeft().beautify();
        Node right = getRight().beautify();
        switch (operator.name) {
            case "*":
                ArrayList<Node> numerators = new ArrayList<>();
                ArrayList<Node> denominators = new ArrayList<>();
                separate(left, denominators, numerators);
                separate(right, denominators, numerators);
                Node numerator = reconstruct(numerators);
                Node denominator = reconstruct(denominators);
                if (denominator == null || denominator.equals(ONE))
                    return numerator;
                else {
                    if (numerator == null) numerator = ONE;
                    return new Binary(numerator, "/", denominator);
                }
            case "+":
                if (isVirtuallyNegative(left) && !isVirtuallyNegative(right)) {
                    Node right1 = new Binary(left, "*", ONE.negate()).simplify();
                    return new Binary(right, "-", right1).beautify();
                } else if (isVirtuallyNegative(right) && !isVirtuallyNegative(left)) {
                    Node right1 = new Binary(right, "*", ONE.negate()).simplify();
                    return new Binary(left, "-", right1).beautify();
                }
            case "^":
                ArrayList<Node> ns = new ArrayList<>();
                ArrayList<Node> ds = new ArrayList<>();
                this.setLeft(left);
                this.setRight(right);

                separate(this, ds, ns);
                Node n = reconstruct(ns);
                Node d = reconstruct(ds);
                if (n == null) return this;
                if (d == null || d.equals(ONE))
                    return n;
                else return new Binary(n, "/", d);

        }
        this.setLeft(left);
        this.setRight(right);
        return this;
    }

    private void flip() {
        Node tmp = getLeft();
        setLeft(getRight());
        setRight(tmp);
    }

    public Binary toAdditionOnly() {
        super.toAdditionOnly();
        if (operator.name.equals("-")) {
            operator = Operator.extract("+");
            setRight(getRight().negate().simplify());
        }
        return this;
    }

    public double val() {
        return operator.eval(getLeft().val(), getRight().val());
    }

    /**
     * for example, "(x-4)(x-5)/(x-3/(x-6))" would first transformed into
     * "(x-4)(x-5)(x-3/(x-6))^-1", and then recursively reduced to
     * "(x-4)(x-5)(x-3*(x-6)^-1)^-1"
     * basic CAS capabilities. Implementation began: May 19th.
     * NOTE: modifies self.
     */
    @Override
    public Binary toExponentialForm() {
        super.toExponentialForm();
        if (!this.is("/")) return this;
        if (getRight().equals(new RawValue(0))) return this; //x/0 cannot be converted to exponential form
        this.setRight(getRight().exp(new RawValue(-1)).simplify());
        operator = Operator.extract("*");
        simplifyParenthesis();
        return this;
    }

    @Override
    public Node firstDerivative(Variable v) {
        switch (operator.name) {
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
                    return new Unary(getLeft(), "ln").mult(getRight())
                            .firstDerivative(v)
                            .mult(this);
                } else {
                    // the exponent part does not contain Variable v
                    // apply power rule -> d/dx(f(x)^n) = n*f(x)^(n-1)
                    return getRight().mult(getLeft().exp(getRight().sub(1)))
                            .mult(getLeft().firstDerivative(v));
                }

        }
        //the derivative cannot be calculated, return symbolic representation instead
        return new Custom(Calculus.DERIVATIVE, this.copy(), v);
    }

    public boolean isUndefined() {
        if (super.isUndefined()) return true;
        if (getRight() instanceof RawValue) {
            RawValue r = ((RawValue) getRight());
            switch (operator.name) {
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
     * @return operand Node
     */
    private Node get(int i) {
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
     * @return operand Node
     */
    private Node getOther(int i) {
        if (i != 1 && i != 2) throw new RuntimeException("invalid index");
        return i == 1 ? getRight() : getLeft();
    }

    /**
     * HELPER METHOD
     * whether this Binary contain the operand as an immediate child
     *
     * @param o Node operand
     * @return 1 if at left side operand, 2 if at right side operand, 0 if !levelOf.
     */
    private int has(Node o) {
        if (getLeft().equals(o)) return 1;
        else if (getRight().equals(o)) return 2;
        return 0;
    }

    /**
     * HELPER METHOD
     *
     * @param r1 RawValue #1
     * @param r2 RawValue #2
     * @return simplified r1 [Operator] r2
     */
    private Node simplify(RawValue r1, RawValue r2) {
        if (!FRACTION) return new RawValue(val());
        if (getLeft() instanceof Fraction && isCommutative()) {
            Fraction f = (Fraction) getLeft().copy();
            RawValue r = (RawValue) getRight().copy();
            switch (operator.name) {
                case "+":
                    return f.add(r);
                case "-":
                    return f.sub(r);
                case "*":
                    return f.mult(r);
                case "/":
                    return f.div(r);
            }
        } else if (getRight() instanceof Fraction && isCommutative()) {
            Fraction f = (Fraction) getRight().copy();
            RawValue r = (RawValue) getLeft().copy();
            switch (operator.name) {
                case "+":
                    return f.add(r);
                case "-":
                    return f.negate().add(r);
                case "*":
                    return f.mult(r);
                case "/":
                    return f.inverse().mult(r);
            }
        } else if (operator.name.equals("^")) { //fractional mode
            if (r1 instanceof Fraction) {
                return ((Fraction) r1).exp(r2);
            } else if (r1.isInteger() && r2 instanceof Fraction) {
                if (r1.val() == 0) return ZERO; // 0^x = 0, as long as x != 0
                boolean r1Negative = false;
                if (!r1.isPositive()) {
                    r1Negative = true;
                    r1 = r1.negate();
                }
                ArrayList<int[]> pairs = MathContext.toBaseExponentPairs(r1.toBigInteger());
                Optional<Binary> reduced = pairs.stream()
                        .filter(p -> p[1] > 1)
                        .map(p -> Operation.exp(p[0], Operation.mult(p[1], r2)))
                        .reduce(Node::mult);

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
                return r1.val() == 0 ? UNDEF : new RawValue(1);
            } else if (r2.val() < 0) { // x^-b = (1/x)^b
                return new Binary(r1.inverse(), "^", r2.negate()).simplify();
            }

            if (r2 instanceof Fraction) {
                double v2 = r2.val();
                if (v2 > 1) {
                    long t = (long) r2.val();
                    Operation o1 = Operation.exp(r1, new RawValue(t));
                    Operation o2 = Operation.exp(r1, Operation.sub(r2, new RawValue(t)));
                    return Operation.mult(o1, o2).simplify();
                }
            }
        }


        if (r1.isInteger() && r2.isInteger()) {
            if (operator.name.equals("/")) {
                return new Fraction(r1.longValue(), r2.longValue()).reduce();
            } else return new RawValue(operator.eval(r1.longValue(), r2.longValue()));
        } else if (!r1.isInteger() && !(r1 instanceof Fraction)) {
            RawValue f1 = Fraction.convertToFraction(r1.doubleValue(), Fraction.TOLERANCE);
            return new Binary(f1, operator, r2).simplify();
        } else if (!r2.isInteger() && !(r2 instanceof Fraction)) {
            RawValue f2 = Fraction.convertToFraction(r2.doubleValue(), Fraction.TOLERANCE);
            return new Binary(r1, operator, f2).simplify();
        }
        return null;
    }

    private Node simplify(Unary u1, Unary u2) {
        if (!u1.getOperand().isNaN() && !u2.getOperand().isNaN()) {
            if (u1.getName().equals(u2.getName()))
                switch (operator.name) {
                    case "+":
                        switch (u1.getName()) {
                            case "log":
                            case "ln":
                                return new Unary(u1.getOperand().mult(u2.getOperand()), u1.getName()).simplify();
                        }
                    case "-":
                        switch (u1.getName()) {
                            case "log":
                            case "ln":
                                return new Unary(u1.getOperand().div(u2.getOperand()), u1.getName()).simplify();
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
     * @return simplified Node
     */
    private Node simplifyRightHand(long i) {
        if (i == 0) {
            switch (operator.name) {
                case "+":
                    return getLeft();
                case "-":
                    return getLeft();
                case "*":
                    return ZERO;
                case "/":
                    return UNDEF;
                case "^":
                    return ONE;
            }
        } else if (i == 1) {
            switch (operator.name) {
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
        boolean k = needsParenthesis(getLeft());
        boolean q = needsParenthesis(getRight());
        String left = k ? "(" + getLeft().toString() + ")" : getLeft().toString();
        String right = q ? "(" + getRight().toString() + ")" : getRight().toString();
        String temp = left + (Mode.COMPACT ? "" : " ") + operator.name + (Mode.COMPACT ? "" : " ") + right;
        return omitParenthesis ? temp : "(" + temp + ")";
    }

    /**
     * HELPER METHOD
     * handles a^b^c = a^(b*c), (a*b)^# = a^#*b^#
     *
     * @param o right hand operand of the binary operation
     * @return simplified self of type Node
     */
    private Node simplifyRightHand(Node o) {
        if (o instanceof RawValue && ((RawValue) o).isInteger()) {
            RawValue r = (RawValue) o;
            Node simplified = simplifyRightHand(r.longValue());
            if (simplified != null) return simplified;
        } else if (o instanceof Unary) {
            Unary uop = (Unary) o;
            switch (operator.name) {
                case "^":
                    switch (uop.getName()) {
                        case "log": // (10^n)^log(b) = b^n
                            if (getLeft() instanceof RawValue) {
                                RawValue r = (RawValue) getLeft();
                                double n = new Unary(r, "log").val();
                                if (r.isInteger() && isInteger(n)) {
                                    return Operation.exp(uop.getOperand(), new RawValue(n)).simplify();
                                }
                            }
                            break;
                        case "ln": // (e^a)^ln(b) = b^a
                            Node k = new Unary(getLeft(), "ln").simplify();
                            if (k instanceof RawValue) {
                                RawValue r1 = (RawValue) k;
                                if (r1.isInteger()) {
                                    return Operation.exp(uop.getOperand(), r1).simplify();
                                }
                            }
                    }
            }
        }
        if (getLeft() instanceof Binary) {
            Binary bin = (Binary) getLeft();
            switch (operator.name) {
                case "^":
                    switch (bin.operator.name) {
                        case "*": // (a*b)^# = a^#*b^#
                            if (o instanceof RawValue) {
                                RawValue r = (RawValue) o;
                                Binary left = new Binary(bin.getLeft(), "^", r);
                                Binary right = new Binary(bin.getRight(), "^", r);
                                return new Binary(left, "*", right).simplify();
                            }
                            break;
                        case "^": // (a^b^c) = a^(b*c)
                            Binary exp = new Binary(bin.getRight(), "*", o);
                            return new Binary(bin.getLeft(), "^", exp).simplify();
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
    private Node simplifyZeroOne() {
        for (int i = 1; i <= 2; i++) {
            if (get(i).val() == 0) {
                switch (operator.name) {
                    case "+":
                        return getOther(i); //should this call .simplify()?
                    case "*":
                        return new RawValue(0);
                    case "^":
                        return i == 1 ? ZERO : ONE;
                }
            } else if (get(i).equals(ONE)) {
                switch (operator.name) {
                    case "*":
                        return getOther(i);
                    case "^":
                        return i == 1 ? ONE : getLeft();
                }
            }
        }

        switch (operator.name) {
            case "-":
                if (getLeft().equals(ZERO)) {
                    return getRight().negate();
                } else if (getRight().equals(ZERO)) {
                    return getLeft();
                }
            case "/":
                if (getRight().equals(ZERO)) return UNDEF; // x/0 = undef
                else if (getRight().equals(ONE)) return getLeft(); // x/1 = x
                else if (getLeft().equals(ZERO)) return ZERO; // 0/x = 0
        }
        return null;
    }

    /**
     * HELPER METHOD
     *
     * @param bin1 left hand operand of type Binary
     * @param bin2 right hand operand of type Binary
     * @return simplified self of type Node
     */
    private Node simplify(Binary bin1, Binary bin2) {
        if (bin1.operator.equals(bin2.operator)) //e.g. x*a + x*b, "*" == "*"
            switch (operator.name) {
                case "+":
                    switch (bin1.operator.name) {
                        case "*":
                                /*
                                1. for the form x*(a+b) + x*c, should it be simplified to x*(a+b+c)?
                                2. for the form x*(a+b) + x*(b-a), it should definitely be simplified to 2*b*x.
                                right now it does only 2.
                                */
                            for (int i = 1; i <= 2; i++) {
                                Node o1 = bin1.get(i);
                                int idx = bin2.has(o1);
                                if (idx != 0) {
                                    Node add = new Binary(bin1.getOther(i), "+", bin2.getOther(idx));
                                    Node simplified = add.copy().simplify();
                                    if (simplified.complexity() < add.complexity())
                                        return new Binary(o1, "*", simplified).simplify();
                                }
                            }
                            break;
                        case "^":
                            break;

                    }
                    break;
                case "*":
                    switch (bin1.operator.name) {
                        case "^":
                                /*
                                1. for the form x^(a+b) * x^c, should it be simplified to x^(a+b+c)?
                                2. for the form x^(a+b) * x^(-a), it should definitely be simplified to 2*b*x.
                                right now it does only 2.
                                 */
                            Node op1Left = bin1.getLeft();
                            Node op2Left = bin2.getLeft();
                            if (op1Left.equals(op2Left)) {
                                Node add = Operation.add(bin1.getRight(), bin2.getRight());
                                Node simplified = add.copy().simplify();
                                if (simplified.complexity() < add.complexity())
                                    return new Binary(op1Left, "^", simplified).simplify();
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
     * @param op  generic Operation
     * @param bin Binary with operator == "^"
     * @return simplified Node
     */
    private Node simplify(Node op, Binary bin) {
        switch (operator.name) {
            case "*":
                if (op.equals(bin.getLeft()) && bin.is("^")) {
                    if (!(op instanceof RawValue)) { //only when a is a variable or expression, a*a^b = a^(b+1) applies
                        Binary exp = new Binary(bin.getRight(), "+", ONE);
                        return new Binary(op, "^", exp).simplify();
                    }
                }
                break; // I always forget to put break!!!! So many bugs were born simply because I forgot to put this statement!!!!
            case "+":
                return bin.ambiguousIteration((o1, o2, operator) -> {
                    if (o1.equals(op) && operator.equals("*") && !(o1 instanceof RawValue)) {
                        // prevent 2*ln(x)+2 be simplified to 2*(ln(x)+1). NOTE: only happens if (o + 1) is simplifiable.
                        Operation add = o2.add(1);
                        if (add.copy().simplify().complexity() < add.complexity())
                            return Operation.mult(op, add).simplify();
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
    private Node ambiguousIteration(AmbiguousOperation ao) {
        for (int i = 1; i <= 2; i++) {
            Node o = ao.operate(get(i), getOther(i), operator);
            if (o != null) return o;
        }
        return null;
    }

    /**
     * This method shouldn't be used when priority == 1, or with the operator ^, since it is not commutative.
     *
     * @param pool ArrayList containing flattened nodes
     */
    private void crossSimplify(ArrayList<Node> pool) {
        if (!(is("*") || is("+"))) return;
        for (int i = 0; i < pool.size() - 1; i++) {
            Node node = pool.get(i);
            for (int k = i + 1; k < pool.size(); k++) {
                Node other = pool.get(k);
                String operation = getPriority() == 2 ? "*" : "+";
                Binary bin = new Binary(node, operation, other);
                int n1 = bin.complexity();
                Node op = bin.simplify(); //be careful, avoid stack overflow
                if (op.complexity() < n1) { //simplifiable
                    pool.remove(i);
                    pool.remove(k - 1);
                    pool.add(op);
                    crossSimplify(pool);//result maybe bin tree or just Node.
                    break;
                }
            }
        }
    }

    /**
     * reconstruct binary operation tree from flattened ArrayList of operations.
     *
     * @return reconstructed Binary tree.
     */
    private Node reconstructBinTree(ArrayList<Node> flattened) {
        if (flattened.size() == 0) throw new JMCException("internal error");
        else if (flattened.size() == 1) return flattened.get(0);
        String op = getPriority() == 2 ? "*" : "+";
        Binary root = new Binary(flattened.remove(0), op, flattened.remove(0));
        while (flattened.size() > 0) root = new Binary(root, op, flattened.remove(0));
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
    public ArrayList<Node> flattened() {
        ArrayList<Node> pool = new ArrayList<>();
        if (operator.priority == 1 || !operator.isStandard())
            return pool; //if the operator is ^, then no commutative property applies.
        this.toAdditionOnly().toExponentialForm();
        flat(pool, getLeft());
        flat(pool, getRight());
        return pool;
    }

    /**
     * this method is specific to binary operator because it tears down the binary tree
     * and extracts nodes of the same binary operator priority, making applying commutative properties
     * of + and * possible.
     *
     * @param pool pool of flattened binary tree nodes.
     * @param node the binary tree to be flattened
     */
    private void flat(ArrayList<Node> pool, Node node) {
        if (node instanceof Binary) {
            Binary bin = ((Binary) node);
            if (bin.getPriority() == this.getPriority()) {
                pool.addAll(bin.flattened());
            } else {
                pool.add(bin);
            }
        } else {
            pool.add(node);
        }
    }

    /**
     * HELPER METHOD
     * base method of the recursively defined expand()
     *
     * @return expanded expression of type Node
     */
    private Node expandBase() {
        switch (operator.name) {
            case "*": // (a+b)*c || (a+b+...)*(c+d+...) = a*c + a*d + ...
                Node result = this.ambiguousIteration((o1, o2, operation) -> {
                    if (isAddition(o1) && isAddition(o2)) {
                        Binary b1 = ((Binary) o1);
                        Binary b2 = ((Binary) o2);
                        return crossExpand(b1, b2);
                    } else if (isAddition(o1)) {
                        return ((Binary) o1).flattened().stream()
                                .map(o -> Operation.mult(o, o2))
                                .reduce(Node::add).get();
                    }
                    return null;
                });
                if (result != null) return result;
                break;
            case "^":
                if (getRight() instanceof RawValue && ((RawValue) getRight()).isInteger()) {
                    long num = ((RawValue) getRight()).longValue();
                    if (getLeft() instanceof Binary) {
                        Binary bin = ((Binary) getLeft());
                        switch (bin.operator.name) {
                            case "*": //(a*b)^# = a^#*b^#
                                Optional<Binary> op1 = bin.flattened().stream()
                                        .map(o -> Operation.exp(o, num))
                                        .reduce(Node::mult);
                                if (op1.isPresent()) return op1.get();
                                break;
                            case "+": // (a+b)^# = ...
                                ArrayList<Node> pool = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                    pool.add(bin.copy());
                                }
                                Optional<Node> op = pool.stream().reduce(Operation::mult);
                                if (op.isPresent()) return op.get().expand();
                                break;
                        }
                    }
                }
        }
        return this;
    }

    private boolean isVirtuallyNegative(Node bin) {
        return bin.val() < 0 || bin instanceof Binary && Node.contains(((Binary) bin.explicitNegativeForm()).flattened(), ONE.negate());
    }

    private Node reconstruct(ArrayList<Node> nodes) {
        try {
            return reconstructBinTree(nodes);
        } catch (JMCException e) {
            return null;
        }
    }

    /**
     * separates denominator and numerator
     *
     * @param o            the Node to be separated
     * @param denominators ArrayList containing denominators
     * @param numerators   ArrayList containing numerators
     */
    private void separate(Node o, ArrayList<Node> denominators, ArrayList<Node> numerators) {
        if (o instanceof Binary) {
            Binary bin = (Binary) o;
            if (isDivision(bin)) {
                numerators.add(bin.getLeft());
                denominators.add(bin.getRight());
                return;
            }

            int idx = expFormIdx(bin);
            switch (idx) {
                case 0:
                    numerators.add(bin);
                    break;
                case 1:
                    Fraction exp = ((Fraction) bin.getRight()).negate();
                    Binary b = new Binary(bin.getLeft(), "^", exp);
                    denominators.add(b); //TODO: rationalize irrational denominator
                    break;
                case 2:
                    RawValue r = ((RawValue) bin.getRight()).negate();
                    if (r.equals(ONE)) { //1/n^1 -> 1/n
                        denominators.add(bin.getLeft());
                        break;
                    }
                    Binary b1 = new Binary(bin.getLeft(), "^", r);
                    denominators.add(b1);
                    break;
                case 3:
                    Node exp1 = new Binary(bin.getRight(), "*", ONE.negate()).simplify();
                    Binary b2 = new Binary(bin.getLeft(), "^", exp1);
                    denominators.add(b2);
                    break;
            }
        } else {
            if (o instanceof Fraction) {
                Fraction f = (Fraction) o;
                if (!f.getNumerator().equals(BigInteger.ONE))
                    numerators.add(new RawValue(f.getNumerator().doubleValue()));
                if (!f.getDenominator().equals(BigInteger.ONE))
                    denominators.add(new RawValue(f.getDenominator().doubleValue()));
            } else if (o.val() != 1) {
                numerators.add(o);
            }
        }
    }

    /**
     * HELPER METHOD
     * "a/b" returns true
     *
     * @return whether operator of o is "/"
     */
    private boolean isDivision(Node o) {
        return o instanceof Binary && ((Binary) o).is("/");
    }

    public String getName() {
        return operator.name;
    }

    public boolean is(String s) {
        return operator.equals(s);
    }

    public Node setLeft(Node node) {
        super.setOperand(node, 0);
        simplifyParenthesis();
        return this;
    }

    @Override
    public Node expand() {
        this.toAdditionOnly().toExponentialForm();
        super.expand();
        return expandBase();
    }

    /**
     * @return string representation of the node coded with Ansi color codes.
     */
    @Override
    public String coloredString() {
        String left = getLeft().coloredString();
        String right = getRight().coloredString();
        left = needsParenthesis(getLeft()) ? coloredParenthesis(left) : left;
        right = needsParenthesis(getRight()) ? coloredParenthesis(right) : right;
        String tmp = left + (Mode.COMPACT ? "" : " ") + color(operator.name, BIN_OP_COLOR) + (Mode.COMPACT ? "" : " ") + right;
        tmp = omitParenthesis ? tmp : coloredParenthesis(tmp);
        return tmp;
    }

    public boolean isCommutative() {
        return "+-*/".contains(operator.name);
    }

//    @Override
//    public boolean equals(Node other) {
//        if (!(other instanceof Binary)) return false;
//        Binary bin = (Binary) other;
//        if (this.isCommutative() && bin.isCommutative() && this.getPriority() == bin.getPriority()) {
//            ArrayList<Node> pool1 = this.flattened();
//            ArrayList<Node> pool2 = bin.flattened();
//            if (pool1.size() != pool2.size()) return false;
//            return pool1.stream().map(o -> Node.remove(pool2, o)).reduce((a, b) -> a && b).get();
//        } else if (operator.equals(bin.operator)) {
//            return bin.getLeft().equals(getLeft()) && bin.getRight().equals(getRight());
//        }
//        return false;
//    }

    /**
     * over 20% improvement in speed over the other version!
     *
     * @param other another Node
     * @return whether the two instances are canonical forms of each other
     */
    @Override
    public boolean equals(Node other) {
        if (!(other instanceof Binary)) return false;
        Binary bin = (Binary) other;
        if (operator.equals(bin.operator)) {
            if (!bin.isOrdered()) bin.order();
            if (!isOrdered()) order();
            return bin.getLeft().equals(getLeft()) && bin.getRight().equals(getRight());
        }
        return false;
    }

    @Override
    public Node replace(Node o, Node r) {
        Node clone = super.replace(o, r);
        if (clone instanceof Binary) ((Binary) clone).simplifyParenthesis();
        return clone;
    }

    /**
     * a series of operations in which the order of left, right hands is unimportant.
     */
    private interface AmbiguousOperation {
        Node operate(Node o1, Node o2, Operator operator);
    }

    public interface BinEvaluable {
        double eval(double a, double b);
    }

    public static class Operator implements BinEvaluable, Nameable {
        private static Map<String, Operator> registeredBinOps;

        static {
            registeredBinOps = new HashMap<>();
            define("+", 3, (a, b) -> a + b);
            define("-", 3, (a, b) -> a - b);
            define("*", 2, (a, b) -> a * b);
            define("/", 2, (a, b) -> a / b);
            define("^", 1, Math::pow);
            define(",", -1, (a, b) -> Double.NaN);
            if (DEBUG) System.out.println("# reserved binary operations declared");
        }

        private BinEvaluable binEvaluable;
        private String name;
        private int priority; //1 is the most prioritized
        private String standardOperations = "+-*/^";

        private Operator(String name, int priority, BinEvaluable evaluable) {
            this.name = name;
            this.binEvaluable = evaluable;
            this.priority = priority;
        }

        private static void define(String name, int priority, BinEvaluable evaluable) {
            registeredBinOps.remove(name);
            registeredBinOps.put(name, new Operator(name, priority, evaluable));
        }

        private static String listAsString(int priority) {
            StringBuilder incrementer = new StringBuilder();
            for (Operator operation : registeredBinOps.values()) {
                if (operation.priority == priority)
                    incrementer.append(operation.name);
            }
            return incrementer.toString();
        }

        private static Operator extract(String name) {
            Operator bin = registeredBinOps.get(name);
            if (bin == null) throw new RuntimeException("undefined binary operator \"" + name + "\"");
            return bin;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean isStandard() {
            return standardOperations.contains(name);
        }

        public double eval(double a, double b) {
            return binEvaluable.eval(a, b);
        }

        public boolean equals(Operator other) {
            return this.name.equals(other.name);
        }

        public boolean equals(String s) {
            return this.name.equals(s);
        }

        public String getName() {
            return name;
        }

    }

}
