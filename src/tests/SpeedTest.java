package tests;

import jmc.Function;
import jmc.cas.Expression;

/**
 * Created by Jiachen on 04/05/2017.
 * Speed comparison between Java compiled function and Interpreted JMC function.
 * Test result: interpreted JMC function is approximately 8/9 the speed of Java compiled function
 */
public class SpeedTest {
    private final static double start = 0, end = 1000000, step = 0.1;
    private final static String expStr = "sin(cos(x^(x+sec(x)^3))+12x)";
    private static Function interpretedFunc = Expression.interpret(expStr);
    private static Function javaCompiledFunction = new Function() {
        @Override
        public double eval(double val) {
            //Java compiled function that is the same as "sin(cos(x^(x+sec(x)^3))+12x)"
            return Math.sin(Math.cos(Math.pow(val, val + Math.pow(1 / Math.cos(val), 3)) + 12 * val));
        }
    };

    //Java compiled function eval thread
    private static Thread trd1 = new Thread(() -> {
        final long initAbsMillis = System.currentTimeMillis();
        for (double i = start; i <= end; i += step)
            javaCompiledFunction.eval(i);
        System.out.println("Java compiled function finished within " + (System.currentTimeMillis() - initAbsMillis) + " ms");
    });

    //JMC interpreted function eval thread
    private static Thread trd2 = new Thread(() -> {
        final long initAbsMillis = System.currentTimeMillis();
        for (double i = start; i <= end; i += step)
            interpretedFunc.eval(i);
        System.out.println("JMC interpreted function finished within " + (System.currentTimeMillis() - initAbsMillis) + " ms");
    });

    //JMC runtime interpreted function eval thread
    /*
    private static Thread trd3 = new Thread(() -> {
        final long initAbsMillis = System.currentTimeMillis();
        for (double i = start; i <= end; i += step)
            Function.interpret(expStr).eval(i);
        System.out.print("RIF: ");
        System.out.println(System.currentTimeMillis() - initAbsMillis);
    });
    */

    public static void main(String args[]) {
        System.out.println("x is from " + start + " to " + end + " with a step size of " + step);
        System.out.println("# computations: " + (int) ((end - start) / step));
        trd1.start();
        trd2.start();
        System.out.println("Computing...");
//        trd3.start();
    }
}
