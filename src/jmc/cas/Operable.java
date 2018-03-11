package jmc.cas;


import java.util.ArrayList;

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
     * negates the original expression
     *
     * @return new instance that is the negated version of the original
     */
    Operable negate();

    /**
     * returns 0, if the current instance is what you are looking for, i.e. this.equals(o);
     *
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
    Operable beautify();

    /**
     * (-#)*x will be converted to (-1)*#*x, where # denotes a number
     * NOTE: does not modify self.
     *
     * @return explicit negative form of the original Operable
     */
    Operable explicitNegativeForm();

    /**
     * @param o the Operable instance to be inspected.
     * @return number of variables in the expression represented by o.
     */
    static int numVars(Operable o) {
        return extractVariables(o).size();
    }

    static ArrayList<Variable> extractVariables(Operable o) {
        ArrayList<Variable> vars = new ArrayList<>();
        for (Character c : Assets.VARS.toCharArray()) {
            Variable var = new Variable(c.toString());
            if (o.levelOf(var) != -1)
                vars.add(var);
        }
        return vars;
    }

    static boolean contains(ArrayList<Operable> operables, Operable target) {
        for (Operable operable : operables) {
            if (operable.equals(target))
                return true;
        }
        return false;
    }

    /**
     * invocation of commonTerms(o1 = "a*b*(c+d)*m", o2 = "f*(c+d)*m")
     * returns [(c+d), m]
     *
     * @param o1 Operable #1
     * @param o2 Operable #2
     * @return an ArrayList containing common terms of o1 and o2
     */
    static ArrayList<Operable> commonTerms(Operable o1, Operable o2) {
        ArrayList<Operable> terms = new ArrayList<>();
        if (o1 instanceof BinLeafNode && o2 instanceof BinLeafNode) {
            if (o1.equals(o2)) {
                terms.add(o1.clone());
            }
        } else if (((o1 instanceof BinaryOperation) && (o2 instanceof BinLeafNode)) || ((o1 instanceof BinLeafNode) && (o2 instanceof BinaryOperation))) {
            BinaryOperation binOp = (BinaryOperation) (o1 instanceof BinaryOperation ? o1 : o2);
            Operable op = o1 instanceof BinaryOperation ? o2 : o1;
            ArrayList<Operable> pool = binOp.flattened();
            if (Operable.contains(pool, op))
                terms.add(op.clone());
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
                        terms.add(op1.clone());
                        break;
                    }
                }
            }
        }
        return terms;
    }

    /**
     * @param o the operable to be replaced
     * @param r the operable to take o's place
     * @return the original operable with o replaced by r.
     */
    Operable replace(Operable o, Operable r);

    static boolean isMultiVar(Operable o) {
        return numVars(o) > 1;
    }

    boolean isUndefined();
}
