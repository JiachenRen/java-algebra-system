package tests;

import jmc.GraphFunction;
import jmc.cas.Expression;
import jmc.graph.Graph;
import jui.JNode;
import processing.core.PApplet;

/**
 * Created by Jiachen on 21/05/2017.
 * Graph UI Test
 */
public class GraphTest extends PApplet {
    public static void main(String args[]) {
        String sketch = Thread.currentThread().getStackTrace()[1].getClassName();
        Thread main = new Thread(() -> PApplet.main(sketch));
        main.start();
    }

    public void settings() {
        size(800, 600, FX2D);
    }

    public void setup() {
        JNode.init(this);
        JNode.add(new Graph(0, 0, width, height));
        Graph graph = ((Graph) JNode.getDisplayables().get(0));
        graph.add(new GraphFunction(Expression.interpret("x")));

    }

    public void draw() {
        background(255, 255, 255);
        JNode.run();
    }

    public void keyPressed() {
        JNode.keyPressed();
    }

    public void keyReleased() {
        JNode.keyReleased();
    }
}
