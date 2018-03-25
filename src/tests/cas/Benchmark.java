package tests.cas;

import jmc.cas.Compiler;
import jmc.cas.Operable;
import jmc.utils.Timer;
import tests.AutoTest;
import tests.TestPrint;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/24/18.
 * Benchmark
 */
public class Benchmark {
    public static void main(String args[]) throws Exception {
        l("Running...");
        Timer timer = new Timer();
        TestPrint.DISABLED = true;
        AutoTest.WRITE = false;
        Operable operable = Compiler.compile("derivative(x*cos(x)*sin(x)*ln(x),x,5)");
        for (int i = 0; i < 5; i++) {
            AutoTest.main(args);
            l(operable.copy().simplify());
            l(Compiler.compile("(a+c+b+d+f+e+g+i+h+j)*(a+e+c+f+h+j+b+d+g+i)").simplify());
        }
        TestPrint.DISABLED = false;
        l("Done... finished within " + timer);
    }
}
