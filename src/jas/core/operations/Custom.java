package jas.core.operations;

import jas.core.*;
import jas.core.components.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static jas.core.Mode.*;
import static jas.core.operations.Argument.*;
import static jas.utils.ColorFormatter.color;

/**
 * Created by Jiachen on 3/17/18.
 * Custom Operation
 */
public class Custom extends Operation implements BinLeafNode, Nameable {
    private static ArrayList<Manipulation> manipulations = new ArrayList<>();
    private Manipulation manipulation;

    static {
        define(Calculus.SUM, Signature.ANY, (operands -> operands.stream().reduce(Node::add).get()));
        define(Calculus.DERIVATIVE, new Signature(ANY, VARIABLE), (operands -> operands.get(0).firstDerivative((Variable) operands.get(1))));
        define(Calculus.DERIVATIVE, new Signature(ANY, VARIABLE, NUMBER), (operands -> {
            double i = operands.get(2).val();
            if (!RawValue.isInteger(i)) throw new JMCException("order of derivative must be an integer");
            return operands.get(0).derivative((Variable) operands.get(1), (int) i);
        }));
        define("simplify", new Signature(ANY), operands -> operands.get(0).simplify());
        define("simplest", new Signature(ANY), operands -> operands.get(0).simplest());
        define("expand", new Signature(ANY), operands -> operands.get(0).expand());
        define("mod", new Signature(ANY, ANY), operands -> {
            double a = operands.get(0).val();
            double b = operands.get(1).val();
            if (Double.isNaN(a) || Double.isNaN(b)) return new Custom("mod", operands);
            return new RawValue(a % b); //NOTE: never return a new CompositeOperation! Causes StackOverflow
        });
        define("num_nodes", new Signature(ANY), operands -> new RawValue(operands.get(0).numNodes()));
        define("num_vars", new Signature(ANY), operands -> new RawValue(operands.get(0).numVars()));
        define("complexity", new Signature(ANY), operands -> new RawValue(operands.get(0).complexity()));
        define("replace", new Signature(ANY, ANY, ANY), operands -> operands.get(0).replace(operands.get(1), operands.get(2)));
        define("random", new Signature(NUMBER), operands -> new RawValue(Math.random() * operands.get(0).val()));
        define("random", new Signature(NUMBER, NUMBER), operands -> {
            double a = operands.get(0).val();
            double b = operands.get(1).val();
            return new RawValue((b - a) * Math.random() + a);
        });
        define("beautify", new Signature(ANY), operands -> operands.get(0).beautify());
        define("val", new Signature(ANY), operands -> new RawValue(operands.get(0).val()));
        define("eval", new Signature(ANY, NUMBER), operands -> new RawValue(operands.get(0).eval(operands.get(1).val())));

        define("define", new Signature(LITERAL, NUMBER), operands -> {
            String constant = ((Literal) operands.get(0)).get();
            Constants.define(constant, () -> operands.get(1).val());
            return Constants.get(constant);
        });

        define("define", new Signature(LITERAL, LIST, ANY), operands -> {
            String name = ((Literal) operands.get(0)).get();
            List arguments = ((List) operands.get(1));
            final Node operation = operands.get(2);
            Signature signature = new Signature(arguments.size());

            // unregister existing manipulations with the same signature.
            ArrayList<Manipulation> overridden = unregister(name, signature);
            String s = "Overridden: " + toString(overridden);
            Literal msg = new Literal(overridden.size() == 0 ? "Done." : s);
            define(name, signature, feed -> {
                ArrayList<Node> args = arguments.unwrap();
                Node tmp = operation.copy();
                for (int i = 0; i < args.size(); i++) {
                    Node arg = args.get(i);
                    tmp = tmp.replace(arg, feed.get(i));
                }
                return tmp.simplify();
            });
            return msg;
        });

        define("binary", new Signature(LITERAL, NUMBER, OPERATION), operands -> {
            String operator = ((Literal) operands.get(0)).get();
            RawValue priority = ((RawValue) operands.get(1));
            Node operation = operands.get(2);
            ArrayList<Node> variables = operation.extractVariables().stream()
                    .map(o -> (Node) o)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (variables.size() != 2 || !Node.contains(variables, new Variable("a"))
                    || !Node.contains(variables, new Variable("b"))) {
                throw new JMCException("definition of binary operation should use only contain 2 variables, [a,b]");
            }
            if (!priority.isInteger()) throw new JMCException("priority must be an integer");
            if (operator.length() > 1) throw new JMCException("binary operator can only be a single character");
            if (Assets.isSymbol(operator.charAt(0)) || Assets.isValidVarName(operator))
                throw new JMCException("reserved symbol '" + operator + "', choose a different one");
            Binary.define(operator, (int) priority.val(), (a, b) -> {
                Node tmp = operation.copy();
                return tmp.replace(new Variable("a"), new RawValue(a))
                        .replace(new Variable("b"), new RawValue(b))
                        .val();
            });
            return new Literal("a" + operator + "b = " + operation);
        });

        define("store", new Signature(LITERAL, ANY), operands -> {
            Variable v = ((Variable) operands.get(0));
            Node o = operands.get(1);
            Variable.store(o, v.getName());
            return o;
        });

        define("del_var", Signature.ANY, operands -> {
            ArrayList<String> deleted = new ArrayList<>();
            operands.forEach(o -> {
                if (!(o instanceof Literal))
                    throw new JMCException("illegal argument [" + o + "]; argument must be literal");
                deleted.add(o.toString().replace("'", "") + ":" +
                        Optional.ofNullable(Variable.del(((Literal) o).get()))
                                .orElse(RawValue.UNDEF)
                                .toString());
            });
            return new Literal("[" + deleted.stream()
                    .reduce((a, b) -> a + "," + b)
                    .orElse("") + "]");
        });

        define("del", Signature.ANY, operands -> {
            ArrayList<Manipulation> removed = new ArrayList<>();
            operands.forEach(o -> {
                if (!(o instanceof Literal)) throw new JMCException("illegal argument " + o);
                removed.addAll(unregister(((Literal) o).get(), Signature.ANY));
            });
            return new Literal("Deleted: " + toString(removed));
        });
    }

