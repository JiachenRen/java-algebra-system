package tests;

import jmc.cas.BinaryOperation;
import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.utils.Utils;

import java.lang.reflect.Method;
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
    public static void main(String args[]) throws Exception {
        configureCAS();

        l(boldBlack("Updating expression library... this takes a while..."));
        updateCandidates("additional/", "exponential/", "simplification/", "expansion/", "nodes/");

        l(lightPurple("\n---------------------------> Simplification <---------------------------\n"));
        l(boldBlack("\n---------> Binary Operations Test <----------\n"));
        test("/tests/files/simplification/bin_ops.txt", "simplify", true);
        l(boldBlack("\n---------> Unary Operations Test <-----------\n"));
        test("/tests/files/simplification/u_ops.txt", "simplify", true);
        l(boldBlack("\n---------> Irrational Numbers Test <---------\n"));
        test("/tests/files/simplification/irr_num.txt", "simplify", true);

        l(lightPurple("\n---------------------------> Expansion <---------------------------\n"));
        l(boldBlack("\n---------> Binary Operations Test <----------\n"));
        test("/tests/files/expansion/bin_ops.txt", "expand", true);
        l(boldBlack("\n---------> Unary Operations Test <-----------\n"));
        test("/tests/files/expansion/u_ops.txt", "expand", true);
        l(boldBlack("\n---------> Irrational Numbers Test <---------\n"));
        test("/tests/files/expansion/irr_num.txt", "expand", true);

        l(lightPurple("\n---------------------------> Addition Only Form <------------------------\n"));
        l(boldBlack("\n---------> Binary Operations Test <----------\n"));
        test("/tests/files/additional/bin_ops.txt", "toAdditionOnly", true);
        l(boldBlack("\n---------> Unary Operations Test <-----------\n"));
        test("/tests/files/additional/u_ops.txt", "toAdditionOnly", true);
        l(boldBlack("\n---------> Irrational Numbers Test <---------\n"));
        test("/tests/files/additional/irr_num.txt", "toAdditionOnly", true);

        l(lightPurple("\n---------------------------> Exponential Form <------------------------\n"));
        l(boldBlack("\n---------> Binary Operations Test <----------\n"));
        test("/tests/files/exponential/bin_ops.txt", "toExponentialForm", true);
        l(boldBlack("\n---------> Unary Operations Test <-----------\n"));
        test("/tests/files/exponential/u_ops.txt", "toExponentialForm", true);
        l(boldBlack("\n---------> Irrational Numbers Test <---------\n"));
        test("/tests/files/exponential/irr_num.txt", "toExponentialForm", true);

        l(lightPurple("\n---------------------------> Number of Nodes <-------------------------\n"));
        l(boldBlack("\n---------> Binary Operations Test <----------\n"));
        test("/tests/files/nodes/bin_ops.txt", "numNodes", false);
        l(boldBlack("\n---------> Unary Operations Test <-----------\n"));
        test("/tests/files/nodes/u_ops.txt", "numNodes", false);
        l(boldBlack("\n---------> Irrational Numbers Test <---------\n"));
        test("/tests/files/nodes/irr_num.txt", "numNodes", false);

    }

    private static Method getMethod(Class<Operable> c, String name) {
        try {
            return c.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("this exception will never happen");
    }

    private static ArrayList<String> getLines(String raw) {
        ArrayList<String> lines = new ArrayList<>();
        Collections.addAll(lines, raw.split("\n"));
        return lines;
    }

    /**
     * update the expressions in /additional, /exponential, /simplification according to ones listed in /expressions
     */
    private static void updateCandidates(String... subdirectories) {
        ArrayList<ArrayList<String>> pool = new ArrayList<>();
        String baseDir = "/tests/files/", ext = ".txt";
        String[] types = new String[]{"bin_ops", "u_ops", "irr_num"};
        for (String type : types) {
            pool.add(getLines(Utils.read(baseDir + type + ext)));
        }
        for (String subDir : subdirectories) {
            for (int m = 0; m < types.length; m++) {
                String type = types[m];
                String dir = baseDir + subDir + type + ext;
                ArrayList<String> lines = getLines(Utils.read(dir));
                for (String newLine : pool.get(m)) {
                    if (!contains(lines, newLine)) {
                        lines.add(getOriginal(newLine));
                        l(lightGreen("\"" + getOriginal(newLine) + "\"") + " added to " + lightBlue(dir));
                    }
                }
                writeLines(dir, lines);
                l(dir + lightGreen(" ... UPDATED"));
            }
        }
    }

    private static boolean contains(ArrayList<String> lines, String s) {
        for (String line : lines) {
            String original = getOriginal(line);
            if (Expression.interpret(original).equals(Expression.interpret(getOriginal(s)))) {
                return true;
            }
        }
        return false;
    }

    private static String getOriginal(String l) {
        return l.contains("->") ? l.substring(0, l.indexOf("->")) : l;
    }

    private static String getComputed(String l) {
        return l.contains("->") ? l.substring(l.indexOf("->") + 2) : "";
    }

    private static void test(String fileName, String method, boolean testValue) throws Exception {
        ArrayList<String> lines = getLines(Utils.read(fileName));

        ArrayList<Operable> ops = (ArrayList<Operable>) lines.stream()
                .map(l -> Expression.interpret(getOriginal(l)))
                .collect(Collectors.toList());

        ArrayList<String> simplifiedStrs = (ArrayList<String>) lines.stream()
                .map(AutoTest::getComputed)
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
            Object obj = getMethod(Operable.class, method).invoke(ops.get(i).copy());
            String now = obj.toString();
            if (prev.equals(now)) {
                l(line + lightGreen("PASSED") + boldBlack(" = ") + lightBlue(prev + " "));
            } else {
                l(line + lightRed("FAILED ")
                        + lightBlue(prev) + lightRed(" ≠ ")
                        + lightCyan(now));
            }
            if (testValue) {
                Operable o1 = Expression.interpret(line), o2 = Expression.interpret(now);
                if (Operable.numVars(o1) > 0) {
                    for (int k = 0; k <= 10; k++) {
                        int t = (int) Math.pow(k, 2);
                        double diffx = o1.eval(k) - o2.eval(k);
                        if (diffx != 0) {
                            l(ensureLength("", maxLength) + boldBlack("DIFF(x=" + t + "): ") + passOrPrint(diffx));
                        }
                    }
                } else {
                    double v = o1.val() - o2.val();
                    if (v != 0.0) {
                        l(ensureLength("", maxLength) + boldBlack("RAW DIFF: ") + passOrPrint(v));
                    }
                }
            }

            lines.set(i, line + "-> " + now);
        }

        writeLines(fileName, lines);
    }

    private static void writeLines(String fileName, ArrayList<String> lines) {
        Optional<String> content = lines.stream().map(l -> l + "\n").reduce((a, b) -> a + b);
        content.ifPresent(c -> Utils.write(fileName, c));
    }

    private static String passOrPrint(double d) {
        if (Double.isNaN(d)) return boldBlack("NaN");
        else if (d == 0 || Math.abs(d) < 1E-10) return lightGreen(Double.toString(d));
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
