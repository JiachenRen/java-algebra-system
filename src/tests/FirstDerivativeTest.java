package tests;

import jmc.cas.Compiler;

import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/17/18.
 * First Derivative Test
 */
public class FirstDerivativeTest {
    public static void main(String args[]) {
//        ArrayList<String> lines = new ArrayList<>();
//        Collections.addAll(lines, Utils.read("/tests/files/u_der.txt").split("\n"));
//        ArrayList<Operable> derivatives = lines.stream()
//                .map(Compiler::compile)
//                .map(o -> o.firstDerivative(new Variable("x")).simplify())
//                .collect(Collectors.toCollection(ArrayList::new));
//        for (int i = 0; i < derivatives.size(); i++) {
//            Operable derivative = derivatives.get(i);
//            l(lines.get(i) + " -> " + derivative);
//        }
        l(Compiler.compile("derivative(x*x^2,x)").simplify());
        l(Compiler.compile("derivative(x*x^a,x)").simplify());
    }


}
