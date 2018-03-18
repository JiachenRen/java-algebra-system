package jmc.cas.operations;

import jmc.cas.BinLeafNode;
import jmc.cas.JMCException;
import jmc.cas.Nameable;
import jmc.cas.Operable;

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
        define("sum", Signature.ANY, (operands -> operands.stream().reduce(Operable::add).get()));
    }


    public CompositeOperation(String name, Operable... operands) {
        this(name, wrap(operands));
    }

    public CompositeOperation(String name, ArrayList<Operable> operands) {
        super(operands);
        this.manipulation = resolveManipulation(name, Signature.resolve(operands));
    }

    private static RegisteredManipulation resolveManipulation(String name, Signature signature) {
        ArrayList<RegisteredManipulation> candidates = registeredManipulations.stream()
                .filter(o -> o.getName().equals(name))
                .collect(Collectors.toCollection(ArrayList::new));
        for (RegisteredManipulation manipulation : candidates) { //prioritize explicit signatures
            if (manipulation.getSignature().equals(signature)) {
                return manipulation;
            }
        }
        for (RegisteredManipulation manipulation : candidates) {
            if (manipulation.getSignature().equals(Signature.ANY)) {
                return manipulation;
            }
        }
        throw new JMCException("cannot resolve operation \"" + name + "\" with signature " + signature);
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

    @Override
    public Operable simplify() {
        super.simplify();
        return manipulation.manipulate(getOperands());
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
