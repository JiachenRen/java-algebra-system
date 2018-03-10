package tests;

import jmc.cas.BinaryOperation;
import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static tests.TestPrint.l;
import static jmc.utils.ColorFormatter.*;

/**
 * Created by Jiachen on 3/10/18.
 * Automatic Test
 */
public class AutoTest {
    public static void main(String args[]) {
        configureCAS();

        String raw = Utils.read("/tests/files/bin_ops.txt");
        ArrayList<String> lines = new ArrayList<>();
        assert raw != null;

        Collections.addAll(lines, raw.split("\n"));
        ArrayList<Operable> ops = (ArrayList<Operable>) lines.stream()
                .map(l -> Expression.interpret(l.contains("->") ? l.substring(0, l.indexOf("->")) : l))
                .collect(Collectors.toList());

        ArrayList<String> simplifiedStrs = (ArrayList<String>) lines.stream()
                .map(l -> l.contains("->") ? l.substring(l.indexOf("->") + 2) : "")
                .collect(Collectors.toList());

        int maxLength = 0;
        for (Operable op : ops) {
            int l = op.toString().length();
            maxLength = l > maxLength ? l : maxLength;
        }
        maxLength += 1;

        int finalMaxLength = maxLength;
        lines = (ArrayList<String>) ops.stream()
                .map(op -> ensureLength(op.toString(), finalMaxLength))
                .collect(Collectors.toList());

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i), prev = simplifiedStrs.get(i).replace(" ", "");
            String now = ops.get(i).clone().simplify().toString();
            if (prev.equals(now)) {
                l(line + lightGreen("PASSED") + boldBlack(" = ") + lightBlue(prev + " "));
            } else {
                l(line + lightRed("FAILED ")
                        + lightBlue(prev) + lightRed(" ≠ ")
                        + lightCyan(now));
            }
            Operable o1 = Expression.interpret(line), o2 = Expression.interpret(now);
            String diff = passOrPrint(o1.val() - o2.val());
            l(ensureLength("", maxLength) + yellow("RAW DIFF: ") + diff);
            String diff0 = passOrPrint(o1.eval(0) - o2.eval(0));
            l(ensureLength("", maxLength) + yellow("DIFF(x=0): ") + diff0);
            String diff1 = passOrPrint(o1.eval(1) - o2.eval(1));
            l(ensureLength("", maxLength) + yellow("DIFF(x=1): ") + diff1);
            lines.set(i, line + "-> " + now + "\n");
        }

        Optional<String> content = lines.stream().reduce((a, b) -> a + b);
        content.ifPresent(c -> Utils.write("/tests/files/bin_ops.txt", c));
    }

    private static String passOrPrint(double d) {
        if (Double.isNaN(d)) return boldBlack("NO RESULT");
        else if (d == 0 || d < 1E-15) return lightGreen("PASSED");
        else return lightRed(Double.toString(d));
    }

    private static void configureCAS() {
        BinaryOperation.define("%", 2, (a, b) -> a % b);
    }

    private static String ensureLength(String s, int length) {
        while (s.length() < length) {
            s += " ";
        }
        return s;
    }
}
