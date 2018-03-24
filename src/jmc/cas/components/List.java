package jmc.cas.components;

import jmc.cas.Operable;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Jiachen on 3/20/18.
 * Wrapper for ArrayList -> the order of the operable matters
 */
public class List extends Operable {
    private ArrayList<Operable> operables;

    public List(ArrayList<Operable> operables) {
        this.operables = operables;
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

    @Override
    public Operable copy() {
        return null;
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
        return 0;
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
        return null;
    }

    /**
     * (-#)*x will be converted to (-1)*#*x, where # denotes a number
     * NOTE: does not modify self.
     *
     * @return explicit negative form of the original Operable
     */
    @Override
    public Operable explicitNegativeForm() {
        return null;
    }

    @Override
    public Operable toAdditionOnly() {
        return null;
    }

    @Override
    public Operable toExponentialForm() {
        return null;
    }

    /**
     * If this method is successfully implemented, it would be marked as a milestone.
     *
     * @param v the variable in which the first derivative is taken with respect to.
     * @return first derivative of the expression
     */
    @Override
    public Operable firstDerivative(Variable v) {
        return null;
    }

    /**
     * Expand the expression; its behavior is exactly what you would expect.
     * e.g. (a+b+...)(c+d) = a*c + a*d + b*c + ...
     *
     * @return expanded expression of type Operable
     */
    @Override
    public Operable expand() {
        return null;
    }

    /**
     * @param o the operable to be replaced
     * @param r the operable to take o's place
     * @return the original operable with o replaced by r.
     */
    @Override
    public Operable replace(Operable o, Operable r) {
        return null;
    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public double eval(double x) {
        return 0;
    }
}
