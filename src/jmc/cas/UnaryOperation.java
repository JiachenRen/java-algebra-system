package jmc.cas;

import jmc.Function;
import jmc.MathContext;

import java.util.ArrayList;

import static java.lang.Math.*;
import static jmc.MathContext.*;

/**
 * Created by Jiachen on 16/05/2017.
 * code refactored May 20th, performance enhanced with static function lib and method reference.
 * breakthrough May 20th.
 */
public class UnaryOperation extends Operation implements LeafNode {

    private Function operation;

    public UnaryOperation(Operable leftHand, String operation) {
        this(leftHand, RegisteredUnaryOperation.extract(operation));
    }

    /**
     * Instantiating using this constructor might compromise CAS capabilities.
     * This constructor accepts non-JMC standard functions; This means that as long as the function
     * of concern returns a value, it would be valid.
     *
     * @param leftHand  e.g. "x" in "log(x)"
     * @param operation "log" in "log(x)"
     */
    public UnaryOperation(Operable leftHand, Function operation) {
        super(leftHand);
        this.operation = operation;
    }

    /**
     * @param x input, double x
     * @return the computed value by plugging in the value of x into the designated unary operation
     */
    public double eval(double x) {
        return operation.eval(getLeftHand().eval(x));
    }


    public static void define(String name, String expression) {
        UnaryOperation.define(name, Expression.interpret(expression));
    }

    public static void define(String name, Evaluable evaluable) {
        RegisteredUnaryOperation.define(name, evaluable);
    }

    /**
     * List of built-in operations:
     * -> cos, acos(cos^-1), sin, asin, tan, atan, sec, csc, cot, log, int, abs, ln, cosh, sinh, tanh, !(factorial)
     *
     * @return an ArrayList containing all of the defined unary operations.
     */
    public static ArrayList<Function> registeredOperations() {
        return RegisteredUnaryOperation.list();
    }

    /**
     * Note: modifies self
     *
     * @return exponential form of self
     */
    @Override
    public Operable toExponentialForm() {
        if (getLeftHand() instanceof Operation) {
            this.setLeftHand(((Operation) getLeftHand()).toExponentialForm());
            return this;
        } else return this;
    }

    /**
     * Note: modifies self.
     * Only delegates downward if it contains an operation.
     *
     * @return a new Operable instance that is the addition only form of self.
     */
    public Operable toAdditionOnly() {
        if (getLeftHand() instanceof Operation) {
            this.setLeftHand(((Operation) this.getLeftHand()).toAdditionOnly());
            return this;
        }
        return this;
    }

    /**
     * @param operable the Operable instance to be negated. IT IS NOT MODIFIED.
     * @return a new Operable instance that represents the negated version of the original
     */
    public static Operable negate(Operable operable) {
        return new BinaryOperation(new RawValue(-1), "*", operable);
    }


    public String toString() {
        return operation.getName() + "(" + getLeftHand().toString() + ")";
    }

    /**
     * Note: modifies self.
     * TODO: ln(a) - ln(b) should be ln(a/b)
     * TODO: log(225) should be 2log(15)
     *
     * @return a new Operable instance that is the simplified version of self.
     */
    public Operable simplify() {
        if (getLeftHand() instanceof Operation) {
            this.setLeftHand((this.getLeftHand()).simplify());
        }

        if (this.isUndefined()) return RawValue.UNDEF;

        double val = this.val();
        if (RawValue.isInteger(val))
            return new RawValue(val);

        if (getLeftHand() instanceof UnaryOperation) {
            UnaryOperation op = (UnaryOperation) getLeftHand();
            switch (this.operation.getName()) {
                case "acos":
                    switch (op.operation.getName()) {
                        case "cos":
                            return op.getLeftHand();
                    }
                    break;
                case "asin":
                    switch (op.operation.getName()) {
                        case "sin":
                            return op.getLeftHand();
                    }
                case "atan":
                    switch (op.operation.getName()) {
                        case "tan":
                            return op.getLeftHand(); // what about tan(pi/2)?
                    }
            }
        } else if (getLeftHand() instanceof Constants.Constant) {
            Constants.Constant c = (Constants.Constant) getLeftHand();
            switch (c.getName()) {
                case "e":
                    switch (operation.getName()) {
                        case "ln":
                            return RawValue.ONE;
                    }
                    break;
                case "pi":
                    switch (operation.getName()) {
                        case "cos":
                        case "sec":
                            return RawValue.ONE.negate();
                        case "sin":
                        case "tan":
                            return RawValue.ZERO;
                    }
            }
        } else if (getLeftHand() instanceof RawValue) {
            RawValue r = (RawValue) getLeftHand();
            if (r.isInteger()) {
                switch (operation.getName()) {
                    case "ln": // ln(a^3) = 3ln(a), where a is a^3 is an integer
                    case "log":
                        ArrayList<Long> factors = getFactors(r.intValue());
                        if (factors.size() > 1) {
                            ArrayList<Long> uniqueFactors = getUniqueFactors(factors);
                            int[] num = numOccurrences(uniqueFactors, factors);
                            if (allTheSame(num)) {
                                RawValue r1 = new RawValue(MathContext.mult(uniqueFactors));
                                return Operable.mult(new RawValue(num[0]), new UnaryOperation(r1, operation));
                            }
                        }
                }
            }

        } else if (getLeftHand() instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) getLeftHand();
            switch (operation.getName()) {
                case "ln":
                    if (binOp.is("^") && binOp.getLeftHand().equals(Constants.getConstant("e"))) {
                        return binOp.getRightHand();
                    }
            }
        }


