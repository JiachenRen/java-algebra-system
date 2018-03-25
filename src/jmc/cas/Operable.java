package jmc.cas;


import jmc.cas.components.Fraction;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;
import jmc.cas.operations.BinaryOperation;
import jmc.cas.operations.Operation;

import java.util.ArrayList;

import static jmc.cas.Mode.PARENTHESIS_COLOR;
import static jmc.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 03/05/2017.
 * Operable
 */
public abstract class Operable implements Evaluable {
    /**
     * invocation of commonTerms(o1 = "a*b*(c+d)*m", o2 = "f*(c+d)*m")
     * returns [(c+d), m]
     *
     * @param o1 Operable #1
     * @param o2 Operable #2
     * @return an ArrayList containing common terms of o1 and o2
     */
    public static ArrayList<Operable> commonTerms(Operable o1, Operable o2) {
        ArrayList<Operable> terms = new ArrayList<>();
        if (o1 instanceof BinLeafNode && o2 instanceof BinLeafNode) {
            if (o1.equals(o2)) {
                terms.add(o1.copy());
            }
        } else if (((o1 instanceof BinaryOperation) && (o2 instanceof BinLeafNode)) || ((o1 instanceof BinLeafNode) && (o2 instanceof BinaryOperation))) {
            BinaryOperation binOp = (BinaryOperation) (o1 instanceof BinaryOperation ? o1 : o2);
            Operable op = o1 instanceof BinaryOperation ? o2 : o1;
            ArrayList<Operable> pool = binOp.flattened();
            if (Operable.contains(pool, op))
                terms.add(op.copy());
        } else if (o1 instanceof BinaryOperation && o2 instanceof BinaryOperation) {
            BinaryOperation binOp1 = (BinaryOperation) o1;
            BinaryOperation binOp2 = (BinaryOperation) o2;
            ArrayList<Operable> pool1 = binOp1.flattened();
            ArrayList<Operable> pool2 = binOp2.flattened();
            for (int i = pool1.size() - 1; i >= 0; i--) {
                Operable op1 = pool1.get(i);
                for (int k = pool2.size() - 1; k >= 0; k--) {
                    Operable op2 = pool2.get(k);
                    if (op1.equals(op2)) {
                        pool1.remove(i);
                        pool2.remove(k);
                        terms.add(op1.copy());
                        break;
                    }
                }
            }
        }
        return terms;
    }

    public static boolean contains(ArrayList<Operable> operables, Operable target) {
        for (Operable operable : operables) {
            if (operable.equals(target))
                return true;
        }
        return false;
    }

    public static boolean remove(ArrayList<Operable> operables, Operable target) {
        for (Operable operable : operables) {
            if (operable.equals(target))
                return operables.remove(operable);
        }
        return false;
    }

    public static String coloredParenthesis(String s) {
        return color("(", PARENTHESIS_COLOR) + s + color(")", PARENTHESIS_COLOR);
    }

    public abstract boolean equals(Operable other);

    public abstract Operable copy();

    public boolean isMultiVar() {
        return numVars() > 1;
    }

    /**
     * orders the expression according to lexicographic order. This saves some computational resource when trying to decide
     * whether a node within the binary tree the the canonical form of another.
     * e.g. c*b*3*a would be reordered to something like 3*a*c*b
     */
    public abstract void order();

    /**
     * @return number of variables in the expression represented by self.
     */
    public int numVars() {
        return extractVariables().size();
    }

    public ArrayList<Variable> extractVariables() {
        ArrayList<Variable> vars = new ArrayList<>();
        for (Character c : Assets.VARS.toCharArray()) {
            Variable var = new Variable(c.toString());
            if (this.levelOf(var) != -1)
                vars.add(var);
        }
        return vars;
    }

    /**
     * returns 0, if the current instance is what you are looking for, i.e. this.equals(o);
     * returns -1 if not found.
     *
     * @param o the Operable instance that you are looking for.
     * @return the level at which operable is found.
     */
    public abstract int levelOf(Operable o);

    public boolean contains(Operable o) {
        return levelOf(o) != -1;
    }

    public abstract String toString();

    /**
     * numNodes() of expression "(3 + 4.5) * 5.3 / 2.7" returns 7
     * numNodes() of expression "(3 + 4.5) * ln(5.3 + 4) / 2.7 / (x + 1) * x / 3" returns 18
     * numNodes() of expression "3 - 2x + 4x - 4 + 7pi" returns 15
     *
     * @return number of nodes (including both leaf nodes and non-leaf nodes)
     */
    public abstract int numNodes();

    /**
     * negates the original expression
     *
     * @return new instance that is the negated version of the original
     */
    public Operable negate() {
        return Operation.mult(RawValue.ONE.negate(), this);
    }

