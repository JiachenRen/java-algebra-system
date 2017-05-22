package jui_lib;

/**
 * Style Control for background/contour/text color
 */
public enum JStyle {
    CONSTANT(0), VOLATILE(1), DISABLED(2);
    private int val;

    JStyle(int i) {
        val = i;
    }

    public int getValue() {
        return val;
    }
}
