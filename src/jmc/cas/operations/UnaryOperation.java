package jmc.cas.operations;

import jmc.Function;
import jmc.MathContext;
import jmc.cas.*;
import jmc.cas.Compiler;
import jmc.cas.components.Constants;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;

import java.math.BigDecimal;
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
        this(operand, UnaryOperator.extract(operation));
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
        UnaryOperator.define(name, evaluable);
    }

    /**
     * List of built-in operations:
     * -> cos, acos(cos^-1), sin, asin, tan, atan, sec, csc, cot, log, int, abs, ln, cosh, sinh, tanh, !(factorial)
     *
     * @return an ArrayList containing all of the defined unary operations.
     */
    public static ArrayList<Function> registeredOperations() {
        return UnaryOperator.list();
    }

    public static boolean isDefined(String name) {
        for (Function function : registeredOperations()) {
            if (function.getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * @param x input, double x
     * @return the computed value by plugging in the value of x into the designated unary operation
     */
    public double eval(double x) {
        return operation.eval(getOperand().eval(x));
    }

    @Override
    public UnaryOperation copy() {
        return new UnaryOperation(getOperand().copy(), operation);
    }

    /**
     * Note: modifies self.
     * TODO: ln(a) - ln(b) should be ln(a/b)
     * log(225) should be 2log(15) -> implemented
     *
     * @return a new Operable instance that is the simplified version of self.
     */
    @Override
    public Operable simplify() {
        super.simplify();

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
                        ArrayList<Long> factors = getFactors(r.longValue());
                        if (factors.size() > 1) {
                            ArrayList<Long> uniqueFactors = getUniqueFactors(factors);
                            int[] num = numOccurrences(uniqueFactors, factors);
                            if (allTheSame(num)) {
                                RawValue r1 = new RawValue(MathContext.mult(uniqueFactors));
                                if (!r1.equals(r)) // do not return 1*ln(10), or it would cause StackOverflow error!
                                    return Operation.mult(new RawValue(num[0]), new UnaryOperation(r1, operation));
                                else return this;
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
    public Operable firstDerivative(Variable v) {
        Operable smallKahuna = getOperand().firstDerivative(v);
        Operable bigKahuna = null;
        switch (this.operation.getName()) {
            case "cos": // d/dx cos(x) = -sin(x)
                bigKahuna = RawValue.ONE.negate().mult(new UnaryOperation(getOperand(), "sin"));
                break;
            case "sin": // d/dx sin(x) = cos(x)
                bigKahuna = new UnaryOperation(getOperand(), "cos");
                break;
            case "ln": // d/dx ln(x) = 1/x
                bigKahuna = RawValue.ONE.div(getOperand());
                break;
            case "log": // d/dx log(x) = 1/x*log(e)
                bigKahuna = RawValue.ONE.div(getOperand()).mult(new UnaryOperation(Constants.E, "log"));
                break;
            case "acos": // d/dx arccos(x) = (-1)/(1-x^2)^(1/2)
                bigKahuna = RawValue.ONE.negate().div(RawValue.ONE.sub(getOperand().sq()).sqrt());
                break;
            case "asin": // d/dx arcsin(x) = -arccos(x)
                bigKahuna = RawValue.ONE.div(RawValue.ONE.sub(getOperand().sq()).sqrt());
                break;
            case "tan": // d/dx tan(x) = 1/cos(x)^2
                bigKahuna = RawValue.ONE.div(new UnaryOperation(getOperand(), "cos").sq());
                break;
            case "atan": // d/dx arctan(x) = 1/(x^2+1)
                bigKahuna = RawValue.ONE.div(getOperand().sq().add(1));
                break;
            case "abs": // d/dx |x| = sign(x)
                bigKahuna = new UnaryOperation(getOperand(), "sign");
                break;
            case "csc": // d/dx csc(x) = -cos(x)/sin(x)^2
                bigKahuna = new UnaryOperation(getOperand(), "cos").mult(-1).div(new UnaryOperation(getOperand(), "sin").sq());
                break;
            case "sec": // d/dx sec(x) = sin(x)/cos(x)^2
                bigKahuna = new UnaryOperation(getOperand(), "sin").div(new UnaryOperation(getOperand(), "cos").sq());
                break;
            case "cot": // d/dx cot(x) = -1/sin(x)^2
                bigKahuna = RawValue.ONE.negate().div(new UnaryOperation(getOperand(), "sin").sq());
                break;
            case "cosh": // d/dx cosh(x) = sinh(x)
                bigKahuna = new UnaryOperation(getOperand(), "sinh");
                break;
            case "sinh": // d/dx sinh(x) = cosh(x)
                bigKahuna = new UnaryOperation(getOperand(), "cosh");
                break;
            case "tanh": // d/dx tanh(x) = 1/cosh(x)^2
                bigKahuna = RawValue.ONE.div(new UnaryOperation(getOperand(), "cosh").sq());
                break;
        }
        if (bigKahuna != null) return smallKahuna.mult(bigKahuna);
        return new CompositeOperation(Calculus.DERIVATIVE, this.copy());
    }

    public boolean isUndefined() {
        if (super.isUndefined()) return true;
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
                    return Math.abs(((RawValue) o).longValue() % 2) == 1;
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

    public Operable getOperand() {
        return getOperand(0);
    }

    public String getName() {
        return operation.getName();
    }

    public Operable setOperand(Operable operable) {
        return super.setOperand(operable, 0);
    }

    public String toString() {
        return operation.getName() + "(" + getOperand().toString() + ")";
    }

    /**
     * Returns true if the operations (Function) e.g. "sin", "cos" are the same
     *
     * @param other the other operable, possibly UnaryOperation or BinaryOperation
     * @return whether or not the two instances are identical to each other.
     */
    @Override
    public boolean equals(Operable other) {
        return other instanceof UnaryOperation
                && ((UnaryOperation) other).operation.equals(this.operation) //evaluates to false for operations "sin" and "cos"
                && this.getOperand().equals(((UnaryOperation) other).getOperand()); //delegate down
    }

    public double val() {
        return operation.eval(getOperand().val());
    }

    private static class UnaryOperator implements Evaluable, Nameable {
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
            define("factorial", x -> MathContext.factorial(new BigDecimal(x).toBigInteger()).doubleValue()); //TODO: overflows! simplification of factorials; handle special notations like ! in the compiler
            define("cosh", Math::cosh);
            define("sinh", Math::sinh);
            define("tanh", Math::tanh);
            define("sign", x -> {
                if (!Double.isNaN(x)) {
                    return x == 0 ? 0 : x > 0 ? 1 : -1;
                }
                return Double.NaN;
            });
            if (Mode.DEBUG) System.out.println("# reserved unary operations declared");
        }

        private Function unaryOperation;

        UnaryOperator(String name) {
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
            reservedFunctions.add(new Function(name, evaluable));
        }

        static ArrayList<Function> list() {
            return reservedFunctions;
        }

        @Override
        public double eval(double x) {
            return unaryOperation.eval(x);
        }

        public boolean equals(UnaryOperator other) {
            return other.unaryOperation.getName().equals(this.unaryOperation.getName());
        }

        public String getName() {
            return unaryOperation.getName();
        }


    }


}
