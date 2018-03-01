package jui;

import processing.core.PConstants;
import processing.core.PGraphics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//designing. Jan 27th. Remember to implement mouseWheel()
//building class, Jan 30th. PGraphics class is applied.
//not finished Jan 30th.
/*April 22nd: text wrapped withing text box!!! text(str,x,y,w,h)*/
//TODO: complete designing the class.
public class TextField extends Contextual {
    private int spacing;
    private float textHeight;
    private PGraphics textArea;
    private int tx, ty;

    public TextField(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public TextField(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public TextField() {
        super();
        init();
    }

    private void init() {
        //setTextSize(15); //setting the default text size to 15
        //textHeight = this.getTextDimension("a")[1];
        setAlign(PConstants.LEFT);
        setSpacing(5);
        updateTextAreaDim();
    }

    public void display() {
        //drawing the background
        //does not work with FX2D!!!
        if (font != null) textArea.textFont(font);
        super.display();

        //displaying the texts on the PGraphics unit, creating a second layer.

        if (textArea == null) return;
        textArea.beginDraw();
        textArea.background(this.backgroundColor);
        textArea.fill(getTextColor());
        //if (textSize > 0.0) textArea.textSize(textSize);
        textArea.textAlign(alignment);
        //textArea.background(0,0,0); testing completed. Jan 30th. 9:59 PM.
        textArea.text("Hello, My name is Jiachen. I built JUI", textArea.width / 2, textArea.height / 2);
        textArea.endDraw();
        getParent().image(textArea, tx, ty);


    }

    private void updateTextAreaDim() {
        /*
        fill the width method.
        textArea.width = w;
        textArea.height = this.getHeight()-(isRounded?rounding*2:0);
        tx = x;
        ty = y+(isRounded?rounding:0);
        */

        /*
        fill the height (which I decided is more reasonable)
        textArea = getParent().createGraphics(getWidth() - (isRounded ? rounding * 2 : 0), this.getHeight(), getParent().sketchRenderer());
        textArea.pixelDensity = getParent().pixelDensity;
        //System.err.println(textArea.width);
        tx = x + (isRounded ? rounding : 0);
        ty = y;
        */

    }

    /**
     * TODO to be completed
     * @param path the path of the file to be loaded
     * @return this instance of text field
     */
    public TextField load(String path) {
        try {
            new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public TextField append() {
        return this;
    }


    public TextField setSpacing(int temp) {
        this.spacing = temp;
        return this;
    }

    @Override
    public void resize(float w, float h) {
        super.resize(w, h);
        updateTextAreaDim();
    }

    @Override
    public TextField setRounded(boolean temp) {
        super.setRounded(temp);
        updateTextAreaDim();
        return this;
    }

    @Override
    public void relocate(float x, float y) {
        super.relocate(x, y);
        updateTextAreaDim();
    }
}