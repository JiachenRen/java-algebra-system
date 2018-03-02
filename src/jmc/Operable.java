package jmc;

/**
 * Created by Jiachen on 03/05/2017.
 * TODO: plugIn(String... vars, Operable... operables);
 * TODO: eval(String... vars, double... values);
 */
public interface Operable extends Evaluable {
    String toString();

    Operable replicate();

    boolean equals(Operable other);

    /**
     * plugs in the operable nested for all variables in the expression
     * NOTE: the method returns the operable with the desired nested operable plugged in, but
     * the operable itself is not altered.
     *
     * @param nested the operable to be plugged in
     * @return the resulting operable with nested plugged in
     */
    Operable plugIn(Operable nested);

    /**
     * NOTE: this method does not alter the operable itself. Instead, it returns an expanded version
     * of the original operable. The operable is also simplified if applicable.
     *
     * @param operable the operable to be expanded
     * @return the expanded version of the input.
     */
    static Operable expand(Operable operable) {
        if (operable instanceof Operation) {
            Operation replica = ((Operation) operable).replicate();
            replica.toExponentialForm();
            System.out.println((char) 27 + "[1m" + "converted to exponential form: " + (char) 27 + "[0m" + Function.colorMathSymbols(replica.toString()));
            Operable potentialOperation = replica.toAdditionOnly();
            System.out.println((char) 27 + "[1m" + "converted to \"+\" only: " + (char) 27 + "[0m" + Function.colorMathSymbols(potentialOperation.toString()));
            if (potentialOperation instanceof Operation) {
                Operable simplified = ((Operation) potentialOperation).simplify(); //recursion removed May 26th.
                if (simplified instanceof Operation && ((Operation) simplified).simplifiable()) {
                    simplified = ((Operation) simplified).simplify();
                }
                return simplified;
            }
            return potentialOperation;
        }
        return operable;
    }

    static Operable getFirstDerivative(Operable operable) {
        operable = operable.replicate(); //is this necessary?
        Operable f1 = operable.plugIn(Function.interpret("x+h").getOperable());
        BinaryOperation subtraction = new BinaryOperation(f1, "-", operable);
        BinaryOperation exp = new BinaryOperation(new Variable("h"), "^", new RawValue(-1));
        return new BinaryOperation(subtraction, "*", exp);
    }
}
