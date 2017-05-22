import jmc_lib.*;
import jui_lib.*;
import processing.core.PApplet;

/**
 * Created by Jiachen on 16/05/2017.
 */
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
        JNode.TEXT_COLOR = color(0, 0, 255);
        JNode.BACKGROUND_COLOR = color(255);
        JNode.ROUNDED = false;

        HBox parent = new HBox("parent", 0, 0, width, height);
        parent.setMargins(0, 0);
        parent.matchWindowDimension(true);

        VBox graphWrapper = new VBox("graphWrapper", 0.9f, 1.0f);
        parent.add(graphWrapper);

        Graph graph = new Graph("graph", 1.0f, 0.9f);
        graphWrapper.add(graph);

        Function test1 = Function.interpret("((sin<x>*(x+3.0))/((cos<x>*tan<x>)-x))*x").setName("test1").setColor(color(0, 150, 50));
        test1.setAsymptoteVisible(true);
        test1.setTangentLineVisible(true);
        graph.add(test1);

        HBox functionInputWrapper1 = new HBox("functionInputWrapper1");
        functionInputWrapper1.setMargins(0, 0);
        functionInputWrapper1.add(new Label("").setContent("f(x)=").setRelativeW(0.13f));
        functionInputWrapper1.add(new TextInput("f(x)").onSubmit(() -> {
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
        }).setStaticContent("((sin<x>*(x+3.0))/((cos<x>*tan<x>)-x))*x"));

        /*
        dynamic function interpretation designed by Jiachen Ren
         */
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
        graphWrapper.add(functionInputWrapper1);

        HBox functionInputWrapper2 = new HBox("functionInputWrapper2");
        functionInputWrapper2.setMargins(0, 0);
        functionInputWrapper2.add(new Label("").setContent("g(x)=").setRelativeW(0.13f));
        functionInputWrapper2.add(new TextInput("g(x)").onSubmit(() -> {
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
        }).setStaticContent("(x^3-3*x^2+3*x+1)/(x-1)*cot<x>"));
        graphWrapper.add(functionInputWrapper2);

        VBox windowDimensionWrapper = new VBox("windowDimensionWrapper", 0.1f, 0.3f);
        //windowDimensionWrapper.setContainerVisible(true);
        //windowDimensionWrapper.setRelativeH(0.5f); TODO DEBUG
        windowDimensionWrapper.setMarginX(0);
        parent.add(windowDimensionWrapper);

        windowDimensionWrapper.add(new Label("").setContent("Window").setAlign(CENTER));
        windowDimensionWrapper.add(new Label("").setContent("Min X").setAlign(CENTER));
        TextInput minX = new TextInput("minX");
        minX.setDefaultContent("-10.0");
        windowDimensionWrapper.add(minX);

        windowDimensionWrapper.add(new Label("").setContent("Max X").setAlign(CENTER));
        TextInput maxX = new TextInput("maxX");
        maxX.setDefaultContent("10.0");
        windowDimensionWrapper.add(maxX);

        windowDimensionWrapper.add(new Label("").setContent("Min Y").setAlign(CENTER));
        TextInput minY = new TextInput("minY");
        minY.setDefaultContent("-10.0");
        windowDimensionWrapper.add(minY);

        windowDimensionWrapper.add(new Label("").setContent("Max Y").setAlign(CENTER));
        TextInput maxY = new TextInput("maxY");
        maxY.setDefaultContent("10.0");
        windowDimensionWrapper.add(maxY);

        windowDimensionWrapper.add(new Button("eq-axes").setContent("Equalize Axis").onClick(graph::equalizeAxes));
        windowDimensionWrapper.add(new Button("ctr-org").setContent("Center Origin").onClick(graph::centerOrigin));

        Runnable alterDimension = () -> graph.setWindow(minX.getFloatValue(), maxX.getFloatValue(), minY.getFloatValue(), maxY.getFloatValue());
        maxX.onSubmit(alterDimension);
        minX.onSubmit(alterDimension);
        maxY.onSubmit(alterDimension);
        minY.onSubmit(alterDimension);

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
