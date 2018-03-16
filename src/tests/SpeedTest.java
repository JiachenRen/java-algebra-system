package tests;

import jmc.Function;
import jmc.cas.Compiler;

/**
 * Created by Jiachen on 04/05/2017.
 * Speed comparison between Java compiled function and Interpreted JMC function.
 * Test result: interpreted JMC function is approximately 8/9 the speed of Java compiled function
 */
public class SpeedTest {
    private final static double start = 0, end = 1000, step = 0.1;
    private final static String expStr = "sin(cos(x^(x+sec(x)^3))+12x)";
    private static Function interpretedFunc = Function.implement(Compiler.compile(expStr));
    private static Function javaCompiledFunction = Function.implement(val -> Math.sin(Math.cos(Math.pow(val, val + Math.pow(1 / Math.cos(val), 3)) + 12 * val)));

    //Java compiled function that is the same as "sin(cos(x^(x+sec(x)^3))+12x)"

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
    private static Thread trd3 = new Thread(() -> {
        final long initAbsMillis = System.currentTimeMillis();
        for (double i = start; i <= end; i += step)
            Compiler.compile(expStr).eval(i);
        System.out.print("Runtime compiled JMC function finished within " + (System.currentTimeMillis() - initAbsMillis) + " ms");
    });


    public static void main(String args[]) {
        System.out.println("x is from " + start + " to " + end + " with a step size of " + step);
        System.out.println("# computations: " + (int) ((end - start) / step));
        trd1.start();
        trd2.start();
        trd3.start();
        System.out.println("Computing...");
    }
}
