package jmc_lib;

import java.util.ArrayList;

import static java.lang.Math.*;

/**
 * Created by Jiachen on 16/05/2017.
 * code refactored May 20th, performance enhanced with static function lib and method reference.
 * breakthrough May 20th.
 */
public class UnaryOperation extends Operation {

    private Function operation;

    public UnaryOperation(Operable leftHand, String operation) {
        this(leftHand, RegisteredUnaryOperation.extract(operation));
    }

    public UnaryOperation(Operable leftHand, Function operation) {
        super(leftHand);
        this.operation = operation;
        if (leftHand instanceof BinaryOperation) {
            ((BinaryOperation) leftHand).setOmitParenthesis(true);
        }
    }

    /**
     *
     * @param x input, double x
     * @return the computed value from the designated unary operation
     * @since May 21st: critical bug fixed.
     */
    public double eval(double x) {
        return operation.eval(getLeftHand().eval(x));
    }


    public static void define(String name, String expression) {
        UnaryOperation.define(name, Function.interpret(expression));
    }

    public static void define(String name, Evaluable evaluable) {
        RegisteredUnaryOperation.define(name, evaluable);
    }

    @Override
    public void toExponentialForm() {
        if (getLeftHand() instanceof Operation) ((Operation) getLeftHand()).toExponentialForm();
    }

    public static Operable negate(Operable operable) {
        return new BinaryOperation(new Raw(0), "-", operable);
    }


    public String toString() {
        return operation.getName() + "<" + getLeftHand().toString() + ">";
    }

    /**
     * TODO process trigonometric simplification
     *
     * @return simplified self
     */
    public Operable simplify() {
        if (getLeftHand() instanceof Operation) {
            ((Operation) getLeftHand()).simplify();
            return this;
        } else return this;
    }

    @Override
    public UnaryOperation replicate() {
        return new UnaryOperation(getLeftHand().replicate(), operation);
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
            System.out.println("reserved unary operations declared");
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

        public static Function extract(String name) {
            for (Function function : reservedFunctions) {
                if (function.getName().equals(name))
                    return function;
            }
            throw new RuntimeException("undefined unary operation: " + "\"" + name + "\"");
        }

        public static void define(String name, Evaluable evaluable) {
            for (int i = 0; i < reservedFunctions.size(); i++) {
                Function function = reservedFunctions.get(i);
                if (function.getName().equals(name))
                    reservedFunctions.remove(i);
            }
            reservedFunctions.add(Function.implement(name, evaluable));
        }
    }
}
