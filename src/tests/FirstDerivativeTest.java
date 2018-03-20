package tests;

import jmc.cas.Compiler;
import jmc.cas.Mode;

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
//        l(Compiler.compile("derivative(x*x^2,x)").simplify());
//        l(Compiler.compile("derivative(x*x^a,x)").simplify());
//        l(Compiler.compile("derivative(x*x^a+b*x^2,x)").simplify());
//        l(Compiler.compile("derivative(x^x,x)").simplify());
//        l(Compiler.compile("derivative(x,x)").simplify());
//        l(Compiler.compile("derivative(a,x)").simplify());
//        l(Compiler.compile("derivative(3,x)").simplify());
//        l(Compiler.compile("derivative(ln(x),x)").simplify());
//        l(Compiler.compile("derivative(x^(-1),x)").simplify());
//        l(Compiler.compile("derivative(ln(2x),x)").simplify());
//        l(Compiler.compile("derivative(2x*ln(x),x)").simplify());
//        l(Compiler.compile("derivative(x^(2x+x),x)").simplify()); //fixed

        //ultimate test -> if this passes, I am just !!!!!!!!
//        Mode.DEBUG = true;
        l(Compiler.compile("x*a*(1-b)^2").simplify()); //problematic
//        l(Compiler.compile("-1*-1*-1").simplify());
//        l(Compiler.compile("derivative(-ln(10)*x^3*ln(x)/(ln(cos(x))-ln(10)*x),x)").simplify()); // problematic

    }


}
