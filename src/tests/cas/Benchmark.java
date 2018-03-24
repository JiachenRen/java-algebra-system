package tests.cas;

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
        AutoTest.main(args);
        TestPrint.DISABLED = false;
        l("Done... finished within " + timer);
    }
}
