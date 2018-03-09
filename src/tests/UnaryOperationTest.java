package tests;

import jmc.cas.Expression;
import jmc.cas.Operable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static jmc.utils.ColorFormatter.boldBlack;
import static jmc.utils.ColorFormatter.lightGreen;
import static tests.TestPrint.*;

/**
 * Created by Jiachen on 3/8/18.
 * UnaryOperation Test
 */
public class UnaryOperationTest {
    private static String ops[] = new String[]{
            "tan(3pi/2)",
            "tan(3pi/2*x)"
    };
    public static void main(String args[]) {
        l(Expression.interpret("tan(3pi/2)").isUndefined());

        ArrayList<String> raw = new ArrayList<>();
        Collections.addAll(raw, ops);
        ArrayList<Operable> operables;
        operables = (ArrayList<Operable>) raw.stream().map(Expression::interpret).collect(Collectors.toList());
        operables.forEach(operable -> l(operable
                + boldBlack("\t->\t")
                + lightGreen(operable.clone().simplify().toString())
                + boldBlack("\t->\t")
                + operable.clone().simplify().beautify()));
    }


}
