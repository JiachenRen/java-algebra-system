package jui_lib.bundles;

import jui_lib.*;
import jui_lib.Button;
import jui_lib.Label;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

import java.util.ArrayList;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.UP;

/**
 * package initiated April 22nd, a collection of utilities with JUI
 * color selector with JUI lib. Created April 22nd.
 */
public class ColorSelector extends VBox {

    private PApplet parent;
    private LinkedColorVar currentColorVar;
    private VBox colorVarsWrapper;
    private HBox centralPanelWrapper;
    private HBox slidersWrapper;
    private HBox titleWrapper;
    private Label colorSliderValues[];
    private Label renderedColor;
    private Label title;
    private VSlider colorSliders[];
    private static PFont font = JNode.UNI_FONT;
    private ArrayList<LinkedColorVar> linkedColorVars;

    public ColorSelector(String id, float relativeW, float relativeH) {
        super(id, relativeW, relativeH);
    }

    public ColorSelector(String id) {
        super(id);
    }

    public ColorSelector(String id, float x, float y, float w, float h) {
        super(id, x, y, w, h);
    }

    /**
     * @deprecated public void setFontSize(int fontSize) {
     * if (fontSize <= 0) return;
     * font = getParent().createFont("Seravek-Regular", fontSize);
     * updateFont();
     * }
     */

    private void updateFont() {
        title.setTextFont(font);
        for (Label label : colorSliderValues)
            label.setTextFont(font);
        for (LinkedColorVar linkedColorVar : linkedColorVars)
            linkedColorVar.setTextFont(font);
    }

    /**
     * initialize the color selector obj, including all of the sub components
     */
    public void init() {
        super.init();

        parent = JNode.getParent();
        linkedColorVars = new ArrayList<>();
        colorSliders = new VSlider[4];
        colorSliderValues = new Label[4];
        String colorVarNames[] = new String[]{"R", "G", "B", "Alpha"};

        title = new Label("title");
        title.setContent("Color Selector");

        renderedColor = new Label("renderedColor", .1f, 1.0f);
        renderedColor.setContent("");
        renderedColor.setBackgroundColor(255);

        titleWrapper = new HBox("titleWrapper");
        titleWrapper.add(title);
        titleWrapper.add(renderedColor);

        colorVarsWrapper = new VBox("currentColorVarWrapper", 0.3f, 1.0f);
        colorVarsWrapper.setContainerVisible(true);

        slidersWrapper = new HBox("slidersWrapper");
        slidersWrapper.setContainerVisible(false);


        for (int i = 0; i < colorSliders.length; i++) {
            VSlider colorSlider = new VSlider(i + "", 1.0f, 0.8f);
            colorSlider.setRollerShape(PConstants.RECT);
            colorSlider.setRollerScalingWidth(1.0f);
            colorSlider.setRange(0, 255);
            colorSlider.setValue(255);
            update(colorSlider, i);

            Label colorSliderValLabel = new Label(colorVarNames[i]);
            colorSliderValLabel.setAlign(CENTER);
            colorSliderValLabel.setContent(255 + "");

            Label colorSliderNameLabel = new Label(colorVarNames[i] + "rect");
            colorSliderNameLabel.setAlign(CENTER);
            colorSliderNameLabel.setContent(colorVarNames[i]);

            VBox rgbSliderWrapper = new VBox("rgbSliderWrapper");
            rgbSliderWrapper.add(colorSliderValLabel);
            colorSliderValues[i] = colorSliderValLabel;
            rgbSliderWrapper.add(colorSlider);
            colorSliders[i] = colorSlider;
            rgbSliderWrapper.add(colorSliderNameLabel);
            slidersWrapper.add(rgbSliderWrapper);

        }

        centralPanelWrapper = new HBox("centralPanelWrapper", 1.0f, 0.8f);
        centralPanelWrapper.setMargins(0, 0);
        centralPanelWrapper.setSpacing(3);
        centralPanelWrapper.add(colorVarsWrapper);
        centralPanelWrapper.add(slidersWrapper);

        this.setSpacing(3);
        this.setMargins(3, 3);
        this.setRounded(true);
        this.setRounding(5);
        this.setContainerVisible(false);
        this.setAlignV(DOWN);
        this.add(titleWrapper);
        this.add(centralPanelWrapper);
        this.applyLayoutToNodes(); // consider adding exclusion?

        this.setSpacing(0);
        this.setMargins(0, 0);
        centralPanelWrapper.setMargins(0, 0).setSpacing(0);
        slidersWrapper.setMargins(0, 0);
        updateRenderedColorDim();
        updateRenderedColor();
    }

    private void update(VSlider colorSlider, int index) {
        colorSlider.onFocus(() -> {
            if (index == 0) currentColorVar.setRed(colorSlider.getIntValue());
            else if (index == 1) currentColorVar.setGreen(colorSlider.getIntValue());
            else if (index == 2) currentColorVar.setBlue(colorSlider.getIntValue());
            else if (index == 3) currentColorVar.setAlpha(colorSlider.getIntValue());
            colorSliderValues[index].setContent(colorSlider.getIntValue() + "");
            updateRenderedColor();
        });
    }

