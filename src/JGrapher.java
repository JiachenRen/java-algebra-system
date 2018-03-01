import jmc_lib.*;
import jui_lib.*;
import jui_lib.bundles.ColorSelector;
import jui_lib.bundles.ValueSelector;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jiachen on 22/05/2017.
 * JGrapher, created May 22nd. Breakthrough.
 */
public class JGrapher extends PApplet {
    private Graph graph;

    public static void main(String args[]) {
        System.out.println("Function Interpretation Test May 16th");
        String sketch = Thread.currentThread().getStackTrace()[1].getClassName();
        Thread proc = new Thread(() -> PApplet.main(sketch));
        proc.start();
    }

    public void settings() {
        size(1100, 780, FX2D);
    }

    private static void enableCoreDump() {
        try {
            Runtime.getRuntime().exec("ulimit -c unlimited");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Process process = Runtime.getRuntime().exec("ulimit -c -l");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String incrementer = "", temp;
            while ((temp = input.readLine()) != null) incrementer += temp;
            if (incrementer.contains("(blocks, -c) 0")) {
                System.err.println("# Fatal Error: Core Dump Failed --> Resurrecting... Failed");
                //enableCoreDump();
                //exit();
            } else System.out.println("# Core Dump Successfully Enabled");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setup() {
        sketchRenderer();
        JNode.init(this);
        if (!JNode.OS.contains("windows")) JGrapher.enableCoreDump();

        JNode.DISPLAY_CONTOUR = true;
        JNode.CONTOUR_THICKNESS = 0.5f;
        //JNode.CONTOUR_COLOR = color(0, 0, 100);
        JNode.BACKGROUND_COLOR = color(255);
        JNode.ROUNDED = false;

        HBox parent = new HBox(0, 0, width, height);
        parent.setCollapseInvisible(true);
        parent.setId("parent");
        parent.setMargins(1, 0);
        parent.matchWindowDimension(true);

        VBox graphWrapper = new VBox();
        graphWrapper.setId("graphWrapper");
        parent.add(graphWrapper);

        Displayable modelLabel = new Label().setAlign(CENTER).setContourVisible(false).setBackgroundColor(0, 0, 0, 25);

        graphWrapper.add(new Label("Grapher Version 1.0 By Jiachen Ren").inheritOutlook(modelLabel));

        graph = new Graph(1.0f, 0.93f);
        graph.setId("graph");
        graphWrapper.add(graph);

        HBox functionInputWrapper = new HBox();
        functionInputWrapper.setMargins(0, 0).setId("#functionInputWrapper");
        graphWrapper.add(functionInputWrapper);

        TextInput functionNameLabel = new TextInput();
        Runnable updateAdvancedPanel = () -> {
            String name = functionNameLabel.getContent();
            Function function = graph.getFunction(name);
            if (function == null) return;
            ValueSelector strokeWeight = (ValueSelector) JNode.get("#0").get(0);
            strokeWeight.setValue(function.getStrokeWeight());

            Button showAsymptotes = (Button) JNode.get("#1").get(0);
            showAsymptotes.setContent(function.isAsymptoteVisible() ? "Hide Asymptote" : "Show Asymptote");

            Button showTangentLine = (Button) JNode.get("#2").get(0);
            showTangentLine.setContent(function.isTangentLineVisible() ? "Hide f'(x)" : "Show f'(x)");

            Button visibility = (Button) JNode.get("#3").get(0);
            visibility.setContent(function.isVisible() ? "Visible" : "Invisible");

            Button continuous = (Button) JNode.get("#4").get(0);
            continuous.setContent(function.getStyle().equals(Function.Style.CONTINUOUS) ? "Continuous" : "Discrete");

            Button dynamic = (Button) JNode.get("#5").get(0);
            dynamic.setContent(function.isDynamic() ? "Dynamic" : "Static");

            ColorSelector functionColor = (ColorSelector) JNode.get("#6").get(0);
            functionColor.setLinkedColorVars(name);
            functionColor.setColor(name, function.getColor());
            functionColor.link(name, () -> graph.getFunction(name).setColor(functionColor.getColorRGBA(name)));
            functionColor.setAsSingleVarMode(); //in lambda, the original function became immutable, thus it needs to be reacquired.

            Button extensionOn = (Button) JNode.get("#7").get(0);
            extensionOn.setContent(function.isAutoAsymptoteExtension() ? "On" : "Off");

            Switch tracingOn = (Switch) JNode.get("#8").get(0);
            tracingOn.setState(function.tracingEnabled());

            Switch match = (Switch) JNode.get("#9").get(0);
            match.setState(function.isMatchAuxiliaryLinesColor());

        };
        TextInput functionTextInput = new TextInput();
        functionNameLabel.setContent("f(x)=").setAlign(CENTER).setId("functionNameLabel").setRelativeW(0.13f).addEventListener(Event.CONTENT_CHANGED, () -> {
            String name = functionNameLabel.getContent();
            Function function = graph.getFunction(name);
            if (function == null) return;
            updateAdvancedPanel.run();
            //noinspection ConstantConditions
            functionTextInput.setContent(((InterpretedFunction) function).getOperable().toString());
        });
        functionInputWrapper.add(functionNameLabel);

        /*
        dynamic function interpretation designed by Jiachen Ren
         */

        functionTextInput.onSubmit(() -> {
            try {
                functionTextInput.setContent(Function.interpret(functionTextInput.getStaticContent()).getOperable().toString());
            } catch (RuntimeException e) {
                System.out.println((char) 27 + "[1;34m" + "simplification failed -> incomplete input" + (char) 27 + "[0m");
            }
        }).setDefaultContent(" type your function in here").setId("f(x)");
        functionTextInput.onKeyTyped(() -> {
            try {
                graph.override(functionNameLabel.getContent(), Function.interpret(functionTextInput.getContent()));
                updateAdvancedPanel.run();
            } catch (RuntimeException e) {
                System.out.println((char) 27 + "[1;31m" + "interpretation incomplete -> pending..." + (char) 27 + "[0m");
            }
        });
        functionInputWrapper.add(functionTextInput);

        VBox std = new VBox(0.1f, 1.0f);
        std.setMarginX(0);
        parent.add(std);

        std.add(new Label().setContent("Window").inheritOutlook(modelLabel));

        Runnable window = () -> {
            @SuppressWarnings("unchecked") ArrayList<TextInput> displayables = (ArrayList<TextInput>) JNode.get("$window");
            graph.setWindow(displayables.get(0).getFloatValue(), displayables.get(1).getFloatValue(), displayables.get(2).getFloatValue(), displayables.get(3).getFloatValue());
        };

        Label windowModelLabel = new Label();
        windowModelLabel.setContent("Max X")
                .inheritOutlook(modelLabel)
                .setRelativeW(0.3f);

        TextInput modelTextInput = new TextInput();
        modelTextInput.onSubmit(window)
                .setDefaultContent("10.0")
                .setId("$window");

        std.add(new HBox().add(new Label()
                .setContent("Min X")
                .inheritOutlook(windowModelLabel)
                .inheritDisplayProperties(windowModelLabel)).add(new TextInput()
                .onSubmit(window)
                .setDefaultContent("-10.0")
                .setId("$window")).setMargins(0, 0)
        );
        std.add(new HBox().add(windowModelLabel).add(modelTextInput).setMargins(0, 0));

        std.add(new HBox().add(new Label()
                .setContent("Min Y")
                .inheritOutlook(windowModelLabel)
                .inheritDisplayProperties(windowModelLabel)).add(new TextInput()
                .onSubmit(window)
                .setDefaultContent("-10.0")
                .setId("$window")).setMargins(0, 0)
        );
        std.add(new HBox().add(new Label()
                .setContent("Max Y")
                .inheritOutlook(windowModelLabel)
                .inheritDisplayProperties(windowModelLabel)).add(new TextInput()
                .onSubmit(window)
                .setDefaultContent("10.0")
                .setId("$window")).setMargins(0, 0)
        );


        std.add(new Button().setContent("Equalize Axis").onClick(graph::equalizeAxes));
        std.add(new Button().setContent("Center Origin").onClick(graph::centerOrigin));
        std.add(new SpaceHolder());
        std.add(new Label("Graph").inheritOutlook(modelLabel));
        std.add(new Switch().setContentOff("Trace Off").setContentOn("Trace On").setState(graph.tracingIsOn()).onClick(() -> graph.setTracingOn(!graph.tracingIsOn())));
        std.add(new Switch().setContentOff("Axes Off").setContentOn("Axes On").setState(graph.isAxesVisible()).onClick(() -> graph.setAxesVisible(!graph.isAxesVisible())));
        std.add(new SpaceHolder());
        std.add(new Label("Evaluation").inheritOutlook(modelLabel));
        std.add(new Switch().setContentOff("Off").setContentOn("On").setState(graph.isEvaluationOn()).onClick(() -> graph.setEvaluationOn(!graph.isEvaluationOn())));
        std.add(new SpaceHolder());
        std.add(new Label("Control").inheritOutlook(modelLabel));
        std.add(new Button().setContent("Drag").onClick(() -> graph.setMode(Graph.Mode.DRAG)));
        std.add(new Button().setContent("Zoom In").onClick(() -> graph.setMode(Graph.Mode.ZOOM_IN)));
        std.add(new Button().setContent("Zoom Out").onClick(() -> graph.setMode(Graph.Mode.ZOOM_OUT)));
        std.add(new Button().setContent("Zoom Rect").onClick(() -> graph.setMode(Graph.Mode.ZOOM_RECT)));

        std.add(new SpaceHolder());
        VBox functionsWrapper = new VBox(1.0f, 0.15f);
        functionsWrapper.setMargins(0, 0);
        for (int i = 0; i < 5; i++) {
            functionsWrapper.add(new Button().setVisible(false));
        }


        std.add(new Label().setContent("Filter").inheritOutlook(modelLabel));
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
            for (int i = 0; i < 5; i++) {
                if (i < filtered.size()) {
                    InterpretedFunction function = (InterpretedFunction) filtered.get(i);
                    final String functionName = function.getName();
                    final String operable = function.getOperable().toString();
                    Button temp = (Button) functionsWrapper.getDisplayables().get(i);
                    temp.setContent(functionName).setVisible(true);
                    temp.onClick(() -> {
                        functionNameLabel.setContent(functionName);
                        //noinspection ConstantConditions
                        JNode.getTextInputById("f(x)").setContent(operable);
                    });
                } else {
                    functionsWrapper.getDisplayables().get(i).setVisible(false);
                }
            }
        }).setId("filterer"));
        std.add(functionsWrapper);


