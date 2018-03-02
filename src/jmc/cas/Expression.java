package jmc.cas;

import jmc.Function;

import java.util.ArrayList;

import static jmc.utils.ColorFormatter.*;
import static jmc.cas.Assets.*;

/**
 * Created by Jiachen on 19/05/2017.
 * Expression class that programmatically interprets mathematical expressions like "ln(x)^2/e*pi" into binary code.
 */
public class Expression extends Function {
    private Operable operable;

    public Expression(Operable operable) {
        this(operable, "");
    }


    public Expression(Operable operable, String name) {
        this(operable, name, false);
    }

    public Expression(Operable operable, String name, boolean dynamic) {
        super(name, dynamic);
        this.operable = operable;
    }

    /**
     * @param expression the string representation of an expression to be incorporated into JMC.
     * @return the Function instance derived from the String representation
     * @since May 19th. This method is the core of JMC. Took Jiachen tremendous effort. This system of
     * method represents his life's work
     */
    public static Expression interpret(String expression) {
        if (expression.equals("")) throw new IllegalArgumentException("cannot interpret an empty string");
        if (numOccurrence(expression, '(') != numOccurrence(expression, ')'))
            throw new IllegalArgumentException("incorrect format: '()' mismatch");
        if (numOccurrence(expression, '<') != numOccurrence(expression, '>'))
            throw new IllegalArgumentException("incorrect format: '<>' mismatch");
        expression = formatUnaryOperations(expression.replace(" ", "").replace("(-", "(0-"));
        String exp = formatCoefficients(expression);
        exp = handleParentheticalNotation(handleCalcPriority(exp));
        System.out.println(boldBlack("formatted input: ") + exp);
        ArrayList<Operable> components = new ArrayList<>();
        int hashId = 0;
        while (exp.indexOf(')') != -1) {
            int[] innerIndices = extractInnerParenthesis(exp, '(', ')');
            String extractedContent = exp.substring(innerIndices[0] + 1, innerIndices[1]);
            Operable formulated = generateOperations(extractedContent, components);
            components.add(formulated);
            String left = innerIndices[0] > 0 ? exp.substring(0, innerIndices[0]) : "";
            exp = left + "&" + hashId + exp.substring(innerIndices[1] + 1);
            System.out.println(lightCyan("func:\t") + colorMathSymbols(exp));
            hashId++;
        }
        Operable operable = generateOperations(exp, components);
        if (operable instanceof BinaryOperation) ((BinaryOperation) operable).setOmitParenthesis(true);
        String colored = colorMathSymbols(operable.toString());
        System.out.println(boldRed("output:\t") + colored);
        return new Expression(operable);
    }


    private static String formatUnaryOperations(String exp) {
        while (containsUnformattedUnaryOperations(exp)) {
            int idx1 = -1;
            for (Function function : UnaryOperation.registeredOperations()) {
                String candidate = function.getName() + "(";
                if (exp.contains(candidate)) {
                    idx1 = exp.indexOf(candidate) + candidate.length() - 1;
                    break;
                }
            }
            int idx2 = findMatchingIndex(exp, idx1, ')');
            exp = replaceAt(replaceAt(exp, idx1, '<'), idx2, '>');
        }
        return exp;
    }

    private static String replaceAt(String s, int idx, char c) {
        return s.substring(0, idx) + c + s.substring(idx + 1);
    }

    private static boolean containsUnformattedUnaryOperations(String exp) {
        for (Function function : UnaryOperation.registeredOperations()) {
            if (exp.contains(function.getName() + "("))
                return true;
        }
        return false;
    }

    public static String colorMathSymbols(String exp) {
        String colored = coloredLine("[36;1m", exp, "<", ">");
        colored = coloredLine("[1;m", colored, "+", "-", "*", "/", "^");
        colored = coloredLine("[1;34m", colored, ")", "(");
        colored = coloredLine("[34;1m", colored, "#");
        colored = coloredLine("[31;1m", colored, "&");
        colored = removeUnnecessaryDecimal(colored);
        return colored;
    }

    private static String removeUnnecessaryDecimal(String exp) {
        for (int i = 0; i < exp.length() - 1; i++) {
            String content = exp.substring(i, i + 2);
            if (content.equals(".0")) {
                if (i + 2 == exp.length()) {
                    exp = exp.substring(0, i);
                } else if (!DIGITS.contains(exp.substring(i + 2, i + 3))) {
                    String left = i == 0 ? "" : exp.substring(0, i);
                    exp = left + exp.substring(i + 2);
                }
            }
        }
        return exp;
    }


