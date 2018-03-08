package tests;

import jmc.cas.RawValue;

/**
 * Created by Jiachen on 3/7/18.
 * RawValue Test
 */
public class RawValueTest {
    public static void main(String args[]) {
        l(RawValue.ZERO, RawValue.ONE, RawValue.UNDEF);
        l(new RawValue(Double.NEGATIVE_INFINITY).isInfinite());
        l(RawValue.ZERO.isInteger());
        l(-RawValue.INFINITY.doubleValue() < 0);
    }

    private static void l(Object... objects) {
        for (Object o : objects) {
            l(o);
        }
    }

    private static void l(Object o) {
        System.out.println(o);
    }
}
