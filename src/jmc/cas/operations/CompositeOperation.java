package jmc.cas.operations;

import jmc.cas.*;
import jmc.cas.components.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static jmc.cas.operations.Argument.*;

/**
 * Created by Jiachen on 3/17/18.
 * Composite Operation
 */
public class CompositeOperation extends Operation implements BinLeafNode, Nameable {
    private static ArrayList<Manipulation> manipulations = new ArrayList<>();
    private Manipulation manipulation;

    static { //TODO: automatically link CAS operations with Operable methods using reflect.
        define(Calculus.SUM, Signature.ANY, (operands -> operands.stream().reduce(Operable::add).get()));
        define(Calculus.DERIVATIVE, new Signature(ANY, VARIABLE), (operands -> operands.get(0).firstDerivative((Variable) operands.get(1))));
        define(Calculus.DERIVATIVE, new Signature(ANY, VARIABLE, INTEGER), (operands -> operands.get(0).derivative((Variable) operands.get(1), (int) operands.get(2).val())));
        define("simplify", new Signature(ANY), operands -> operands.get(0).simplify());
        define("simplest", new Signature(ANY), operands -> operands.get(0).simplest());
        define("expand", new Signature(ANY), operands -> operands.get(0).expand());
        define("num_nodes", new Signature(ANY), operands -> new RawValue(operands.get(0).numNodes()));
        define("complexity", new Signature(ANY), operands -> new RawValue(operands.get(0).complexity()));
        define("replace", new Signature(ANY, ANY, ANY), operands -> operands.get(0).replace(operands.get(1), operands.get(2)));
        define("beautify", new Signature(ANY), operands -> operands.get(0).beautify());
        define("val", new Signature(ANY), operands -> new RawValue(operands.get(0).val()));
        define("eval", new Signature(ANY, DECIMAL), operands -> new RawValue(operands.get(0).eval(operands.get(1).val()))); //TODO: Argument type Number
        define("eval", new Signature(ANY, INTEGER), operands -> new RawValue(operands.get(0).eval(operands.get(1).val()))); //method overloading

        Manipulable constClosure = operands -> {
            String constant = ((Literal) operands.get(0)).get();
            Constants.define(constant, () -> operands.get(1).val());
            return Constants.get(constant);
        };

        define("define", new Signature(LITERAL, INTEGER), constClosure);
        define("define", new Signature(LITERAL, DECIMAL), constClosure);

        define("define", new Signature(LITERAL, LIST, ANY), operands -> {
            String name = ((Literal) operands.get(0)).get();
            List arguments = ((List) operands.get(1));
            final Operable operable = operands.get(2);
            define(name, Signature.ANY, feed -> {
                ArrayList<Operable> unwrap = arguments.unwrap();
                Operable captured = operable.copy();
                for (int i = 0; i < unwrap.size(); i++) {
                    Operable arg = unwrap.get(i);
                    captured = captured.replace(arg, feed.get(i));
                }
                return captured.simplify();
            });
            return new Literal("Done.");
        });
    }


    public CompositeOperation(String name, Operable... operands) {
        this(name, wrap(operands));
    }

    public CompositeOperation(String name, ArrayList<Operable> operands) {
        super(operands);
        this.manipulation = resolveManipulation(name, Signature.resolve(operands));
    }

    private static Manipulation resolveManipulation(String name, Signature signature) {
        ArrayList<Manipulation> candidates = manipulations.stream()
                .filter(o -> o.getName().equals(name))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Manipulation manipulation : candidates) { //prioritize explicit signatures
            if (manipulation.getSignature().equals(signature)) {
                return manipulation;
            }
        }
        for (Manipulation manipulation : candidates) {
            if (manipulation.getSignature().equals(Signature.ANY)) {
                return manipulation;
            }
        }
        throw new JMCException("cannot resolve operation \"" + name + "\" with signature " + signature);
    }

    public static ArrayList<Manipulation> registeredManipulations() {
        return manipulations;
    }

    public static void define(String name, Signature signature, Manipulable manipulable) {
        manipulations.add(new Manipulation(name, signature, manipulable));
    }

    public static void register(Manipulation manipulation) {
        manipulations.add(manipulation);
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

    public Operable exec() {
        return manipulation.manipulate(getOperands());
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
