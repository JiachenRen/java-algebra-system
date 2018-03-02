package tests;

import jmc.Function;
import jmc.Operable;

/**
 * Created by Jiachen on 27/05/2017.
 */
public class CasPlugInTest {
    public static void main(String args[]) {
        Operable operable = Function.interpret("ln<log<x^(2*e^2+x)>>^(1/5)/(x^3+2*x+9)^(1/3*e*x)").getOperable();
        //operable =Operable.expand(operable);
        //operable = operable.plugIn(Function.interpret("x+h").getOperable());
        //operable = Operable.expand(operable);
        operable = Operable.getFirstDerivative(operable);
//        operable = Operable.expand(operable);
        l(f(operable.toString()));
    }

    private static void l(String s) {
        System.out.println(s);
    }

    private static String f(String s) {
        return Function.colorMathSymbols(s);
    }
}
