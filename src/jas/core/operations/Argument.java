package jas.core.operations;

import jas.core.JMCException;
import jas.core.Node;
import jas.core.components.*;

/**
 * Created by Jiachen on 3/17/18.
 * Argument type
 */
public enum Argument {
    NUMBER,
    VARIABLE,
    ANY,
    OPERATION,
    MATRIX,
    LIST,
    VECTOR,
    LITERAL;

    public static Argument resolve(Node o) {
        if (!o.isNaN() || o instanceof RawValue) {
            return NUMBER;
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
