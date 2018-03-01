package jui;

/**
 * Style Control for background/contour/text color
 */
public enum Mode {
    CONSTANT(0), VOLATILE(1), DISABLED(2);
    private int val;

    Mode(int i) {
        val = i;
    }

    public int getValue() {
        return val;
    }
}
