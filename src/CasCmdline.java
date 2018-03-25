import jas.core.Compiler;
import jas.core.JMCException;

import java.util.Scanner;

import static jas.utils.ColorFormatter.*;
import static tests.TestPrint.l;
import static tests.TestPrint.p;

/**
 * Created by Jiachen on 3/2/18.
 * JMC Computer Algebra System commandline application
 */
public class CasCmdline {
    public static void main(String args[]) {
        System.out.println("Welcome to JMC computer algebra system (CAS)\nDesigned by Jiachen Ren\nMIT licensed (c) 2018\n");
        Scanner scanner = new Scanner(System.in);
//        Mode.DEBUG = true;
        while (true) {
            p(boldBlack("\t: "));
            String input = scanner.nextLine();
            if (input.equals("exit")) return;
            try {
                l(boldBlack("\t= ") + Compiler.compile(input).simplify().coloredString() + "\n");
            } catch (JMCException e) {
                l(lightBlue("\t> ") + lightRed(e.getMessage()) + "\n");
            }
        }
    }
}