    private static String toString(ArrayList<Manipulation> manipulations) {
        return "[" + manipulations.stream()
                .map(Manipulation::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("") + "]";
    }


    public static ArrayList<Manipulation> unregister(String name, Signature signature) {
        ArrayList<Manipulation> unregistered = new ArrayList<>();
        for (int i = manipulations.size() - 1; i >= 0; i--) {
            Manipulation manipulation = manipulations.get(i);
            if (manipulation.equals(name, signature))
                unregistered.add(manipulations.remove(i));
        }
        return unregistered;
    }


    public Custom(String name, Node... operands) {
        this(name, wrap(operands));
    }

    public Custom(String name, ArrayList<Node> operands) {
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
        Optional<String> args = getOperands().stream()
                .map(Node::toString)
                .reduce((a, b) -> a + "," + b);
        return getName() + "(" + (args.orElse("")) + ")";
    }

    public double val() {
        Node node = manipulation.manipulate(getOperands());
        if (node.equals(this)) return Double.NaN;
        return node.val();
    }

    public String getName() {
        return manipulation.getName();
    }

    public double eval(double x) {
        return manipulation.manipulate(getOperands()).eval(x);
    }

    @Override
    public Node simplify() {
        super.simplify();
        /* special case: when f(x) is defined as f(ANY), the default behavior is to distribute the custom operation
        to each element in the List accordingly, such that define('f',{x},x^2+2x+1), f({a,b}) = {a^2+2a+1,b^2+2b+1}
         */
        if (manipulation.getSignature().equals(new Signature(ANY)) && getOperand(0) instanceof List) {
            return ((List) getOperand(0)).customOp(this).simplify();
        }
        Node manipulated = manipulation.manipulate(getOperands());
        if (manipulated.equals(this)) return this;
        return manipulated.simplify();
    }

    public Node exec() {
        return manipulation.manipulate(getOperands());
    }

    /**
     * @return string representation of the node coded with Ansi color codes.
     */
    @Override
    public String coloredString() {
        Optional<String> args = getOperands().stream()
                .map(Node::coloredString)
                .reduce((a, b) -> a + color(",", COMMA_COLOR) + b);
        return color(getName(), CUSTOM_OP_COLOR) + color("(", PARENTHESIS_COLOR) + (args.orElse("")) + color(")", PARENTHESIS_COLOR);
    }

    @Override
    public Node firstDerivative(Variable v) {
        return this.simplify().firstDerivative(v);
    }

    public Custom copy() {
        return new Custom(getName(), getOperands().stream()
                .map(Node::copy)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public boolean equals(Node o) {
        if (!super.equals(o)) return false;
        if (o instanceof Custom) {
            Custom co = ((Custom) o);
            return co.getName().equals(getName());
        }
        return false;
    }
}
