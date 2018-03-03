package tests;

import jmc.cas.*;
import jui.*;
import processing.core.PApplet;

/**
 * Created by Jiachen on 16/05/2017.
 * toExponentialForm test
 */
public class CasComprehensiveTest {
    public static void main(String args[]) {
        //to exponential form test
        Operation operation = (Operation) Expression.interpret("x/(x-1)/(x+1/(x-1))");
        operation.toExponentialForm();
        operation.toAdditionOnly();
        System.out.println(Expression.colorMathSymbols(operation.toString()));

        //to addition only test
        operation = (Operation) Expression.interpret("x-3x^2+4x-5");
        operation.toAdditionOnly();
        System.out.println(Expression.colorMathSymbols(operation.toString()));

        //plug in test
        operation = (Operation) Expression.interpret("ln<log<x^(2*e^2+x)>>^(1/5)/(x^3+2*x+9)^(1/3*e*x)");
        operation.plugIn(new Variable("x"), Expression.interpret("h"));
        System.out.println(operation);

        //fraction test
        System.out.println(Fraction.convertToFraction(3.1415926535766));
        System.out.println(Fraction.convertToFraction(3.1415926535766, 5E-14));

    }
}
