package jas.core.components;

import jas.core.JASException;
import jas.core.Node;
import jas.core.operations.Binary;
import jas.core.operations.Custom;
import jas.core.operations.Unary;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static jas.core.Mode.COMMA_COLOR;
import static jas.core.Mode.CURLY_BRACKET_COLOR;
import static jas.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 3/20/18.
 * Wrapper for ArrayList -> the order of the node matters
 */
public class List extends Node {
    private ArrayList<Node> nodes;

    public List(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Node> unwrap() {
        return nodes;
    }

    @Override
    public boolean equals(Node other) {
        if (!(other instanceof List)) return false;
        List list = ((List) other);
        if (list.size() != size()) return false;
        for (int i = 0; i < size(); i++) {
            Node node = get(i);
            if (!node.equals(list.get(i)))
                return false;
        }
        return true;
    }

    public Node get(int i) {
        return nodes.get(i);
    }

    public int size() {
        return nodes.size();
    }

    /**
     * perform binary operation with another list that has matching dimension.
     * NOTE: modifies self
     *
     * @param binOp binary operation to be performed
     * @param other another list with matching dimension, i.e. size == size
     * @return two lists merged with binary operation
     */
    public List binOp(Binary binOp, List other) {
        if (size() != other.size()) throw new JASException("list dimension mismatch");
        ArrayList<Node> unwrap = unwrap();
        for (int i = 0; i < unwrap.size(); i++) {
            Node node = unwrap.get(i);
            set(i, new Binary(node, binOp.getName(), other.get(i)));
        }
        return this;
    }

    /**
     * perform binary operation with another node element.
     * NOTE: modifies self
     *
     * @param binOp   binary operation to be performed
     * @param other   an node instance
     * @param forward if this is 'a' in a/b, then set [forward] to true; otherwise false
     * @return self with all of the elements performed binOp with [other]
     */
    public List binOp(Binary binOp, Node other, boolean forward) {
        ArrayList<Node> unwrap = unwrap();
        for (int i = 0; i < unwrap.size(); i++) {
            Node node = unwrap.get(i);
            Binary op = forward ? new Binary(node, binOp.getName(), other)
                    : new Binary(other, binOp.getName(), node);
            set(i, op);
        }
        return this;
    }

    /**
     * perform unary operation on each element in the list
     *
     * @param uOp Unary to be performed for each element in the list
     * @return self
     */
    public List uOp(Unary uOp) {
        nodes = nodes.stream().map(o -> new Unary(o, uOp.getFunction()))
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * perform custom operation on each element in the list
     *
     * @param customOp a custom operation taking in only one argument with signature ANY
     * @return self
     */
    public List customOp(Custom customOp) {
        nodes = nodes.stream().map(o -> new Custom(customOp.getName(), o))
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    public void set(int i, Node o) {
        nodes.set(i, o);
    }

    public void set(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Node copy() {
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
     * @param o the Node instance that you are looking for.
     * @return the level at which node is found.
     */
    @Override
    public int levelOf(Node o) {
        return 0;
    }

    @Override
    public String toString() {
        Optional<String> args = nodes.stream().map(Node::toString).reduce((a, b) -> a + "," + b);
        return "{" + (args.orElse("")) + "}";
    }

    @Override
    public String coloredString() {
        Optional<String> args = nodes.stream()
                .map(Node::coloredString)
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
     * e.g. if the Node represents the expression "(5 + 7) / 2 ^ 2", val() returns 3.
     * however, if variables exists in the expression, NaN is returned.
     * e.g. if the Node represents the expression "(5 + 7x) / 2 ^ 2", val() returns NaN.
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
     * Node::simplify(Node o) is optimized for complex simplifications like when taking the 10th derivative of x*cos(x)*sin(x)
     *
     * @return simplified expression
     */
    @Override
    public Node simplify() {
        nodes = nodes.stream()
                .map(Node::simplify)
                .collect(Collectors.toCollection(ArrayList::new));
        return this;
    }

    /**
     * basically reversing the effects of toAdditionalOnly and toExponentialForm
     * a*b^(-1) -> a/b,
     * a*(1/3) -> a/3,
     * a+(-1)*b -> a-b
     * <p>
     * before invoking this method, the Node should already by at a stage where it is simplified,
     * converted to additional only and in exponential form.
     *
     * @return beautified version of the original
     */
    @Override
    public Node beautify() {
        return this;
    }

    /**
     * (-#)*x will be converted to (-1)*#*x, where # denotes a number
     * NOTE: does not modify self.
     *
     * @return explicit negative form of the original Node
     */
    @Override
    public Node explicitNegativeForm() {
        return this;
    }

    @Override
    public Node toAdditionOnly() {
        return this;
    }

    @Override
    public Node toExponentialForm() {
        return this;
    }

    /**
     * If this method is successfully implemented, it would be marked as a milestone.
     *
     * @param v the variable in which the first derivative is taken with respect to.
     * @return first derivative of the expression
     */
    @Override
    public Node firstDerivative(Variable v) {
        return this;
    }

    /**
     * Expand the expression; its behavior is exactly what you would expect.
     * e.g. (a+b+...)(c+d) = a*c + a*d + b*c + ...
     *
     * @return expanded expression of type Node
     */
    @Override
    public Node expand() {
        return this;
    }

    /**
     * @param o the node to be replaced
     * @param r the node to take o's place
     * @return the original node with o replaced by r.
     */
    @Override
    public Node replace(Node o, Node r) {
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