        return this;
    }

    @Override
    public UnaryOperation clone() {
        return new UnaryOperation(getLeftHand(), operation);
    }

    private static class RegisteredUnaryOperation implements Evaluable {
        private static ArrayList<Function> reservedFunctions;
        private Function unaryOperation;

        static {
            reservedFunctions = new ArrayList<>();
            define("cos", Math::cos);
            define("sin", Math::sin);
            define("log", Math::log10);
            define("int", Math::floor);
            define("tan", Math::tan);
            define("atan", Math::atan);
            define("asin", Math::asin);
            define("acos", Math::acos);
            define("abs", Math::abs);
            define("ln", x -> log(x) / Constants.valueOf("e"));
            define("sec", x -> 1 / cos(x));
            define("csc", x -> 1 / sin(x));
            define("cot", x -> 1 / tan(x));
            define("!", x -> MathContext.factorial(abs((long) x)));
            define("cosh", Math::cosh);
            define("sinh", Math::sinh);
            define("tanh", Math::tanh);
            System.out.println("# reserved unary operations declared");
        }

        RegisteredUnaryOperation(String name) {
            for (Function function : reservedFunctions) {
                if (function.getName().equals(name))
                    unaryOperation = function;
            }
        }

        @Override
        public double eval(double x) {
            return unaryOperation.eval(x);
        }

        private static Function extract(String name) {
            for (Function function : reservedFunctions) {
                if (function.getName().equals(name))
                    return function;
            }
            throw new RuntimeException("undefined unary operation: " + "\"" + name + "\"");
        }

        private static void define(String name, Evaluable evaluable) {
            for (int i = 0; i < reservedFunctions.size(); i++) {
                Function function = reservedFunctions.get(i);
                if (function.getName().equals(name))
                    reservedFunctions.remove(i);
            }
            reservedFunctions.add(Function.implement(name, evaluable));
        }

        static ArrayList<Function> list() {
            return reservedFunctions;
        }

        public boolean equals(RegisteredUnaryOperation other) {
            return other.unaryOperation.getName().equals(this.unaryOperation.getName());
        }
    }

    /**
     * Returns true if the operations (Function) e.g. "sin", "cos" are the same
     *
     * @param other the other operable, possibly UnaryOperation or BinaryOperation
     * @return whether or not the two instances are identical to each other.
     */
    public boolean equals(Operable other) {
        return other instanceof UnaryOperation
                && ((UnaryOperation) other).operation.equals(this.operation) //evaluates to false for operations "sin" and "cos"
                && this.getLeftHand().equals(((UnaryOperation) other).getLeftHand()); //delegate down
    }

    /**
     * Creates a new Operable with its variable replaced with {nested}
     * Note: modifies
     *
     * @param replacement the operable to be plugged in
     * @return a new instance with its original variable replaced with {nested}
     */
    public Operable plugIn(Variable var, Operable replacement) {
        if (this.getLeftHand().equals(var))
            this.setLeftHand(replacement);
        else this.getLeftHand().plugIn(var, replacement);
        return this;
    }

    public int numNodes() {
        return 1 + getLeftHand().numNodes();
    }

    public double val() {
        return operation.eval(getLeftHand().val());
    }

    public boolean isUndefined() {
        if (getLeftHand().isUndefined()) return true;
        if (getLeftHand().val() != Double.NaN) {
            double n = getLeftHand().val();
            switch (operation.getName()) {
                case "ln":
                    return n <= 0;
                case "log":
                    return n <= 0;
                case "asin":
                    return n > 1 || n < -1;
                case "acos":
                    return n > 1 || n < -1;
//                case "tan":
//                case "cot":
//                case "sec":
//                case "csc":
            }
        }

        Operable o;
        switch (operation.getName()) {
            case "tan": // domain: x != pi/2 + n*pi
            case "sec":
                o = Operable.div(this.getLeftHand(), Operable.div(Constants.getConstant("pi"), RawValue.TWO)).simplify();
                if (o instanceof RawValue && ((RawValue) o).isInteger()) {
                    return Math.abs(((RawValue) o).intValue() % 2) == 1;
                }
                break;
            case "cot": // domain: x != n*pi
            case "csc":
                o = Operable.div(this.getLeftHand(), Constants.getConstant("pi")).simplify();
                if (o instanceof RawValue && ((RawValue) o).isInteger())
                    return true;

        }

        return false;
    }

    public int levelOf(Operable o) {
        if (this.equals(o)) return 0;
        int i = getLeftHand().levelOf(o);
        if (i == -1) return -1;
        return i + 1;
    }

    public Operable beautify() {
        return setLeftHand(getLeftHand().beautify());
    }

    public Operable explicitNegativeForm() {
        return clone().setLeftHand(getLeftHand().explicitNegativeForm());
    }

    public Operable replace(Operable o, Operable r) {
        if (this.equals(o)) return r;
        UnaryOperation clone = this.clone();
        return clone.setLeftHand(clone.getLeftHand().replace(o, r));
    }

}