    /**
     * updates all the displayables
     */
    private void updateRenderedColorDim() {
        float th = renderedColor.getHeight();
        renderedColor.setRelativeW(th / titleWrapper.getWidth());
    }

    @Override
    public void display() {
        super.display();
        if (currentColorVar.linkedMethod != null)
            currentColorVar.linkedMethod.run();
    }

    public ColorSelector link(String varName, Runnable runnable) {
        link(getLinkedColorVarById(varName), runnable);
        return this;
    }

    //change listener
    public void link(LinkedColorVar linkedColorVar, Runnable runnable) {
        linkedColorVar.setLinkedMethod(runnable);
    }

    public int getColorRGBA(String varName) {
        LinkedColorVar linkedColorVar = getLinkedColorVarById(varName);
        if (linkedColorVar != null)
            return linkedColorVar.getColorRGBA();
        return -1;
    }

    public LinkedColorVar getLinkedColorVarById(String id) {
        for (LinkedColorVar linkedColorVar : linkedColorVars) {
            if (linkedColorVar.getId().equals(id)) {
                return linkedColorVar;
            }
        }
        return null;
    }

    public ColorSelector setLinkedColorVars(String... names) {
        linkedColorVars = new ArrayList<>();
        colorVarsWrapper.removeAll();

        for (int i = 0; i < names.length; i++) {
            LinkedColorVar linkedColorVar;
            if (names.length <= 5) {
                linkedColorVar = new LinkedColorVar(names[i], 1.0f, 0.2f);
            } else {
                linkedColorVar = new LinkedColorVar(names[i]);
            }
            linkedColorVar.setContent(names[i]);
            linkedColorVar.setBackgroundColor(backgroundColor);
            linkedColorVar.setMousePressedBackgroundColor(mousePressedBackgroundColor);
            linkedColorVar.setMouseOverBackgroundColor(mouseOverBackgroundColor);
            linkedColorVar.onClick(() -> {
                setFocus(linkedColorVar.getId());
            });
            linkedColorVars.add(linkedColorVar);
            colorVarsWrapper.add(linkedColorVar);
        }

        /*
        the currentColorVar defaults to the first in the arrayList.
         */
        currentColorVar = linkedColorVars.get(0);
        return this;
    }


    private void setFocus(String linkedColorVarId) {
        for (LinkedColorVar linkedColorVar : linkedColorVars) {
            if (linkedColorVar.getId().equals(linkedColorVarId))
                currentColorVar = linkedColorVar;
        }

        int rgba[] = new int[]{
                currentColorVar.getRed(),
                currentColorVar.getGreen(),
                currentColorVar.getBlue(),
                currentColorVar.getAlpha()
        };

        for (int i = 0; i < rgba.length; i++) {
            colorSliders[i].setValue(rgba[i]);
            colorSliderValues[i].setContent(rgba[i] + "");
        }
        updateRenderedColor();
    }

    private void updateRenderedColor() {
        int rgba[] = new int[4];
        for (int i = 0; i < rgba.length; i++)
            rgba[i] = colorSliders[i].getIntValue();
        renderedColor.setBackgroundColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public ColorSelector setColorRGBA(String id, int r, int g, int b, int a) {
        for (LinkedColorVar linkedColorVar : linkedColorVars) {
            if (linkedColorVar.getId().equals(id))
                linkedColorVar.setColorRGBA(r, g, b, a);
        }
        return this;
    }

    @Override
    public void resize(float w, float h) {
        super.resize(w, h);

        /*
         * TODO
         * @warning this is resource exhaustive
         */
        updateRenderedColorDim();//TODO
    }

    @Override
    public void relocate(float x, float y) {
        super.relocate(x, y);
    }

    /*
    if you want your code to word, do not forget to implement Controllable! Or it would not be recognized in JNode
     */
    class LinkedColorVar extends Button implements Controllable {
        private int red, green, blue;
        private int alpha;
        private Runnable linkedMethod;

        LinkedColorVar(String id, float relativeW, float relativeH) {
            super(id, relativeW, relativeH);
            init();
        }

        LinkedColorVar(String id) {
            super(id);
            init();
        }

        public LinkedColorVar(String id, float x, float y, float w, float h) {
            super(id, x, y, w, h);
            init();
        }

        int getColorRGBA() {
            return getParent().color(red, green, blue, alpha);
        }

        public int getColorRGB() {
            return getParent().color(red, green, blue);
        }

        void setColorRGBA(int r, int g, int b, int a) {
            setRed(r);
            setGreen(g);
            setBlue(b);
            setAlpha(a);
        }

        int getRed() {
            return red;
        }

        int getGreen() {
            return green;
        }

        int getBlue() {
            return blue;
        }

        int getAlpha() {
            return alpha;
        }

        void setRed(int r) {
            red = r;
        }

        void setGreen(int g) {
            green = g;
        }

        void setBlue(int b) {
            blue = b;
        }

        void setAlpha(int a) {
            alpha = a;
        }

        void setLinkedMethod(Runnable runnable) {
            linkedMethod = runnable;
        }
    }
}
