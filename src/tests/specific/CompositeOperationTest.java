package tests.specific;

import jmc.cas.Compiler;
import jmc.cas.components.Constants;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;
import jmc.cas.operations.Argument;
import jmc.cas.operations.CustomOperation;
import jmc.cas.operations.Manipulation;
import jmc.cas.operations.Signature;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/17/18.
 * Composite Operation Test
 */
public class CompositeOperationTest {
    public static void main(String args[]) {
        l(Compiler.compile("a+log(3+a)+4"));
        CustomOperation co = (CustomOperation) Compiler.compile("sum(4+7+5,5+x,log(7+cos(x)),x)");
        l(co); //christ I finally did it!!!
        l(co.eval(5));
        l(co.simplify());
        l(RawValue.ONE.div(new Variable("x")).mult(Constants.E));
        l(RawValue.ONE.negate().div(RawValue.ONE.sub(new Variable("x").sq()).sqrt()));

//        l(Compiler.compile("a+log(3+a)+4"));
        l(Argument.NUMBER.equals(Argument.ANY));
        l(Argument.ANY.equals(Argument.NUMBER));
        l(Argument.VARIABLE.equals(Argument.NUMBER));
        l(Argument.NUMBER.equals(Argument.NUMBER));
        l(Argument.OPERATION.equals(Argument.NUMBER));
        l(Compiler.compile("expand(a*(b+c))").exec());

        CustomOperation.register(new Manipulation("custom", new Signature(Argument.ANY), operands -> {
            double calc = Math.log(operands.get(0).numNodes());
            return new RawValue(calc);
        }));

        l(Compiler.compile("custom(x+b-c)").val());
        CustomOperation.unregister("custom", Signature.ANY);
        l(Compiler.compile("custom(x+b-c)").val());
    }
}
