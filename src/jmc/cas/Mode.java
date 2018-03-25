package jmc.cas;

import static jmc.utils.AnsiColor.*;

/**
 * Created by Jiachen on 3/4/18.
 * Mode: radians vs. degrees, debug on/off, etc.
 */
public class Mode {
    public static boolean DEBUG = false;
    public static boolean FRACTION = true;
    public static boolean COMPACT = true;
    public static String U_OP_COLOR = LIGHT_BLUE.toString();
    public static String BIN_OP_COLOR = BLACK.toString();
    public static String CUSTOM_OP_COLOR = GREEN.toString();
    public static String CONSTANT_COLOR = LIGHT_CYAN.toString();
    public static String CURLY_BRACKET_COLOR = BROWN.toString();
    public static String COMMA_COLOR = YELLOW.toString();
    public static String PARENTHESIS_COLOR = LIGHT_RED.toString();
    public static String LITERAL_COLOR = LIGHT_GREEN.toString();
    public static String NUMBER_COLOR = GREEN.toString();
    public static String FRACTION_COLOR = GREEN.toString();
    public static String VARIABLE_COLOR = BOLD_BLACK.toString();
    public static String DECIMAL_FORMAT = "0.##################"; //needs improvement!
}
