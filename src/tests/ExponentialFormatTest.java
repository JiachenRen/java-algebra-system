package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.cas.Operation;
import jui.*;
import processing.core.PApplet;

/**
 * Created by Jiachen on 16/05/2017.
 * toExponentialForm test
 */
public class ExponentialFormatTest {
    public static void main(String args[]) {
        Operation operable = (Operation) Expression.interpret("x/(x-1)/(x+1/(x-1))");
        operable.toExponentialForm();
        operable.toAdditionOnly();
        System.out.println(Expression.colorMathSymbols(operable.toString()));

        operable = (Operation) Expression.interpret("x-3x^2+4x-5");
        operable.toAdditionOnly();
        System.out.println(Expression.colorMathSymbols(operable.toString()));
    }
}
