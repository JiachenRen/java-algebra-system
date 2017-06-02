package jmc_lib;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

/**
 * Function class,
 */
public abstract class Function implements Evaluable {
    private static final ScriptEngine SCRIPT_ENGINE;
    private static final String DIGITS;
    private static final String SYMBOLS;
    private static final String VARS;
    private static final String LETTERS;
    private String name;
    private boolean isVisible;
    private boolean asymptoteVisible;
    private boolean tangentLineVisible;
    private boolean tracingEnabled;
    private Plot plot;
    private Style graphStyle;
    private boolean dynamic;
    private float strokeWeight;
    private int color;
    private boolean matchAuxiliaryLinesColor;
    private boolean autoAsymptoteExtension;

    public enum Style {
        CONTINUOUS, DISCRETE
    }

    //Initialization of Apple Script SCRIPT_ENGINE
    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        SCRIPT_ENGINE = mgr.getEngineByName("AppleScript");
        DIGITS = "0123456789.";
        SYMBOLS = "()+-*/^<>";
        //noinspection SpellCheckingInspection
        VARS = "abcdfghjklmnopqrstuvwxyz";
        //noinspection SpellCheckingInspection
        LETTERS = "abcdefghijklmnopqrstuvwxyz";
    }

    {
        isVisible = true;
        graphStyle = Style.CONTINUOUS;
        strokeWeight = 1;
        setMatchAuxiliaryLinesColor(false);
        setTracingEnabled(true);
        setAutoAsymptoteExtension(true);
    }

    /**
     * Default anonymous constructor
     */
    public Function() {
        this("");
    }

    public Function(String name) {
        this(name, false);
    }

    public Function(String name, boolean dynamic) {
        setName(name);
        setDynamic(dynamic);
    }

    public static Function implement(String name, Evaluable evaluable) {
        return implement(evaluable).setName(name);
    }

    public static Function implement(Evaluable evaluable) {
        return new Function() {
            @Override
            public double eval(double val) {
                return evaluable.eval(val);
            }
        };
    }

    /**
     * @param expression the string representation of an expression to be incorporated into JMC.
     * @return the Function instance derived from the String representation
     * @since May 19th. This method is the core of JMC. Took Jiachen tremendous effort. This system of
     * method represents his life's work
     */
    public static InterpretedFunction interpret(String expression) {
        if (expression.equals("")) throw new IllegalArgumentException("cannot interpret an empty string");
        if (numOccurrence(expression, '(') != numOccurrence(expression, ')'))
            throw new IllegalArgumentException("incorrect format: '()' mismatch");
        if (numOccurrence(expression, '<') != numOccurrence(expression, '>'))
            throw new IllegalArgumentException("incorrect format: '<>' mismatch");
        expression = expression.replace(" ", "");
        String exp = Function.formatCoefficients(expression);
        exp = handleParentheticalNotation(Function.handleCalcPriority(exp));
        System.out.println((char) 27 + "[1m" + "formatted input: " + (char) 27 + "[0m" + exp);
        ArrayList<Operable> components = new ArrayList<>();
        int hashId = 0;
        while (exp.indexOf(')') != -1) {
            int[] innerIndices = extractInnerParenthesis(exp, '(', ')');
            String extractedContent = exp.substring(innerIndices[0] + 1, innerIndices[1]);
            Operable formulated = generateOperations(extractedContent, components);
            components.add(formulated);
            String left = innerIndices[0] > 0 ? exp.substring(0, innerIndices[0]) : "";
            exp = left + "&" + hashId + exp.substring(innerIndices[1] + 1);
            System.out.println((char) 27 + "[1;36m" + "func:\t" + (char) 27 + "[0m" + formatColorMathSymbols(exp));
            hashId++;
        }
        Operable operable = generateOperations(exp, components);
        if (operable instanceof BinaryOperation) ((BinaryOperation) operable).setOmitParenthesis(true);
        String colored = formatColorMathSymbols(operable.toString());
        System.out.println((char) 27 + "[31;1m" + "output:\t" + (char) 27 + "[0m" + colored);
        return new InterpretedFunction(operable);
    }

    public static String formatColorMathSymbols(String exp) {
        //String colored = coloredLine("[35;1m", exp, "x");
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
        System.out.println((char) 27 + "[32;1m" + "exp:\t" + (char) 27 + "[0m" + formatColorMathSymbols(segment)); //skill learned May 16th, colored output!
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
            System.out.println((char) 27 + "[34;1m" + "unary:\t" + (char) 27 + "[0m" + formatColorMathSymbols(segment));
            segment = left + "#" + operableHashId + segment.substring(indices[1] + 1);
            operableHashId++;
        }
        for (int p = 1; p <= 3; p++) { //prioritize ^ over */ over +-. This way it is flexible for adding more operations.
            for (int i = 0; i < segment.length(); i++) {
                CharSequence op = segment.subSequence(i, i + 1);
                if (BinaryOperation.binaryOperations(p).contains(op)) {
                    int[] indices = Function.extractOperationIndices(segment, i);
                    String[] operandStrs = new String[]{segment.substring(indices[0], i), segment.substring(i + 1, indices[1] + 1)};
                    ArrayList<Operable> operands = Function.getOperands(pendingOperations, operables, operandStrs);
                    Operation operation = new BinaryOperation(operands.get(0), op.toString(), operands.get(1));
                    pendingOperations.add(operation);
                    String left = indices[0] == 0 ? "" : segment.substring(0, indices[0]);
                    segment = left + "#" + operableHashId + segment.substring(indices[1] + 1);
                    System.out.println("->\t\t" + formatColorMathSymbols(segment));
                    i = 0;
                    operableHashId++;
                }
            }
        }
        if (pendingOperations.size() == 0) {
            return Function.getOperands(pendingOperations, operables, new String[]{segment}).get(0);
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
                        operands.add(new Raw(Double.valueOf(string)));
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("undefined operand \"" + string + "\"");
                    }
                }
            }
        }
        return operands;
    }

    /**
     * returns a colored line!
     *
     * @param modifier a sequence of escape characters for modifying the color& font.
     * @param line     the line to be colored
     * @param symbols  the symbols to be replaced with color
     * @return a beautifully formatted&colorful line for printing!
     * @since May 16th
     */
    public static String coloredLine(String modifier, String line, String... symbols) {
        for (String symbol : symbols) {
            line = line.replace(symbol, (char) 27 + modifier + symbol + (char) 27 + "[0m");
        }
        return line;
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
     * Reconstruct Function instance with Apple Script. Efficient, but only compatible with Mac.
     *
     * @param exp the expression to be converted to a sensible Function stance with AppleScript.
     * @return the Function instance constructed with Apple's scripting SCRIPT_ENGINE.
     */
    public static Function evalOsaScript(String exp) {
        final String expression = Function.formatCoefficients(exp);
        return new Function() {
            @Override
            public double eval(double val) {
                String script = Function.formatExpForOsa(expression, val);
                try {
                    return new Double(SCRIPT_ENGINE.eval(script).toString());
                } catch (ScriptException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    throw new UnsupportedOperationException("osa script only supported on Macintosh OS");
                }
                return 0;
            }
        };
    }

    private static String formatExpForOsa(String expression, double val) {
        return expression.toLowerCase().replace("x", Double.toString(val));
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
        String non_operators = ")>"+LETTERS;
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

    /**
     * This is an abstract method to be defined by the anonymous subclass of Function; the definition
     * of the subclass fro this method should properly define how the function is going to be
     * evaluated.
     *
     * @param val the value that is going to be plugged into this Function for evaluation
     * @return the result gained from the evaluation with val and this Function instance's definition.
     */
    public abstract double eval(double val);

    /**
     * Calculates and stores a plot according to a specific Range
     *
     * @param rangeX the range in which a plot is going to be generated
     */
    public void updatePlot(Range rangeX, Range rangeY, float graphHeight) {
        plot = createPlot(rangeX, rangeY, graphHeight);
    }

    /**
     * TODO automatic graphic plotting density enhancement
     *
     * @param rangeX      the domain in which the graph is going to get graphed.
     * @param rangeY      the range in which the graph is going to get graphed.
     * @param graphHeight the height of the graph to be plotted.
     * @return the plotted graph plot according to the range.
     * @since May 7th, debugged updateStepVal().
     */
    public Plot createPlot(Range rangeX, Range rangeY, float graphHeight) {
        Plot plot = new Plot(rangeX);
        Range copied = new Range(rangeX);
        Point prevPoint = null;
        while (copied.hasNextStep()) {
            double temp = copied.getCurStep();
            Point curPoint = new Point(temp, this.eval(temp));
            if (prevPoint != null && (rangeY.isInScope(curPoint.getY()) ^ rangeY.isInScope(prevPoint.getY()))) {
                double diff_y = Math.abs(curPoint.getY() - prevPoint.getY());
                double pixels_y = Plot.map(Math.abs(diff_y), 0, rangeY.getSpan(), 0, graphHeight);
                if (pixels_y > 5 && graphStyle.equals(Style.CONTINUOUS)) {
                    double step = rangeX.getStep() / Math.abs(pixels_y);
                    Range newRangeX = new Range(prevPoint.getX() + step, curPoint.getX() - step, step);
                    while (newRangeX.hasNextStep()) {
                        double val = newRangeX.getCurStep();
                        double evaluated = eval(val);
                        Point newPoint = new Point(val, evaluated);
                        plot.add(newPoint);// TODO May 8th
                        newRangeX.next();
                    }
                }
            }
            prevPoint = curPoint;
            plot.add(curPoint);
            copied.next();
        }
        plot.sort(); //a computational expensive solution to the bug, yet it worked! TODO improve it
        plot.insertVerticalAsymptote(rangeY, this);
        return plot;
    }

    /**
     * Returns the calculated plot of a specific range according to the definition of this Function.
     */
    public Plot getPlot() {
        return plot;
    }


    public String getName() {
        return name;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Style getStyle() {
        return graphStyle;
    }

    public Function setGraphStyle(Style style) {
        this.graphStyle = style;
        return this;
    }

    public Function setName(String name) {
        this.name = name;
        return this;
    }

    public Function setDynamic(boolean temp) {
        this.dynamic = temp;
        return this;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy) {
        return numericalSolve(y, lowerBound, upperBound, accuracy, 1000);
    }

    public ArrayList<Double> numericalSolve(double y, double lowerBound, double upperBound, double accuracy, int steps) {
        if (Math.abs(upperBound - lowerBound) <= accuracy) {
            ArrayList<Double> results = new ArrayList<>();
            double solution = (lowerBound + upperBound) / 2;
            results.add(solution);
            return results;
        }
        ArrayList<Double> solutions = new ArrayList<>();
        double stepVal = (upperBound - lowerBound) / steps;
        boolean isAbove = this.eval(lowerBound) > y;
        for (double i = lowerBound + stepVal; i <= upperBound; i += stepVal) {
            double cur = this.eval(i);
            if (cur > y ^ isAbove) {
                isAbove = cur > y;
                solutions.addAll(numericalSolve(y, i - stepVal, i, accuracy, steps));
            }
        }
        return solutions;
    }

    public float getStrokeWeight() {
        return strokeWeight;
    }

    public Function setStrokeWeight(float strokeWeight) {
        this.strokeWeight = strokeWeight;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Function setColor(int color) {
        this.color = color;
        return this;
    }

    public Function inheritStyle(Function other) {
        this.strokeWeight = other.strokeWeight;
        this.color = other.color;
        this.asymptoteVisible = other.asymptoteVisible;
        this.tangentLineVisible = other.tangentLineVisible;
        this.autoAsymptoteExtension = other.autoAsymptoteExtension;
        this.setGraphStyle(other.getStyle());
        this.setDynamic(other.dynamic);
        return this;
    }

    public boolean isAsymptoteVisible() {
        return asymptoteVisible;
    }

    public Function setAsymptoteVisible(boolean asymptoteVisible) {
        this.asymptoteVisible = asymptoteVisible;
        return this;
    }

    public boolean isTangentLineVisible() {
        return tangentLineVisible;
    }

    public void setTangentLineVisible(boolean tangentLineVisible) {
        this.tangentLineVisible = tangentLineVisible;
    }

    /**
     * TODO debug, java doc, not yet functional!
     *
     * @param x
     * @param y
     * @param allowed_diff
     * @param attempts
     * @return
     */
    public boolean containsPoint(double x, double y, double allowed_diff, int attempts) {
        double step = allowed_diff / attempts;
        Point org_point = new Point(x, y);
        for (double i = x - allowed_diff; i <= x + allowed_diff; i += step) {
            Point cur_point = new Point(i, this.eval(i));
            if (Point.dist(org_point, cur_point) <= allowed_diff)
                return true;
        }
        for (double i = y - allowed_diff; i <= y + allowed_diff; y += step) {
            ArrayList<Double> extrapolated = this.numericalSolve(i, x - allowed_diff, x + allowed_diff, allowed_diff, attempts);
            if (extrapolated.size() == 0) continue;
            Point cur_point = new Point(extrapolated.get(0), i);
            if (Point.dist(org_point, cur_point) <= allowed_diff)
                return true;
        }
        return false;
    }

    public boolean isAutoAsymptoteExtension() {
        return autoAsymptoteExtension;
    }

    public void setAutoAsymptoteExtension(boolean autoAsymptoteExtension) {
        this.autoAsymptoteExtension = autoAsymptoteExtension;
    }

    public boolean tracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public boolean isMatchAuxiliaryLinesColor() {
        return matchAuxiliaryLinesColor;
    }

    public void setMatchAuxiliaryLinesColor(boolean matchAuxiliaryLinesColor) {
        this.matchAuxiliaryLinesColor = matchAuxiliaryLinesColor;
    }

    public boolean equals(Function other){
        return this.getName().equals(other.getName());
    }
}
