package jmc.cas.operations;

import jmc.cas.*;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;

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

    static { //TODO: automatically link CAS operations with Operable methods using reflect.
        define(Calculus.SUM, Signature.ANY, (operands -> operands.stream().reduce(Operable::add).get()));
        define(Calculus.DERIVATIVE, new Signature(Argument.ANY, Argument.VARIABLE), (operands -> operands.get(0).firstDerivative((Variable) operands.get(1))));
        define("simplify", new Signature(Argument.ANY), operands -> operands.get(0).simplify());
        define("expand", new Signature(Argument.ANY), operands -> operands.get(0).expand());
        define("num_nodes", new Signature(Argument.ANY), operands -> new RawValue(operands.get(0).numNodes()));
        define("complexity", new Signature(Argument.ANY), operands -> new RawValue(operands.get(0).complexity()));
        define("replace", new Signature(Argument.ANY, Argument.ANY, Argument.ANY), operands -> operands.get(0).replace(operands.get(1), operands.get(2)));
        define("beautify", new Signature(Argument.ANY), operands -> operands.get(0).beautify());
        define("val", new Signature(Argument.ANY), operands -> new RawValue(operands.get(0).val()));
        define("eval", new Signature(Argument.ANY, Argument.DECIMAL), operands -> new RawValue(operands.get(0).eval(operands.get(1).val()))); //TODO: Argument type Number
        define("eval", new Signature(Argument.ANY, Argument.INTEGER), operands -> new RawValue(operands.get(0).eval(operands.get(1).val()))); //method overloading
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
        return getName() + "(" + (args.orElse("")) + ")";
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
        return manipulation.manipulate(getOperands()).simplify(); //TODO: might cause StackOverflow
    }

    @Override
    public Operable firstDerivative(Variable v) {
        return this.simplify().firstDerivative(v);
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
