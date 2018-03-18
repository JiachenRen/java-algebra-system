package tests.specific;

import jmc.cas.Compiler;
import jmc.cas.components.Constants;
import jmc.cas.components.RawValue;
import jmc.cas.components.Variable;
import jmc.cas.operations.Argument;
import jmc.cas.operations.CompositeOperation;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/17/18.
 * Composite Operation Test
 */
public class CompositeOperationTest {
    public static void main(String args[]) {
        l(Compiler.compile("a+log(3+a)+4"));
        CompositeOperation co = (CompositeOperation) Compiler.compile("sum(4+7+5,5+x,log(7+cos(x)),x)");
        l(co); //christ I finally did it!!!
        l(co.eval(5));
        l(co.simplify());
        l(RawValue.ONE.div(new Variable("x")).mult(Constants.E));
        l(RawValue.ONE.negate().div(RawValue.ONE.sub(new Variable("x").sq()).sqrt()));

//        l(Compiler.compile("a+log(3+a)+4"));
        l(Argument.DECIMAL.equals(Argument.ANY));
        l(Argument.ANY.equals(Argument.DECIMAL));
        l(Argument.VARIABLE.equals(Argument.DECIMAL));
        l(Argument.INTEGER.equals(Argument.DECIMAL));
        l(Argument.OPERATION.equals(Argument.INTEGER));
    }
}
