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
        Operable operable = Expression.interpret("x/(x-1)/(x+1/(x-1))");
        Operable exponentialForm = ((Operation) operable).toExponentialForm();
        System.out.println(Expression.colorMathSymbols(exponentialForm.toString()));

        operable = Expression.interpret("x-3x^2+4x-5");
        Operable additionOnly = ((Operation) operable).toAdditionOnly();
        System.out.println(Expression.colorMathSymbols(additionOnly.toString()));
    }
}
