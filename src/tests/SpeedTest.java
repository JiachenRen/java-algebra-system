package tests;

import jmc_lib.Function;

/**
 * Created by Jiachen on 04/05/2017.
 */
public class SpeedTest {
    public static void main(String args[]) {
        Function func = new Function() {
            @Override
            public double eval(double val) {
                return Math.sin(Math.cos(Math.pow(val, val + Math.pow(1 / Math.cos(val), 3)) + 12 * val));
            }
        };

        Function interpreted = Function.interpret("sin<cos<x^(x+sec<x>^3)>+12x>");

        Function osaFunc = Function.evalOsaScript("3 * x^2 + 2 * x + 4");
        //System.out.println(osaFunc.eval(7.954));
        //System.out.println(func.eval(7.954));
        osaJFuncSpeedInspection(osaFunc, func, interpreted, "3 * x^2 + 2 * x + 4");

    }

    private static void osaJFuncSpeedInspection(Function osaFunc, Function jFunc, Function iFunc, String raw) {
        final double start = 0, end = 1000000, step = 0.1;

        //osaFunction thread
        Thread osaThread = new Thread(() -> {
            final long initAbsMillis = System.currentTimeMillis();
            for (double i = start; i <= end; i += step)
                osaFunc.eval(i);
            System.out.print("OSA: ");
            System.out.println(System.currentTimeMillis() - initAbsMillis);
        });

        //Function thread
        Thread jFuncThread = new Thread(() -> {
            final long initAbsMillis = System.currentTimeMillis();
            for (double i = start; i <= end; i += step)
                jFunc.eval(i);
            System.out.print("JF: ");
            System.out.println(System.currentTimeMillis() - initAbsMillis);
        });

        //Interpreted thread: internal architecture designed by Jiachen Ren
        Thread iFuncThread = new Thread(() -> {
            final long initAbsMillis = System.currentTimeMillis();
            for (double i = start; i <= end; i += step)
                iFunc.eval(i);
            System.out.print("IF: ");
            System.out.println(System.currentTimeMillis() - initAbsMillis);
        });

        //Runtime Interpretation thread: internal architecture designed by Jiachen Ren
        Thread rIFuncThread = new Thread(() -> {
            final long initAbsMillis = System.currentTimeMillis();
            for (double i = start; i <= end; i += step)
                Function.interpret(raw).eval(i);
            System.out.print("RIF: ");
            System.out.println(System.currentTimeMillis() - initAbsMillis);
        });

        //initializing multithreading...
        osaThread.start();
        jFuncThread.start();
        iFuncThread.start();
        //rIFuncThread.start();
    }
}
