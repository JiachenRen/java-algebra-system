package jmc.cas;

import jmc.Function;
import jmc.MathContext;

import java.util.ArrayList;

import static java.lang.Math.*;

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
     *
     * @return a new Operable instance that is the simplified version of self.
     */
    public Operable simplify() {
        if (getLeftHand() instanceof Operation) {
            //TODO: process trigonometric simplification
            //if this.operation = "atan" && getLeftHand().operation = "tan" then simplify
            this.setLeftHand(((Operation) this.getLeftHand()).simplify());
            return this;
        } else return this;
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
            define("log", Math::log);
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
            define("!", x -> MathContext.f(abs((long) x)));
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

    public boolean isUndefined() {
        if (getLeftHand().isUndefined()) return true;
        if (getLeftHand() instanceof RawValue) {
            double n = ((RawValue) getLeftHand()).doubleValue();
            switch (operation.getName()) {
                case "ln": return n <= 0;
                case "log": return n <= 0;
                case "asin": return n > 1 || n < -1;
                case "acos": return n > 1 || n < -1;
//                case "tan":
//                case "cot":
//                case "sec":
//                case "csc":
            }
        }
        return false;
    }

}
