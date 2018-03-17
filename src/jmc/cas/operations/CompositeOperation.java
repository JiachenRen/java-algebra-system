package jmc.cas.operations;

import jmc.cas.*;
import jmc.cas.Operable;
import jmc.cas.components.RawValue;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 3/17/18.
 * Composite Operation
 */
public class CompositeOperation extends Operation implements BinLeafNode, Nameable {
    private static ArrayList<RegisteredManipulation> registeredManipulations = new ArrayList<>();
    private RegisteredManipulation manipulation;

    static {
        define("comp", new Signature(Argument.ANY, Argument.ANY, Argument.ANY), (operands -> new RawValue(0)));
    }


    public CompositeOperation(String name, Operable... operands) {
        this(name, wrap(operands));
    }

    public CompositeOperation(String name, ArrayList<Operable> operands) {
        super(operands);
        this.manipulation = resolveManipulation(name, Signature.resolve(operands));
    }

    public static RegisteredManipulation resolveManipulation(String name, Signature signature) {
        for (RegisteredManipulation manipulation : registeredManipulations) {
            if (manipulation.getName().equals(name) && manipulation.getSignature().equals(signature)) {
                return manipulation;
            }
        }
        throw new JMCException("cannot resolve operation \""+name+"\" with signature "+signature);
    }

    public static ArrayList<RegisteredManipulation> registeredManipulations() {
        return registeredManipulations;
    }

    public static void define(String name, Signature signature, Manipulation manipulation) {
        registeredManipulations.add(new RegisteredManipulation(name, signature, manipulation));
    }

    public static void register(RegisteredManipulation registeredManipulation) {
        registeredManipulations.add(registeredManipulation);
    }


    public String toString() {
        Optional<String> args = getOperands().stream().map(Operable::toString).reduce((a, b) -> a + "," + b);
        return getName() + "(" + (args.isPresent() ? args.get() : "") + ")";
    }

    public double val() {
        return manipulation.manipulate(getOperands()).val();
    }

    public String getName() {
        return manipulation.getName();
    }

    public double eval(double x) {
        return manipulation.manipulate(getOperands()).eval(x);
    }

    public CompositeOperation copy() {
        return new CompositeOperation(getName(), getOperands().stream()
                .map(Operable::copy)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public boolean equals(Operable o) {
        if (!super.equals(o)) return false;
        if (o instanceof CompositeOperation) {
            CompositeOperation co = ((CompositeOperation) o);
            return co.getName().equals(getName());
        }
        return false;
    }
}
