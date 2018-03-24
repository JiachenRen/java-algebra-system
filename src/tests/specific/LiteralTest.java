package tests.specific;

import jmc.cas.Compiler;
import jmc.cas.components.Literal;

import static tests.TestPrint.*;

/**
 * Created by Jiachen on 3/23/18.
 * Literal Test
 */
public class LiteralTest {
    public static void main(String args[]){
        l(Compiler.compile("('heef'+x)^2*a").expand().simplify());
        l(new Literal("hello"));

    }
}
