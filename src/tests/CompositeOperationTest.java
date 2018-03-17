package tests;

import jmc.cas.Compiler;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/17/18.
 * Composite Operation Test
 */
public class CompositeOperationTest {
    public static void main(String args[]) {
        l(Compiler.compile("comp(a,b,comp(a,c))+x/c"));

    }
}