        std.add(new SpaceHolder());

        VBox adv = new VBox(0.1f, 1f);
        adv.setMarginX(0);
        adv.setVisible(false);
        parent.add(adv);

        Button button = new Button("Show Advanced");
        button.attachMethod(() -> button.setVisible(graph.getFunctions().size() > 0));
        button.onClick(() -> {
            button.setContent(button.getContent().equals("Show Advanced") ? "Hide Advanced" : "Show Advanced");
            adv.setVisible(!adv.isVisible);
            parent.syncSize();
            parent.arrange();
        }).setAlign(CENTER).setVisible(false);
        std.add(button);
        std.add(new Button("Exit").onClick(() -> Runtime.getRuntime().exit(0)));

        adv.add(new Label().setAlign(CENTER).setContent("Advanced").inheritOutlook(modelLabel));


        adv.add(new SpaceHolder());
        adv.add(new Label().setContent("Attributes").inheritOutlook(modelLabel));
        adv.add(new Switch().setContentOn("Tracing Enabled").setContentOff("Tracing Disabled").onClick(() -> {
            Switch self = (Switch) JNode.get("#8").get(0);
            getCurrentFunction().setTracingEnabled(self.isOn());
        }).setId("#8"));

        Button showAsymptotes = new Button().setContent("Show Asymptotes");
        showAsymptotes.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setAsymptoteVisible(!function.isAsymptoteVisible());
            showAsymptotes.setContent(showAsymptotes.getContent().equals("Show Asymptotes") ? "Hide Asymptotes" : "Show Asymptotes");
        }).setId("#1");
        adv.add(showAsymptotes);

        Button showTangentLine = new Button().setContent("Show f'(x)");
        showTangentLine.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setTangentLineVisible(!function.isTangentLineVisible());
            showTangentLine.setContent(showTangentLine.getContent().equals("Show f'(x)") ? "Hide f'(x)" : "Show f'(x)");
        }).setId("#2");
        adv.add(showTangentLine);

        Button visibility = new Button().setContent("Visible");
        visibility.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setVisible(!function.isVisible());
            visibility.setContent(visibility.getContent().equals("Visible") ? "Invisible" : "Visible");
        }).setId("#3");
        adv.add(visibility);

        Button continuous = new Button().setContent("Continuous");
        continuous.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setGraphStyle(function.getStyle().equals(Function.Style.CONTINUOUS) ? Function.Style.DISCRETE : Function.Style.CONTINUOUS);
            continuous.setContent(continuous.getContent().equals("Continuous") ? "Discrete" : "Continuous");
        }).setId("#4");
        adv.add(continuous);

        Button dynamic = new Button().setContent("Static");
        dynamic.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setDynamic(!function.isDynamic());
            dynamic.setContent(dynamic.getContent().equals("Static") ? "Dynamic" : "Static");
        }).setId("#5");
        adv.add(dynamic);

        adv.add(new SpaceHolder());
        adv.add(new Label().setContent("VA Extension").inheritOutlook(modelLabel));
        Button extensionOn = new Button().setContent("On");
        extensionOn.onClick(() -> {
            Function function = getCurrentFunction();
            if (function != null)
                function.setAutoAsymptoteExtension(!function.isAutoAsymptoteExtension());
            extensionOn.setContent(extensionOn.getContent().equals("On") ? "Off" : "On");
        }).setId("#7");
        adv.add(extensionOn);

        adv.add(new SpaceHolder());
        adv.add(new SpaceHolder());
        adv.add(new Label().setContent("Color").inheritOutlook(modelLabel));

        ColorSelector functionColor = new ColorSelector(1.0f, 0.3f);
        functionColor.setId("#6");
        functionColor.getTitleWrapper().setRelativeH(0.13f);
        adv.add(functionColor);

        adv.add(new SpaceHolder());

        adv.add(new Label().setContent("Additional").inheritOutlook(modelLabel));
        adv.add(new Switch().setContentOn("Differentiate").setContentOff("Uniform").onClick(() -> {
            Switch self = (Switch) JNode.get("#9").get(0);
            getCurrentFunction().setMatchAuxiliaryLinesColor(self.isOn());
        }).setId("#9"));
        ValueSelector strokeWeight = new ValueSelector(1.0f, 0.057f);
        strokeWeight.setTitlePercentage(0.7f);
        strokeWeight.getTitleLabel().setAlign(CENTER);
        strokeWeight.setRange(0.1f, 3).setTitle("Stroke Weight").setValue(1f).link(() -> {
            getCurrentFunction().setStrokeWeight(strokeWeight.getFloatValue());
        }).setId("#0");
        adv.add(strokeWeight);
        adv.add(new SpaceHolder());
        adv.add(new Button().setContent(JNode.ROUNDED ? "Rounded" : "Rectangular").onClick(() -> {
            JNode.getDisplayables().forEach(displayable -> {
                displayable.setRounded(!displayable.isRounded);
            });
            Button self = (Button) JNode.get("UI-ROUNDING").get(0);
            self.setContent(self.getContent().equals("Rounded") ? "Rectangular" : "Rounded");
        }).setId("UI-ROUNDING"));


        JNode.add(parent);

        UnaryOperation.define("~", Function.interpret("x*x"));
        BinaryOperation.define("%", 2, (a, b) -> a % b);
        Constants.define("$C", () -> 1);

        Element.getList().forEach(e -> Constants.define("$" + e, e::getAtomicMass));

        Constants.list();

        graph.equalizeAxes();
    }

    @SuppressWarnings("ConstantConditions")
    private Function getCurrentFunction() {
        return graph.getFunction(JNode.getTextInputById("functionNameLabel").getContent());
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
        if (keyCode == ESC) key = (char) 0;
        JNode.keyPressed();
    }

    public void keyReleased() {
        JNode.keyReleased();
    }

    public void mouseWheel() {
        //to be implemented. Jan 27th.
    }

}
