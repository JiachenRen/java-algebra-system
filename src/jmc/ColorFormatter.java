package jmc;

/**
 * Created by Jiachen on 3/1/18.
 * ColorFormatter class whose job is to color the String for the system outputs.
 */
public class ColorFormatter {
    public static String boldBlack(String s) {
        return color(s, "[1m");
    }

    public static String boldRed(String s) {
        return color(s, "[31;1m");
    }

    public static String lightCyan(String s) {
        return color(s, AnsiColor.Light_Cyan.toString());
    }

    public static String lightGreen(String s) {
        return color(s, AnsiColor.Light_Green.toString());
    }

    public static String lightBlue(String s) {
        return color(s, AnsiColor.Light_Blue.toString());
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

    public static String color(String s, String modifier) {
        return (char) 27 + modifier + s + (char) 27 + "[0m";
    }
}
