import jmc_lib.*;
import jui_lib.*;
import processing.core.PApplet;

/**
 * Created by Jiachen on 16/05/2017.
 */
/*
public class FunctionInterpretationTest extends PApplet {
    public static void main(String args[]) {
        System.out.println("Function Interpretation Test May 16th");
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
        JNode.DISPLAY_CONTOUR = true;
        JNode.BACKGROUND_COLOR = color(255);
        JNode.ROUNDED = false;
        JNode.COLOR_MODE = HSB;

        HBox parent = new HBox(0, 0, width, height);
        parent.setId("parent");
        parent.setMargins(0, 0);
        parent.matchWindowDimension(true);

        VBox graphWrapper = new VBox(0.9f, 1.0f);
        graphWrapper.setId("graphWrapper");
        parent.add(graphWrapper);

        Graph graph = new Graph(1.0f, 0.9f);
        graph.setId("graph");
        graphWrapper.add(graph);

        Function test1 = Function.interpret("((sin<x>*(x+3.0))/((cos<x>*tan<x>)-x))*x").setName("test1").setColor(color(0, 150, 50));
        test1.setAsymptoteVisible(true);
        test1.setTangentLineVisible(true);
        graph.add(test1);

        HBox func1 = new HBox();
        func1.setMargins(0, 0);
        func1.add(new Label().setContent("f(x)=").setRelativeW(0.13f));
        func1.add(new TextInput().onSubmit(() -> {
            TextInput temp = JNode.getTextInputById("f(x)");
            try {
                if (temp != null) {
                    graph.override("test1", Function.interpret(temp.getStaticContent()));
                    InterpretedFunction function = ((InterpretedFunction) graph.getFunction("test1"));
                    Operable extracted = function.getOperable();
                    if (extracted instanceof Operation) ((Operation) extracted).toExponentialForm();
                    temp.setStaticContent(extracted.toString());
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }).setStaticContent("((sin<x>*(x+3.0))/((cos<x>*tan<x>)-x))*x").setId("f(x)"));

        /*
        dynamic function interpretation designed by Jiachen Ren

        TextInput textInput = JNode.getTextInputById("f(x)");
        if (textInput != null) {
            textInput.onKeyTyped(() -> {
                try {
                    textInput.setStaticContent(Function.interpret(textInput.getContent()).getOperable().toString());
                    textInput.getSubmitMethod().run();
                } catch (RuntimeException e) {
                    System.out.println((char) 27 + "[1;31m" + "interpretation incomplete -> pending..." + (char) 27 + "[0m");
                }
            });
        }
        graphWrapper.add(func1);

        HBox func2 = new HBox();
        func2.setMargins(0, 0);
        func2.add(new Label().setContent("g(x)=").setRelativeW(0.13f));
        func2.add(new TextInput().onSubmit(() -> {
            TextInput temp = JNode.getTextInputById("g(x)");
            try {
                if (temp != null) {
                    graph.override("test2", Function.interpret(temp.getStaticContent()).setColor(color(100, 0, 200)));
                    InterpretedFunction function = ((InterpretedFunction) graph.getFunction("test2"));
                    Operable extracted = function.getOperable();
                    temp.setStaticContent(extracted.toString());
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }).setStaticContent("(x^3-3*x^2+3*x+1)/(x-1)*cot<x>").setId("g(x)"));
        graphWrapper.add(func2);

        VBox std = new VBox(0.1f, 0.6f);
        //std.setContainerVisible(true);
        //std.setRelativeH(0.5f); TODO DEBUG
        std.setMarginX(0);
        parent.add(std);

        std.add(new Label().setContent("Window").setAlign(CENTER));
        std.add(new Label().setContent("Min X").setAlign(CENTER));
        TextInput minX = new TextInput().setDefaultContent("-10.0");
        std.add(minX);

        std.add(new Label().setContent("Max X").setAlign(CENTER));
        TextInput maxX = new TextInput().setDefaultContent("10.0");
        std.add(maxX);

        std.add(new Label().setContent("Min Y").setAlign(CENTER));
        TextInput minY = new TextInput().setDefaultContent("-10.0");
        std.add(minY);

        std.add(new Label().setContent("Max Y").setAlign(CENTER));
        TextInput maxY = new TextInput().setDefaultContent("10.0");
        std.add(maxY);

        std.add(new Button().setContent("Equalize Axis").onClick(graph::equalizeAxes));
        std.add(new Button().setContent("Center Origin").onClick(graph::centerOrigin));

        Runnable alterDimension = () -> graph.setWindow(minX.getFloatValue(), maxX.getFloatValue(), minY.getFloatValue(), maxY.getFloatValue());
        maxX.onSubmit(alterDimension);
        minX.onSubmit(alterDimension);
        maxY.onSubmit(alterDimension);
        minY.onSubmit(alterDimension);

        std.add(new SpaceHolder());
        std.add(new Label("Control").setAlign(CENTER));
        std.add(new Button().setContent("Drag").onClick(() -> graph.setMode(Graph.Mode.DRAG)));
        std.add(new Button().setContent("Zoom In").onClick(() -> graph.setMode(Graph.Mode.ZOOM_IN)));
        std.add(new Button().setContent("Zoom Out").onClick(() -> graph.setMode(Graph.Mode.ZOOM_OUT)));
        std.add(new Button().setContent("Zoom Rect").onClick(() -> graph.setMode(Graph.Mode.ZOOM_RECT)));

        std.add(new SpaceHolder());
        std.add(new Button("Show Advanced").setAlign(CENTER));




        JNode.add(parent);

        UnaryOperation.define("~", Function.interpret("x*x"));
        BinaryOperation.define("%", 2, (a, b) -> a % b);
        Constants.define("c", () -> 1);
        Constants.list();
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
*/