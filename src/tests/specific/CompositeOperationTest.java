package tests.specific;

import jmc.cas.operations.Argument;
import jmc.cas.Compiler;
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
        l(co);
        l(co.eval(5));

//        l(Compiler.compile("a+log(3+a)+4"));
        l(Argument.DECIMAL.equals(Argument.ANY));
        l(Argument.ANY.equals(Argument.DECIMAL));
        l(Argument.VARIABLE.equals(Argument.DECIMAL));
        l(Argument.INTEGER.equals(Argument.DECIMAL));
        l(Argument.OPERATION.equals(Argument.INTEGER));
    }
}
