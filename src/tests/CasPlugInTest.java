package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;

/**
 * Created by Jiachen on 27/05/2017.
 * CAS plug in test
 */
public class CasPlugInTest {
    public static void main(String args[]) {
        Operable operable = Expression.interpret("ln<log<x^(2*e^2+x)>>^(1/5)/(x^3+2*x+9)^(1/3*e*x)");
        //operable =Operable.expand(operable);
        //operable = operable.plugIn(GraphFunction.interpret("x+h").getOperable());
        //operable = Operable.expand(operable);
        operable = Operable.getFirstDerivative(operable);
//        operable = Operable.expand(operable);
        l(f(operable.toString()));
    }

    private static void l(String s) {
        System.out.println(s);
    }

    private static String f(String s) {
        return Expression.colorMathSymbols(s);
    }
}