    /**
     * @param exp the expression to be analyzed.
     * @return the indices of the innermost opening parenthesis and the closing parenthesis
     * @since May 16th
     */
    public static int[] extractInnerParenthesis(String exp, char open, char close) {
        int closeIndex = exp.indexOf(close);
        int openIndex = exp.substring(0, closeIndex).lastIndexOf(open);
        return new int[]{openIndex, closeIndex};
    }

    private static ArrayList<Operable> clone(ArrayList<Operable> operables) {
        ArrayList<Operable> cloned = new ArrayList<>();
        for (Operable operable : operables) {
            cloned.add(operable);
        }
        return cloned;
    }


    private static Operable generateOperations(String segment, ArrayList<Operable> operables) {
        System.out.println(lightGreen("exp:\t") + colorMathSymbols(segment)); //skill learned May 16th, colored output!
        int operableHashId = 0;
        ArrayList<Operable> pendingOperations = new ArrayList<>();
        while (segment.indexOf('<') != -1) {
            int indices[] = extractInnerParenthesis(segment, '<', '>');
            String extracted = segment.substring(indices[0] + 1, indices[1]);
            Operable innerOperable = generateOperations(extracted, operables); //TODO DEBUG
            String unaryOperation = segment.substring(0, indices[0]);
            int startIndex = 0;
            for (int i = indices[0] - 1; i >= 0; i--) {
                char c = segment.charAt(i);
                if (SYMBOLS.contains(Character.toString(c))) {
                    unaryOperation = segment.substring(i + 1, indices[0]);
                    startIndex = i;
                    break;
                }
            }
            pendingOperations.add(new UnaryOperation(innerOperable, unaryOperation));
            String left = startIndex == 0 ? "" : segment.substring(0, startIndex + 1);
            System.out.println(lightBlue("unary:\t") + colorMathSymbols(segment));
            segment = left + "#" + operableHashId + segment.substring(indices[1] + 1);
            operableHashId++;
        }
        for (int p = 1; p <= 3; p++) { //prioritize ^ over */ over +-. This way it is flexible for adding more operations.
            for (int i = 0; i < segment.length(); i++) {
                CharSequence op = segment.subSequence(i, i + 1);
                if (BinaryOperation.binaryOperations(p).contains(op)) {
                    int[] indices = extractOperationIndices(segment, i);
                    String[] operandStrs = new String[]{segment.substring(indices[0], i), segment.substring(i + 1, indices[1] + 1)};
                    ArrayList<Operable> operands = getOperands(pendingOperations, operables, operandStrs);
                    Operation operation = new BinaryOperation(operands.get(0), op.toString(), operands.get(1));
                    pendingOperations.add(operation);
                    String left = indices[0] == 0 ? "" : segment.substring(0, indices[0]);
                    segment = left + "#" + operableHashId + segment.substring(indices[1] + 1);
                    System.out.println("->\t\t" + colorMathSymbols(segment));
                    i = 0;
                    operableHashId++;
                }
            }
        }
        if (pendingOperations.size() == 0) {
            return getOperands(pendingOperations, operables, new String[]{segment}).get(0);
        }
        return pendingOperations.get(pendingOperations.size() - 1);
    }

    private static ArrayList<Operable> getOperands(ArrayList<Operable> pendingOperations, ArrayList<Operable> operables, String[] operandStrs) {
        ArrayList<Operable> operands = new ArrayList<>();
        for (String string : operandStrs) {
            int componentIndex = string.indexOf("#");
            int bundleIndex = string.indexOf("&");
            if (componentIndex != -1) {
                int hashId = Integer.valueOf(string.substring(componentIndex + 1));
                operands.add(pendingOperations.get(hashId));
            } else if (bundleIndex != -1) {
                int bundleId = Integer.valueOf(string.substring(bundleIndex + 1));
                operands.add(operables.get(bundleId));
            } else {
                if (VARS.contains(string.toLowerCase())) {
                    operands.add(new Variable(string.toLowerCase()));
                } else if (Constants.contains(string)) {
                    operands.add(Constants.getConstant(string));
                } else {
                    if (string.equals("")) throw new RuntimeException("missing operand(s)");
                    try {
                        operands.add(new RawValue(Double.valueOf(string)));
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("undefined operand \"" + string + "\"");
                    }
                }
            }
        }
        return operands;
    }


