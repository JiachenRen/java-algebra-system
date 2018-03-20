import jmc.cas.Compiler;
import jmc.cas.JMCException;

import java.util.Scanner;

import static jmc.utils.ColorFormatter.lightGreen;
import static jmc.utils.ColorFormatter.lightRed;
import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/2/18.
 * JMC Computer Algebra System commandline application
 */
public class CasCmdline {
    public static void main(String args[]) {
        System.out.println("Welcome to JMC computer algebra system (CAS)\nDesigned by Jiachen Ren\nMIT licensed (c) 2018\n");
        System.out.println("Please enter commands: ");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit")) return;
            try {
                l(lightGreen(Compiler.compile(input).simplify()) + "\n");
            } catch (JMCException e) {
                l(lightRed(e.getMessage()));
            }
        }
    }
}
