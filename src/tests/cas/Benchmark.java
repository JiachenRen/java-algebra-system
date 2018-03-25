package tests.cas;

import jmc.cas.Compiler;
import jmc.cas.Mode;
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
        AutoTest.WRITE = false;
//        Mode.COMPACT = false;
        Operable operable = Compiler.compile("derivative(x*cos(x)*sin(x)*ln(x),x,5)");
        for (int i = 0; i < 5; i++) {
            TestPrint.DISABLED = true;
            AutoTest.main(args);
            l(operable.copy().simplify().coloredString());
            l(Compiler.compile("(a+c+b-d+f+e+g+i+h+j)*(a+e+c+f+h+j+b-d+g+i)").simplify().coloredString());
            l(Compiler.compile("(a+c+b+d+f+e+g+i+h+j)*(a+e+c+f+h+j+b+d+g+i)").expand().simplify().coloredString());
            l(Compiler.compile("derivative(ln(x)*x*cos(x),x,10)").simplify().eval(6));
            TestPrint.DISABLED = false;
        }
        l("Done... finished within " + timer);
    }
}
