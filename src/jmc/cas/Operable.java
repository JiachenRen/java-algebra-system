package jmc.cas;

/**
 * Created by Jiachen on 03/05/2017.
 * Operable
 */
public interface Operable extends Evaluable {
    String toString();

    Operable clone();

    boolean equals(Operable other);

    /**
     * numNodes() of expression "(3 + 4.5) * 5.3 / 2.7" returns 7
     * numNodes() of expression "(3 + 4.5) * ln(5.3 + 4) / 2.7 / (x + 1) * x / 3" returns 18
     * numNodes() of expression "3 - 2x + 4x - 4 + 7pi" returns 15
     *
     * @return number of nodes (including both leaf nodes and non-leaf nodes)
     */
    int numNodes();

    /**
     * returns 0, if the current instance is what you are looking for, i.e. this.equals(o);
     * @param o the Operable instance that you are looking for.
     * @return the level at which operable is found.
     */
    int levelOf(Operable o);

    /**
     * plugs in the operable nested for all variables in the expression
     * NOTE: the method returns the operable with the desired nested operable plugged in, but
     * the operable itself is not altered.
     *
     * @param var         the variable to be replaced
     * @param replacement the operable to be plugged in
     * @return the resulting operable with nested plugged in
     */
    Operable plugIn(Variable var, Operable replacement);

    Operable simplify();

    /**
     * traverses the composite tree and evaluates every single node that represents a raw value.
     * e.g. if the Operable represents the expression "(5 + 7) / 2 ^ 2", val() returns 3.
     * however, if variables exists in the expression, NaN is returned.
     * e.g. if the Operable represents the expression "(5 + 7x) / 2 ^ 2", val() returns NaN.
     *
     * @return arbitrary value of the node.
     */
    double val();

    boolean isUndefined();
}
