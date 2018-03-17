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
public class UnaryOperation extends Operation implements BinLeafNode {

    private Function operation;

    public UnaryOperation(Operable operand, String operation) {
        this(operand, RegisteredUnaryOperation.extract(operation));
    }

    /**
     * Instantiating using this constructor might compromise CAS capabilities.
     * This constructor accepts non-JMC standard functions; This means that as long as the function
     * of concern returns a value, it would be valid.
     *
     * @param operand   e.g. "x" in "log(x)"
     * @param operation "log" in "log(x)"
     */
    public UnaryOperation(Operable operand, Function operation) {
        super(wrap(operand));
        this.operation = operation;
    }




    public static void define(String name, String expression) {
        UnaryOperation.define(name, Compiler.compile(expression));
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
     * @param x input, double x
     * @return the computed value by plugging in the value of x into the designated unary operation
     */
    public double eval(double x) {
        return operation.eval(getOperand().eval(x));
    }

    /**
     * Note: modifies self
     *
     * @return exponential form of self
     */
    @Override
    public Operable toExponentialForm() {
        if (getOperand() instanceof Operation) {
            this.setOperand(getOperand().toExponentialForm());
            return this;
        } else return this;
    }

    public int complexity() {
        return getOperand().complexity() + 1;
    }

    /**
     * Note: modifies self.
     * TODO: ln(a) - ln(b) should be ln(a/b)
     * TODO: log(225) should be 2log(15)
     *
     * @return a new Operable instance that is the simplified version of self.
     */
    public Operable simplify() {
        if (getOperand() instanceof Operation) {
            this.setOperand((this.getOperand()).simplify());
        }

        if (this.isUndefined()) return RawValue.UNDEF;

        double val = this.val();
        if (RawValue.isInteger(val))
            return new RawValue(val);

        if (getOperand() instanceof UnaryOperation) {
            UnaryOperation op = (UnaryOperation) getOperand();
            switch (this.operation.getName()) { // TODO: domain!!!
                case "cos":
                    switch (op.operation.getName()) {
                        case "acos":
                            return op.getOperand();
                    }
                    break;
                case "sin":
                    switch (op.operation.getName()) {
                        case "asin":
                            return op.getOperand();
                    }
                case "tan":
                    switch (op.operation.getName()) {
                        case "atan":
                            return op.getOperand(); // what about tan(pi/2)?
                    }
            }
        } else if (getOperand() instanceof Constants.Constant) {
            Constants.Constant c = (Constants.Constant) getOperand();
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
        } else if (getOperand() instanceof RawValue) {
            RawValue r = (RawValue) getOperand();
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
                                return Operation.mult(new RawValue(num[0]), new UnaryOperation(r1, operation));
                            }
                        }
                }
            }

        } else if (getOperand() instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) getOperand();
            switch (operation.getName()) {
                case "ln":
                    if (binOp.is("^") && binOp.getLeft().equals(Constants.getConstant("e"))) {
                        return binOp.getRight();
                    }
            }
        }


        return this;
    }

    @Override
    public UnaryOperation copy() {
        return new UnaryOperation(getOperand(), operation);
    }

    /**
     * Note: modifies self.
     * Only delegates downward if it contains an operation.
     *
     * @return a new Operable instance that is the addition only form of self.
     */
    public UnaryOperation toAdditionOnly() {
        if (getOperand() instanceof Operation) {
            this.setOperand(((Operation) this.getOperand()).toAdditionOnly());
            return this;
        }
        return this;
    }

    public String toString() {
        return operation.getName() + "(" + getOperand().toString() + ")";
    }

    public String getName() {
        return operation.getName();
    }

    private static class RegisteredUnaryOperation implements Evaluable, Nameable {
        private static ArrayList<Function> reservedFunctions;

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
            define("ln", x -> log(x) / log(Constants.E.val()));
            define("sec", x -> 1 / cos(x));
            define("csc", x -> 1 / sin(x));
            define("cot", x -> 1 / tan(x));
            define("!", x -> MathContext.factorial(abs((long) x)));
            define("cosh", Math::cosh);
            define("sinh", Math::sinh);
            define("tanh", Math::tanh);
            if (Mode.DEBUG) System.out.println("# reserved unary operations declared");
        }

        private Function unaryOperation;

        RegisteredUnaryOperation(String name) {
            for (Function function : reservedFunctions) {
                if (function.getName().equals(name))
                    unaryOperation = function;
            }
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

        @Override
        public double eval(double x) {
            return unaryOperation.eval(x);
        }

        public String getName() {
            return unaryOperation.getName();
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
                && this.getOperand().equals(((UnaryOperation) other).getOperand()); //delegate down
    }

    /**
     * Creates a new Operable with its variable replaced with {nested}
     * Note: modifies
     *
     * @param replacement the operable to be plugged in
     * @return a new instance with its original variable replaced with {nested}
     */
    public Operable plugIn(Variable var, Operable replacement) {
        if (this.getOperand().equals(var))
            this.setOperand(replacement);
        else this.getOperand().plugIn(var, replacement);
        return this;
    }

    public int numNodes() {
        return 1 + getOperand().numNodes();
    }

    public double val() {
        return operation.eval(getOperand().val());
    }

    public boolean isUndefined() {
        if (getOperand().isUndefined()) return true;
        if (getOperand().val() != Double.NaN) {
            double n = getOperand().val();
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
                o = Operation.div(this.getOperand(), Operation.div(Constants.getConstant("pi"), RawValue.TWO)).simplify();
                if (o instanceof RawValue && ((RawValue) o).isInteger()) {
                    return Math.abs(((RawValue) o).intValue() % 2) == 1;
                }
                break;
            case "cot": // domain: x != n*pi
            case "csc":
                o = Operation.div(this.getOperand(), Constants.getConstant("pi")).simplify();
                if (o instanceof RawValue && ((RawValue) o).isInteger())
                    return true;

        }

        return false;
    }

    public int levelOf(Operable o) {
        if (this.equals(o)) return 0;
        int i = getOperand().levelOf(o);
        if (i == -1) return -1;
        return i + 1;
    }

    public Operable getOperand() {
        return getOperand(0);
    }

    public Operable setOperand(Operable operable) {
        return super.setOperand(operable, 0);
    }


    public Operable beautify() {
        return setOperand(getOperand().beautify());
    }

    public Operable explicitNegativeForm() {
        return copy().setOperand(getOperand().explicitNegativeForm());
    }

    public Operable expand() {
        return setOperand(getOperand().expand());
    }

    public Operable replace(Operable o, Operable r) {
        if (this.equals(o)) return r;
        UnaryOperation clone = this.copy();
        return clone.setOperand(clone.getOperand().replace(o, r));
    }


}
