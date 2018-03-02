package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 25/05/2017.
 * (x+3)(x-3)
 * (x+3)(x-3)(x+3)(x-4)
 * x^3*x^4
 * x^3*x^x
 * x^3*x^(3+4+x) -failed -fixed May 26th
 * (x+2)+(2x+2) -not working for now -fixed May 26th
 * (x+3)(x-3)x -fixed May 26th
 * (x^2-9)(x-4) -huge bug!
 * x^2*(-20)+(-9)*x^2
 * "x+h-x" -doesn't work yet!
 */
public class CasExpansionTest {
    private static String testSubjects[] = new String[]{
            "ln<log<x^(2*e^2+x)>>^(1/5)/(x^3+2*x+9)^(1/3*e*x)",
            "(x+3)(x-3)",
            "(x+3)(x-3)(x+3)(x-4)",
            "x^3*x^4",
            "x^3*x^x",
            "x^3*x^(3+4+x)",
            "(x+2)+(2x+2)",
            "(x+3)(x-3)x",
            "(x^2-9)(x-4)",
            "x^2*(-20)+(-9)*x^2",
            "x+2h",
            "(a+b)(a-d)",
            "(a+2b)(a-d)",
            "(x+a)*x^2",

    };

    private static ArrayList<Operable> operables;

    public static void main(String args[]) {
         synopticDiagnosis();
//        Operable operable = Function.interpret("(x+3)(x-sin<x>)/(x-1)*x(x+2)").getOperable();
//        operable = Operable.getFirstDerivative(operable);
//        //operable = Operable.expand(operable); TODO: debug
//        System.out.println(operable);
    }

    private static void synopticDiagnosis() {
        operables = new ArrayList<>();
        for (String expression : testSubjects) {
            Operable extracted = Expression.interpret(expression).getOperable();
            extracted = Operable.expand(extracted);
            operables.add(extracted);
        }
        for (int i = 0; i < operables.size(); i++) {
            l("original:\t" + f(testSubjects[i]));
            l("expanded:\t" + f(operables.get(i).toString()));
        }
    }

    private static void l(String s) {
        System.out.println(s);
    }

    private static String f(String s) {
        return Expression.colorMathSymbols(s);
    }

    private static void inspect(String s) {
        Operable extracted = Expression.interpret(s).getOperable();
        extracted = Operable.expand(extracted);
        l((char) 27 + "[31;1m" + "expanded: " + (char) 27 + "[0m" + f(extracted.toString()));
    }
}
