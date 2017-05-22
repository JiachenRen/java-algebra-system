import jmc_lib.*;
import jui_lib.*;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jiachen on 22/05/2017.
 * JGrapher, created May 22nd. Breakthrough.
 */
public class JGrapher extends PApplet {
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
        sketchRenderer();
        JNode.init(this);
        JNode.DISPLAY_CONTOUR = true;
        JNode.CONTOUR_COLOR = color(100,100,100);
        JNode.BACKGROUND_COLOR = color(255);
        JNode.ROUNDED = true;

        HBox parent = new HBox(0, 0, width, height);
        parent.setCollapseInvisible(true);
        parent.setId("parent");
        parent.setMargins(0, 0);
        parent.matchWindowDimension(true);

        VBox graphWrapper = new VBox();
        graphWrapper.setId("graphWrapper");
        parent.add(graphWrapper);

        Graph graph = new Graph(1.0f, 0.95f);
        graph.setId("graph");
        graphWrapper.add(graph);

        HBox func1 = new HBox();
        func1.setMargins(0, 0);
        func1.add(new TextInput().setContent("f(x)=").setId("func_name").setRelativeW(0.13f));
        func1.add(new TextInput().onSubmit(() -> {
            TextInput temp = JNode.getTextInputById("f(x)");
            TextInput name = JNode.getTextInputById("func_name");
            try {
                if (temp != null && name != null) {
                    String func_name = name.getContent();
                    graph.override(func_name, Function.interpret(temp.getStaticContent()));
                    InterpretedFunction function = ((InterpretedFunction) graph.getFunction(func_name));
                    Operable extracted = function.getOperable();
                    //if (extracted instanceof Operation) ((Operation) extracted).toExponentialForm();
                    temp.setStaticContent(extracted.toString());
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }).setStaticContent("((sin<x>*(x+3.0))/((cos<x>*tan<x>)-x))*x").setId("f(x)"));

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
        graphWrapper.add(func1);

        VBox std = new VBox(0.1f, 1.0f);
        //std.setContainerVisible(true);
        //std.setRelativeH(0.5f); //TODO DEBUG
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
        VBox functionsWrapper = new VBox(1.0f,0.15f);
        functionsWrapper.setMargins(0, 0);


        std.add(new Label().setContent("Filter").setAlign(CENTER));
        std.add(new TextInput().setDefaultContent("Name").onKeyTyped(() -> {
            TextInput self = JNode.getTextInputById("filterer");
            if (self == null) return;
            ArrayList<Function> functions = graph.getFunctions();
            ArrayList<Function> filtered = new ArrayList<>();
            functions.forEach(function -> {
                if (filtered.size() < 5 && function.getName().startsWith(self.getContent()))
                    if (!filtered.contains(function))
                        filtered.add(function);
            });
            functions.forEach(function -> {
                if (filtered.size() < 5 && function.getName().contains(self.getContent()))
                    if (!filtered.contains(function))
                        filtered.add(function);
            });
            functions.forEach(function -> {
                if (filtered.size() < 5 && containsAllChars(function.getName(), self.getContent()))
                    if (!filtered.contains(function))
                        filtered.add(function);
            });
            if (filtered.size() == 0) return;
            functionsWrapper.removeAll();
            filtered.forEach((function) -> {
                new Thread(() -> {
                    try {
                        functionsWrapper.add(new Button(1.0f, 0.2f).setContent(function.getName()).onClick(() -> {
                            TextInput func_name = JNode.getTextInputById("func_name");
                            TextInput func_def = JNode.getTextInputById("f(x)");
                            if (func_name == null || func_def == null) return;
                            func_name.setStaticContent(function.getName());
                            if (function instanceof InterpretedFunction)
                                func_def.setStaticContent(((InterpretedFunction) function).getOperable().toString());
                        }));
                    } catch (RuntimeException e) {
                        System.out.print("");
                    }
                }).start();
            });
        }).setId("filterer"));
        std.add(functionsWrapper);


        std.add(new SpaceHolder());

        VBox adv = new VBox(0.1f, 0.5f);
        adv.setMarginX(0);
        adv.setVisible(false);
        parent.add(adv);

        Button button = new Button("Show Advanced");
        button.onClick(() -> {
            button.setContent(button.getContent().equals("Show Advanced") ? "Hide Advanced" : "Show Advanced");
            adv.setVisible(!adv.isVisible);
            parent.syncSize();
            parent.arrange();
        }).setAlign(CENTER);
        std.add(button);

        adv.add(new Label().setContent("Advanced"));
        //adv.add();


        JNode.add(parent);

        UnaryOperation.define("~", Function.interpret("x*x"));
        BinaryOperation.define("%", 2, (a, b) -> a % b);
        Constants.define("c", () -> 1);

        Constants.list();
    }

    public static Set<Character> stringToCharacterSet(String s) {
        Set<Character> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        return set;
    }

    public static boolean containsAllChars(String container, String contained) {
        return stringToCharacterSet(container).containsAll
                (stringToCharacterSet(contained));
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
