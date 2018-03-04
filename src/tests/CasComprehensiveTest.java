package tests;

import jmc.cas.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static jmc.utils.ColorFormatter.*;

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
        l(Expression.colorMathSymbols(operation.toString()));

        //to addition only test
        operation = (Operation) Expression.interpret("x-3x^2+4x-5");
        operation.toAdditionOnly();
        l(Expression.colorMathSymbols(operation.toString()));

        //plug in test
        operation = (Operation) Expression.interpret("ln<log<x^(2*e^2+x)>>^(1/5)/(x^3+2*x+9)^(1/3*e*x)");
        operation.plugIn(new Variable("x"), Expression.interpret("h"));
        l(operation);

        //fraction test
        RawValue f1 = Fraction.convertToFraction(3.1415926535766);
        RawValue f2 = Fraction.convertToFraction(0.1403508772, 5E-7);
        RawValue f3 = Fraction.convertToFraction(12.375);
        RawValue f4 = Fraction.convertToFraction(12.375, 5E-14);
        RawValue f5 = new Fraction(5, 5);
        RawValue f6 = new Fraction(4, 4).reduce();
        l(f1, f2, f3, f4, f5, f6);

        RawValue raw = new RawValue(14.0);
        RawValue raw1 = new RawValue(14.3);
        l(raw.isInteger());

        Fraction.TOLERANCE = 5E-7;

        Fraction f7 = new Fraction(11, 7);
        Fraction f8 = new Fraction(6, 14);
        l(f7 + " + " + f8 + " = " + f7.clone().add(f8));
        l(f8 + " - " + f7 + " = " + f8.clone().sub(f7));
        l(raw1 + " + " + f7 + " = " + f7.clone().add(raw1));
        l(f7 + " x " + f8 + " = " + f7.clone().mult(f8));
        l(f8 + " x " + raw1 + " = " + f8.clone().mult(raw1));
        l(f8 + " / " + raw1 + " = " + f8.clone().div(raw1));
        l(f8 + " / " + f7 + " = " + f8.clone().div(f7));

        Operation op1 = (Operation) Expression.interpret("(3 + 4.5) * ln(5.3 + 4) / 2.7 / (x + 1) * x / 3");
        l(((BinaryOperation) op1).flattened());
        Operation op2 = (Operation) Expression.interpret("3 - 2x + 4x - 4 + 7pi");
        l(((BinaryOperation) op2).flattened());

        l(op1.numNodes(), op2.numNodes());

        Operable c = new Constants.Constant("e", () -> Math.E);
        System.out.println(c);

        ArrayList<String> ops = new ArrayList<>();
        Collections.addAll(ops, "(3 + 4.5x) * 5.3 / 2.7 * (5x + log(10))", "2^((x^2)^3)", "1473x", "0/0", "0/0 + 0", "5 + 7 -log(11) + e^2","0^x");
        List<Operable> operables;
        operables = ops.stream().map(Expression::interpret).collect(Collectors.toList());
        operables.forEach(op -> {
            l(boldBlack("original: ") + Expression.colorMathSymbols(op.toString()));
            l(lightCyan("arbitrary value: ") + op.val());
            l(lightRed("undefined: ") + op.isUndefined());
            if (op instanceof Operation) {
                l(lightGreen("simplified: ") + Expression.colorMathSymbols(op.clone().simplify().toString()));
            }
            l("");
        });

        System.out.println(new RawValue(5).equals(new RawValue(5.0)));
        System.out.println(RawValue.UNDEF.isUndefined());
        System.out.println(RawValue.UNDEF);
        System.out.println(RawValue.UNDEF.equals(RawValue.UNDEF));
        System.out.println(Fraction.UNDEF.isUndefined());
        System.out.println(new Fraction(3,0).isUndefined());
        System.out.println(Math.log(11));

        Operable operable = Expression.interpret("0^0");
        System.out.println(operable.eval(3));

//        ArrayList<String> arr = new ArrayList<>();
//        Collections.addAll(arr, "a","b","c","d","e","f");
//        System.out.println(arr.subList(1,arr.size()-1));


    }

    private static void l(Object... objects) {
        for (Object o : objects) {
            l(o);
        }
    }

    private static void l(Object o) {
        System.out.println(o);
    }
}
