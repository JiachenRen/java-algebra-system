package jmc.cas.operations;

import jmc.cas.JMCException;
import jmc.cas.Operable;
import jmc.cas.components.*;

/**
 * Created by Jiachen on 3/17/18.
 * Argument type
 */
public enum Argument {
    DECIMAL,
    INTEGER,
    VARIABLE,
    ANY,
    OPERATION,
    MATRIX,
    LIST,
    VECTOR,
    LITERAL;

    public static Argument resolve(Operable o) {
        if (!o.isNaN()) {
            return RawValue.isInteger(o.val()) ? INTEGER : DECIMAL;
        } else if (o instanceof Literal) { // be careful, Literal is a subclass of Variable.
            return LITERAL;
        } else if (o instanceof Variable) {
            return VARIABLE;
        } else if (o instanceof Operation) {
            return OPERATION;
        } else if (o instanceof Matrix) {
            return MATRIX;
        } else if (o instanceof List) {
            return LIST;
        } else if (o instanceof Vector) {
            return VECTOR;
        }
        throw new JMCException("cannot resolve argument type \"" + o + "\"");
    }

    public boolean equals(Argument other) {
        return super.equals(ANY) || other == ANY || super.equals(other);
    }
}
