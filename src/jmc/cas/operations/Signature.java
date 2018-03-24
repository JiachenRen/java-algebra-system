package jmc.cas.operations;

import jmc.cas.Operable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 3/17/18.
 * Signature
 */
public class Signature {
    public static final Signature ANY = new Signature();
    private Argument[] args;

    public Signature(Argument... args) {
        this.args = args;
    }

    public Signature(int numArgs) {
        args = new Argument[numArgs];
        Arrays.fill(args, Argument.ANY);
    }

    private Signature(ArrayList<Argument> args) {
        this(args.toArray(new Argument[args.size()]));
    }

    public boolean equals(Signature other) {
        if (other.args.length == 0 || args.length == 0) return true; // special case
        if (other.args.length != this.args.length) return false;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].equals(other.args[i]))
                return false;
        }
        return true;
    }

    static Signature resolve(ArrayList<Operable> args) {
        return new Signature(args.stream()
                .map(Argument::resolve)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public String toString() {
        if (this.args == null || args.length == 0) return "ANY...";
        ArrayList<Argument> args = new ArrayList<>();
        Collections.addAll(args, this.args);
        Optional<String> str = args.stream().map(Enum::toString).reduce((a, b) -> a + ", " + b);
        return str.orElse("");
    }
}
