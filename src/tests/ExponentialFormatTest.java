package tests;

import jmc_lib.*;
import jui_lib.*;
import processing.core.PApplet;

/**
 * Created by Jiachen on 16/05/2017.
 */
public class ExponentialFormatTest extends PApplet {
    public static void main(String args[]) {
        System.out.println("Function Interpretation Test May 16th");
        String sketch = Thread.currentThread().getStackTrace()[1].getClassName();
        Thread proc = new Thread(() -> PApplet.main(sketch));
        proc.start();
    }

    public void settings() {
        size(400, 300, FX2D);
        pixelDensity(2);
    }

    public void setup() {
        JNode.init(this);
        Operable operable = Function.interpret("x/(x-1)/(x+1/(x-1))").getOperable();
        ((Operation) operable).toExponentialForm();
        System.out.println(Function.formatColorMathSymbols(operable.toString()));
    }

    public void draw() {
        background(255);
        JNode.run();
    }

    public void keyPressed() {
        JNode.keyPressed();
    }

    public void keyReleased() {
        JNode.keyReleased();
    }

    public void mouseWheel() {
        //to be implemented. Jan 27th.
    }
}
