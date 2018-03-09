package jmc.utils;

import jui.JNode;

/**
 * Created by Jiachen on 3/1/18.
 * ColorFormatter class whose job is to color the String for the system outputs.
 */
public class ColorFormatter {
    public static String boldBlack(Object o) {
        return color(o.toString(), "[1m");
    }

    public static String lightRed(Object o) {
        return color(o.toString(), AnsiColor.LIGHT_RED.toString());
    }

    public static String lightCyan(Object o) {
        return color(o.toString(), AnsiColor.LIGHT_CYAN.toString());
    }

    public static String lightGreen(Object o) {
        return color(o.toString(), AnsiColor.LIGHT_GREEN.toString());
    }

    public static String lightBlue(Object o) {
        return color(o.toString(), AnsiColor.LIGHT_BLUE.toString());
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
        if (JNode.OS.toLowerCase().contains("windows"))
            return line;
        for (String symbol : symbols) {
            line = line.replace(symbol, (char) 27 + modifier + symbol + (char) 27 + "[0m");
        }
        return line;
    }

    public static String color(String s, String modifier) {
        if (JNode.OS.toLowerCase().contains("windows")) return s;
        return (char) 27 + modifier + s + (char) 27 + "[0m";
    }
}
