package tests;

/**
 * Created by Jiachen on 3/7/18.
 * Test printing methods.
 */
public class TestPrint {
    public static void l(Object... objects) {
        for (Object o : objects) {
            l(o);
        }
    }

    public static void l(Object o) {
        System.out.println(o);
    }
}
