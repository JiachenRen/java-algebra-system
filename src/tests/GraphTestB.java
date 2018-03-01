package tests;

import jmc.Function;
import jmc.Graph;
import jui.HSlider;
import jui.JNode;
import jui.Table;
import jui.VBox;
import processing.core.PApplet;

/**
 * Created by Jiachen on 06/05/2017.
 */
public class GraphTestB extends PApplet {
    public static void main(String args[]) {
        System.out.println("VSlider Driver Testing");
        String sketch = Thread.currentThread().getStackTrace()[1].getClassName();
        Thread proc = new Thread(() -> PApplet.main(sketch));
        proc.start();
    }

    public void settings() {
        size(800, 600, FX2D);
        pixelDensity(2);
    }

    public void setup() {
        JNode.init(this);
        VBox parent = new VBox(0, 0, width, height);
        parent.setId("parent");
        JNode.add(parent);

        Table table = new Table(1.0f, 0.9f);
        table.setId("parent");
        parent.add(table);

        for (int r = 0; r < table.getRows(); r++) {
            for (int c = 0; c < table.getColumns(); c++) {
                int which = (int)(Math.random()*4);
                Graph graph = new Graph().setId("graph");
                graph.add(new Function("f1") {
                    @Override
                    public double eval(double val) {
                        return Math.cos(Math.pow(val, 3) * 2) * randomAssign(val,which);
                    }
                });
                table.set(r, c, graph);
                PApplet.println(graph.getId());
            }
        }


        HSlider control1 = new HSlider();
        control1.setId("control1");
        control1.setScalingFactor(0.5f);
        control1.setRange(-10f, 10f);
        control1.setValue(0);
        parent.add(control1);
        control1.onFocus(() -> {
            JNode.search("graph").forEach(graph -> {
                ((Graph) graph).override("f1", new Function() {
                    @Override
                    public double eval(double val) {
                        return Math.cos(Math.pow(val, 3) * control1.getFloatValue()) * randomAssign(val,2);
                    }
                }.setDynamic(false).setGraphStyle(Function.Style.CONTINUOUS));
            });
        });
    }

    public void draw() {
        background(255);
        JNode.run();
    }

    private double randomAssign(double val, int which) {
        switch (which) {
            case 0:
                return Math.log(val);
            case 1:
                return Math.sin(val) * val;
            case 2:
                return Math.pow(Math.tan(val), 2);
            case 3:
                return Math.asin(val * 2);
        }
        return 0;
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
