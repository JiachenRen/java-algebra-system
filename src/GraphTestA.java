import jmc_lib.Function;
import jmc_lib.Graph;
import jui_lib.HSlider;
import jui_lib.JNode;
import jui_lib.VBox;
import processing.core.PApplet;

/**
 * Created by Jiachen on 06/05/2017.
 */
public class GraphTestA extends PApplet {
    public static void main(String args[]) {
        System.out.println("VSlider Driver Testing");
        String sketch = Thread.currentThread().getStackTrace()[1].getClassName();
        Thread proc = new Thread(() -> PApplet.main(sketch));
        proc.start();
    }

    public void settings() {
        size(800, 600, FX2D);
    }

    public void setup() {
        JNode.init(this);
        VBox parent = new VBox(0, 0, width, height);
        parent.setId("parent");


        Graph graph = new Graph(1.0f, 0.9f);
        graph.setId("graph");
        parent.add(graph);

        HSlider control1 = new HSlider();
        control1.setId("control1");
        control1.setScalingFactor(0.5f);
        control1.setRange(0.5f, 1.5f);
        control1.setValue(1);
        /*
        control1.attachMethod(() -> {
            control1.setValue(2.5f + cos(frameCount / 1000.0f) * 2.5f);
            graph.override("f1", new Function() {
                @Override
                public double eval(double val) {
                    return Math.tan(Math.pow(control1.getFloatValue(), val));
                }
            });
        });
        */
        parent.add(control1);

        HSlider control2 = new HSlider();
        control2.setId("control2");
        control2.setScalingFactor(0.5f);
        control2.setRange(0.5f, 1.5f);
        control2.setValue(1);
        parent.add(control2);

        control2.onFocus(() -> {
            graph.override("f1", new Function() {
                @Override
                public double eval(double val) {
                    return Math.tan(val) * Math.cos(control1.getFloatValue() * val) * Math.sin(control2.getFloatValue() * val);
                }
            });
        });
        control1.onFocus(() -> {
            graph.override("f1", new Function() {
                @Override
                public double eval(double val) {
                    return Math.tan(val) * Math.cos(control1.getFloatValue() * val) * Math.sin(control2.getFloatValue() * val);
                }
            });
        });

        JNode.add(parent);

        graph.add(new Function("f1") {
            @Override
            public double eval(double val) {
                return (val + 3) * (val - 2) * (val * val + 7) / ((val + 2) * (val - 2));
            }
        });

        graph.setWindowY(-100, 100);
        graph.setWindowX(-100, 100);

        System.out.println(graph.getFunction("f1").getPlot());
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
