package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import static jmc.utils.ColorFormatter.*;

/**
 * Created by Jiachen on 3/7/18.
 * Simplification Test
 */
public class CasSimplificationTest {
    private static String ops[] = new String[]{
            "1*x",
            "0*x",
            "0^x",
            "x^0",
            "0^0",
            "1^0",
            "0/0",
            "x/0",
            "x/x",
            "x-0",
            "0+0",
            "0/1",
            "x+1",
            "x-1"
    };

    public static void main(String args[]) {
        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Expression::interpret).collect(Collectors.toList());
        operables.forEach(operable -> l(operable
                + boldBlack("\t->\t")
                + lightGreen(operable.clone().simplify().toString())
                + boldBlack("\t->\t")
                + operable.clone().simplify().simplify()));
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
