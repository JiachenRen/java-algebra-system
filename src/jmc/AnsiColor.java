package jmc;

/**
 * Created by Jiachen on 3/2/18.
 * An enum encapsulating String representations of Ansi colors.
 */
public enum AnsiColor {
    BLACK("[0;30m"),
    BLUE("[0;34m"),
    GREEN("[0;32"),
    CYAN("[0;36"),
    RED("[0;31"),
    PURPLE("[0;35"),
    BROWN("[0;33"),
    GRAY("[0;37"),
    DARK_GRAY("[1;30"),
    LIGHT_BLUE("[1;34"),
    LIGHT_GREEN("[1;32"),
    LIGHT_CYAN("[1;36"),
    LIGHT_RED("[1;31"),
    LIGHT_PURPLE("[1;35"),
    YELLOW("[1;33"),
    WHITE("[1;37");
    private String raw;

    AnsiColor(String raw) {
        this.raw = raw;
    }

    public String toString() {
        return this.raw;
    }
}
