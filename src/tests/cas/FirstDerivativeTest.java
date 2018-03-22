package tests.cas;

import static jmc.cas.Compiler.compile;
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
//        l(compile("derivative(x*x^2,x)").simplify());
//        l(compile("derivative(x*x^a,x)").simplify());
//        l(compile("derivative(x*x^a+b*x^2,x)").simplify());
//        l(compile("derivative(x^x,x)").simplify());
//        l(compile("derivative(x,x)").simplify());
//        l(compile("derivative(a,x)").simplify());
//        l(compile("derivative(3,x)").simplify());
//        l(compile("derivative(ln(x),x)").simplify());
//        l(compile("derivative(x^(-1),x)").simplify());
//        l(compile("derivative(ln(2x),x)").simplify());
//        l(compile("derivative(2x*ln(x),x)").simplify());
//        l(compile("derivative(x^(2x+x),x)").simplify()); //fixed

        //ultimate test -> if this passes, I am just !!!!!!!!
//        Mode.DEBUG = true;
//        l(compile("x*a*(1-b)^2").simplify()); //problematic
//        l(compile("-1*-1*-1").simplify());
//        Operable superLongExp = compile("derivative(-ln(10)*x^3*ln(x)/(ln(cos(x))-ln(10)*x),x)");
//        l(boldBlack("original: " + superLongExp));
//        superLongExp = superLongExp.simplify();
//        l(lightBlue("simplified(taken first derivative): "+superLongExp));
//        superLongExp = superLongExp.expand();
//        l(lightBlue("expanded: "+superLongExp));
//        superLongExp = superLongExp.expand();
//        l(lightGreen("evaluated at 6 (checked with Ti-Nspire CAS): "+superLongExp.eval(6)));
//        l(compile("(1+2*<3)/4>").firstDerivative(new Variable("x")).expand().simplify().beautify());
//        l(compile("(1+2*<3)/4>"));
//        l(compile("(a+b+c)*(c+a+b)").simplify());
//        l(compile("x^2*ln(x)").derivative(new Variable("x"), 2).expand().simplify());
//        l(compile("2*ln(x)+2*1+1").simplify());
//        l(compile("derivative(x*cos(x/3)*sin(x/3)/3+ln(x)*x,x,10)").exec()); //wow, check this out...
        l(compile("derivative(cos(x)*sin(x)*x*ln(x),x,10)").exec());
//        l(compile("ln(x)^(-2)/x").simplify().beautify());
//        l(compile("(a+b+c)^2").expand());

    }


}