    /**
     * @return whether the operable represents a number
     */
    public boolean isNaN() {
        return Double.isNaN(val());
    }

    /**
     * traverses the composite tree and evaluates every single node that represents a raw value.
     * e.g. if the Operable represents the expression "(5 + 7) / 2 ^ 2", val() returns 3.
     * however, if variables exists in the expression, NaN is returned.
     * e.g. if the Operable represents the expression "(5 + 7x) / 2 ^ 2", val() returns NaN.
     *
     * @return arbitrary value of the node.
     */
    public abstract double val();

    /**
     * @return level of complexity of the expression represented by an integer with 1 being
     * the most simplistic
     */
    public abstract int complexity();

    /**
     * NOTE: useful for simple simplifications.
     * Operable::simplify(Operable o) is optimized for complex simplifications like when taking the 10th derivative of x*cos(x)*sin(x)
     *
     * @return simplified expression
     */
    public abstract Operable simplify();

    /**
     * handles super complex simplifications. Unless you have hundreds of nested binary operations, don't use this method.
     *
     * @return the simplest representation of the expression in expanded form.
     */
    public Operable simplest() {
        ArrayList<Operable> simplifiedForms = new ArrayList<>();
        Operable s = this.copy();
        while (!contains(simplifiedForms, s)) {
            simplifiedForms.add(s.copy());
            s = s.expand().simplify();
        }
        return simplifiedForms.get(simplifiedForms.size() - 1);
    }

    /**
     * basically reversing the effects of toAdditionalOnly and toExponentialForm
     * a*b^(-1) -> a/b,
     * a*(1/3) -> a/3,
     * a+(-1)*b -> a-b
     * <p>
     * before invoking this method, the Operable should already by at a stage where it is simplified,
     * converted to additional only and in exponential form.
     *
     * @return beautified version of the original
     */
    public abstract Operable beautify();

    /**
     * (-#)*x will be converted to (-1)*#*x, where # denotes a number
     * NOTE: does not modify self.
     *
     * @return explicit negative form of the original Operable
     */
    public abstract Operable explicitNegativeForm();

    public abstract Operable toAdditionOnly();

    public abstract Operable toExponentialForm();

    /**
     * If this method is successfully implemented, it would be marked as a milestone.
     *
     * @param v the variable in which the first derivative is taken with respect to.
     * @return first derivative of the expression
     */
    public abstract Operable firstDerivative(Variable v);

    /**
     * @param v the variable in which the first derivative is taken with respect to.
     * @param n the nth derivative
     * @return the nth derivative of the expression
     */
    public Operable derivative(Variable v, int n) {
        Operable der = this.copy().simplify();
        while (n > 0) {
            //.expand().simplify() gives the completely simplified form.
            der = der.firstDerivative(v).simplest();
            n--;
        }
        return der;
    }

    public Operable exec() {
        return this;
    }

    /**
     * Expand the expression; its behavior is exactly what you would expect.
     * e.g. (a+b+...)(c+d) = a*c + a*d + b*c + ...
     *
     * @return expanded expression of type Operable
     */
    public abstract Operable expand();

    /**
     * @return string representation of the operable coded with Ansi color codes.
     */
    public abstract String coloredString();

    /**
     * @param o the operable to be replaced
     * @param r the operable to take o's place
     * @return the original operable with o replaced by r.
     */
    public abstract Operable replace(Operable o, Operable r);

    public abstract boolean isUndefined();

    public BinaryOperation mult(Operable o) {
        return new BinaryOperation(this.copy(), "*", o);
    }

    public BinaryOperation mult(double n) {
        return new BinaryOperation(this.copy(), "*", new RawValue(n));
    }

    public BinaryOperation add(Operable o) {
        return new BinaryOperation(this.copy(), "+", o.copy());
    }

    public BinaryOperation add(double n) {
        return new BinaryOperation(this.copy(), "+", new RawValue(n));
    }

    public BinaryOperation sub(Operable o) {
        return new BinaryOperation(this.copy(), "-", o.copy());
    }

    public BinaryOperation sub(double n) {
        return new BinaryOperation(this.copy(), "-", new RawValue(n));
    }

    public BinaryOperation div(Operable o) {
        return new BinaryOperation(this.copy(), "/", o.copy());
    }

    public BinaryOperation div(double n) {
        return new BinaryOperation(this.copy(), "/", new RawValue(n));
    }

    public BinaryOperation exp(Operable o) {
        return new BinaryOperation(this.copy(), "^", o.copy());
    }

    public BinaryOperation exp(double n) {
        return new BinaryOperation(this.copy(), "^", new RawValue(n));
    }

    public BinaryOperation sqrt() {
        return exp(new Fraction(1, 2));
    }

    public BinaryOperation sq() {
        return exp(2);
    }
}
