package jmc.cas;

/**
 * Created by Jiachen on 3/17/18.
 * Argument type
 */
public enum Argument {
    DECIMAL,
    INTEGER,
    VARIABLE,
    ANY,
    OPERATION;

    public Argument resolve(Operable o) {
        if (!o.isNaN()) {
            return RawValue.isInteger(o.val()) ? INTEGER : DECIMAL;
        } else if (o instanceof Variable) {
            return VARIABLE;
        } else if (o instanceof Operation) {
            return OPERATION;
        }
        throw new JMCException("cannot resolve argument type \"" + o + "\"");
    }
}
