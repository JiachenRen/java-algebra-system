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
     * plugs in the operable nested for all variables in the expression
     * NOTE: the method returns the operable with the desired nested operable plugged in, but
     * the operable itself is not altered.
     *
     * @param var the variable to be replaced
     * @param replacement the operable to be plugged in
     * @return the resulting operable with nested plugged in
     */
    Operable plugIn(Variable var, Operable replacement);
}