    /**
     * TODO DEBUG
     *
     * @param segment the segment of expression
     * @param index   index of the binary operation
     * @return the indices of the beginning and the end of the binary operation, both inclusive
     */
    private static int[] extractOperationIndices(String segment, int index) {
        String leftHand = segment.substring(0, index);
        String rightHand = segment.substring(index + 1);
        int lastOccurrence = 0;
        for (int i = 0; i < leftHand.length(); i++) {
            CharSequence op = leftHand.subSequence(i, i + 1);
            if (BinaryOperation.binaryOperations().contains(op) || SYMBOLS.contains(op))
                lastOccurrence = i + 1;
        }

        int firstOccurrence = segment.length() - 1;// debugged May 16th
        for (int i = 0; i < rightHand.length(); i++) {
            CharSequence op = rightHand.subSequence(i, i + 1);
            if (BinaryOperation.binaryOperations().contains(op) || SYMBOLS.contains(op)) {
                firstOccurrence = index + i;
                break;
            }
        }
        //System.out.println("single statement indices: " + lastOccurrence + " " + firstOccurrence);// debug completed May 16th
        return new int[]{lastOccurrence, firstOccurrence};
    }


    /**
     * @param s String that is going to be indexed for occurrence of char c
     * @param c char c for num of occurrence.
     * @return the number of occurrence of char c in String s.
     */
    private static int numOccurrence(String s, char c) {
        int count = 0;
        while (s.indexOf(c) != -1) {
            s = s.substring(s.indexOf(c) + 1);
            count++;
        }
        return count;
    }

    /**
     * Formats the raw String to prepare it for the formulation into a Function instance.
     * For instance, the call to formatCoefficients("x+2x^2+3x+4") would return "x+2*x^2+3*x+4"
     *
     * @param exp the expression to be formatted
     * @return the formatted expression.
     * @since May 16th: also handles the special unary operator that is the same as "-", so
     * "-6" would become "(0-6)"
     */
    private static String formatCoefficients(String exp) {
        for (int i = 0; i < exp.length() - 1; i++) {
            char digit = exp.charAt(i), x = exp.charAt(i + 1);
            if (DIGITS.contains(Character.toString(digit)) || digit == ')') {
                if (VARS.contains(Character.toString(x)) || Constants.startsWidthConstant(exp.substring(i + 1))) {
                    exp = exp.replace(digit + "" + x, digit + "*" + x);
                }
            }
        }
        if (exp.charAt(0) == '-') exp = "0" + exp;
        for (int i = 1; i < exp.length() - 1; i++) {
            char subtract = exp.charAt(i), symbol = exp.charAt(i - 1);
            if (subtract == '-' && "(*/^<".contains(Character.toString(symbol))) {
                int[] indices = extractOperationIndices(exp, i);
                String extracted = exp.substring(i, indices[1] + 1);
                exp = exp.replace(symbol + "" + extracted, symbol + "(0" + extracted + ")");
            }
        }
        return exp;
    }

    private static String handleCalcPriority(String exp) {
        for (int i = 0; i < exp.length() - 2; i++) {
            if (exp.charAt(i) == '<') {
                int q = findMatchingIndex(exp, i, '>');
                if (exp.charAt(i + 1) == '(' && findMatchingIndex(exp, i + 1, ')') == q - 1)
                    continue;
                String l = exp.substring(0, i + 1);
                String middle = exp.substring(i + 1, q);
                if (VARS.contains(middle))
                    continue;
                middle = '(' + middle + ')';
                exp = l + middle + exp.substring(q);
            }
        }
        return exp;
    }

    /**
     * findMatchingIndex("(56+(34+2))*(32+(13+34+2))",4,')') would return 9
     * findMatchingIndex("(56+(34+2))*(32+(13+34+2))",0,')') would return 10
     *
     * @param exp        the expression to be inspected for a matched pair of closure.
     * @param init       the initial index, i.e beginning of the designated closure
     * @param lookingFor the char that signifies the termination of a stack of the closure.
     * @return the matching index in which the closure correspond to init terminates.
     * @since May 19th most efficient method I've ever wrote.
     */
    public static int findMatchingIndex(String exp, int init, char lookingFor) {
        char start = exp.charAt(init);
        for (int i = init + 1; i < exp.length(); i++) {
            char cur = exp.charAt(i);
            if (cur == start) i = findMatchingIndex(exp, i, lookingFor);
            else if (cur == lookingFor) return i;
        }
        return -1;
    }

    private static String handleParentheticalNotation(String exp) {
        String non_operators = ")>" + LETTERS;
        for (int i = 1; i < exp.length(); i++) {
            char cur = exp.charAt(i);
            if (cur == '(') {
                String prev = Character.toString(exp.charAt(i - 1));
                if (DIGITS.contains(prev) || non_operators.contains(prev)) {
                    exp = exp.replace(prev + "" + cur, prev + "*" + cur);
                }
            }
        }
        return exp;
    }

    @Override
    public double eval(double val) {
        return operable.eval(val);
    }

    public Operable getOperable() {
        return operable;
    }

    public void setOperable(Operable operable) {
        this.operable = operable;
    }
}
