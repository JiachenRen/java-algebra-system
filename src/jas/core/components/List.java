package jas.core.components;

import jas.core.JMCException;
import jas.core.Operable;
import jas.core.operations.BinaryOperation;
import jas.core.operations.CustomOperation;
import jas.core.operations.UnaryOperation;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static jas.core.Mode.COMMA_COLOR;
import static jas.core.Mode.CURLY_BRACKET_COLOR;
import static jas.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 3/20/18.
 * Wrapper for ArrayList -> the order of the operable matters
 */
public class List extends Operable {
    private ArrayList<Operable> operables;

    public List(ArrayList<Operable> operables) {
        this.operables = operables;
    }

    public ArrayList<Operable> unwrap() {
        return operables;
    }

    @Override
    public boolean equals(Operable other) {
        if (!(other instanceof List)) return false;
        List list = ((List) other);
        if (list.size() != size()) return false;
        for (int i = 0; i < size(); i++) {
            Operable operable = get(i);
            if (!operable.equals(list.get(i)))
                return false;
        }
        return true;
    }

    public Operable get(int i) {
        return operables.get(i);
    }

    public int size() {
        return operables.size();
    }

    /**
     * perform binary operation with another list that has matching dimension.
     * NOTE: modifies self
     *
     * @param binOp binary operation to be performed
     * @param other another list with matching dimension, i.e. size == size
     * @return two lists merged with binary operation
     */
    public List binOp(BinaryOperation binOp, List other) {
        if (size() != other.size()) throw new JMCException("list dimension mismatch");
        ArrayList<Operable> unwrap = unwrap();
        for (int i = 0; i < unwrap.size(); i++) {
            Operable operable = unwrap.get(i);
            set(i, new BinaryOperation(operable, binOp.getName(), other.get(i)));
        }
        return this;
    }

    /**
     * perform binary operation with another operable element.
     * NOTE: modifies self
     *
     * @param binOp   binary operation to be performed
     * @param other   an operable instance
     * @param forward if this is 'a' in a/b, then set [forward] to true; otherwise false
     * @return self with all of the elements performed binOp with [other]
     */
    public List binOp(BinaryOperation binOp, Operable other, boolean forward) {
        ArrayList<Operable> unwrap = unwrap();
        for (int i = 0; i < unwrap.size(); i++) {
            Operable operable = unwrap.get(i);
            BinaryOperation op = forward ? new BinaryOperation(operable, binOp.getName(), other)
                    : new BinaryOperation(other, binOp.getName(), operable);
            set(i, op);
        }
        return this;
    }

    /**
     * perform unary operation on each element in the list
     *
     * @param uOp UnaryOperation to be performed for each element in the list
     * @return self
     */
    public List uOp(UnaryOperation uOp) {
        operables = operables.stream().map(o -> new UnaryOperation(o, uOp.getFunction()))
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * perform custom operation on each element in the list
     *
     * @param customOp a custom operation taking in only one argument with signature ANY
     * @return self
     */
    public List customOp(CustomOperation customOp) {
        operables = operables.stream().map(o -> new CustomOperation(customOp.getName(), o))
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    public void set(int i, Operable o) {
        operables.set(i, o);
    }

    public void set(ArrayList<Operable> operables) {
        this.operables = operables;
    }

    @Override
    public Operable copy() {
        return this;
    }

    /**
     * orders the expression according to lexicographic order. This saves some computational resource when trying to decide
     * whether a node within the binary tree the the canonical form of another.
     * e.g. c*b*3*a would be reordered to something like 3*a*c*b
     */
    @Override
    public void order() {
        //TODO: implement
    }

    /**
     * returns 0, if the current instance is what you are looking for, i.e. this.equals(o);
     * returns -1 if not found.
     *
     * @param o the Operable instance that you are looking for.
     * @return the level at which operable is found.
     */
    @Override
    public int levelOf(Operable o) {
        return 0;
    }

    @Override
    public String toString() {
        Optional<String> args = operables.stream().map(Operable::toString).reduce((a, b) -> a + "," + b);
        return "{" + (args.orElse("")) + "}";
    }

    @Override
    public String coloredString() {
        Optional<String> args = operables.stream()
                .map(Operable::coloredString)
                .reduce((a, b) -> a + color(",", COMMA_COLOR) + b);
        return color("{", CURLY_BRACKET_COLOR) + (args.orElse("")) + color("}", CURLY_BRACKET_COLOR);
    }

    /**
     * numNodes() of expression "(3 + 4.5) * 5.3 / 2.7" returns 7
     * numNodes() of expression "(3 + 4.5) * ln(5.3 + 4) / 2.7 / (x + 1) * x / 3" returns 18
     * numNodes() of expression "3 - 2x + 4x - 4 + 7pi" returns 15
     *
     * @return number of nodes (including both leaf nodes and non-leaf nodes)
     */
    @Override
    public int numNodes() {
        return 0;
    }

    /**
     * traverses the composite tree and evaluates every single node that represents a raw value.
     * e.g. if the Operable represents the expression "(5 + 7) / 2 ^ 2", val() returns 3.
     * however, if variables exists in the expression, NaN is returned.
     * e.g. if the Operable represents the expression "(5 + 7x) / 2 ^ 2", val() returns NaN.
     *
     * @return arbitrary value of the node.
     */
    @Override
    public double val() {
        return Double.NaN;
    }

    /**
     * @return level of complexity of the expression represented by an integer with 1 being
     * the most simplistic
     */
    @Override
    public int complexity() {
        return 0;
    }

    /**
     * NOTE: useful for simple simplifications.
     * Operable::simplify(Operable o) is optimized for complex simplifications like when taking the 10th derivative of x*cos(x)*sin(x)
     *
     * @return simplified expression
     */
    @Override
    public Operable simplify() {
        operables = operables.stream()
                .map(Operable::simplify)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
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
    @Override
    public Operable beautify() {
        return this;
    }

    /**
     * (-#)*x will be converted to (-1)*#*x, where # denotes a number
     * NOTE: does not modify self.
     *
     * @return explicit negative form of the original Operable
     */
    @Override
    public Operable explicitNegativeForm() {
        return this;
    }

    @Override
    public Operable toAdditionOnly() {
        return this;
    }

    @Override
    public Operable toExponentialForm() {
        return this;
    }

    /**
     * If this method is successfully implemented, it would be marked as a milestone.
     *
     * @param v the variable in which the first derivative is taken with respect to.
     * @return first derivative of the expression
     */
    @Override
    public Operable firstDerivative(Variable v) {
        return this;
    }

    /**
     * Expand the expression; its behavior is exactly what you would expect.
     * e.g. (a+b+...)(c+d) = a*c + a*d + b*c + ...
     *
     * @return expanded expression of type Operable
     */
    @Override
    public Operable expand() {
        return this;
    }

    /**
     * @param o the operable to be replaced
     * @param r the operable to take o's place
     * @return the original operable with o replaced by r.
     */
    @Override
    public Operable replace(Operable o, Operable r) {
        return this;
    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public double eval(double x) {
        return Double.NaN;
    }
}
