package jui_lib;

import processing.core.PConstants;

import static processing.core.PApplet.*;

//add public abstract void setValue(float val);
//Jan 29th: deprecated all textHeight dividend variable in TextInput, Button, and Label.
//TODO the adjustTextSize method should be replaced with a better one. April 22nd.
public class Label extends Contextual {
    public Label(String id, float x, float y, float w, float h) {
        super(id, x, y, w, h);
        init();
    }

    public Label(String id, float relativeW, float relativeH) {
        super(id, relativeW, relativeH);
        init();
    }

    public Label(String id) {
        super(id);
        init();
    }

    public void init() {
        setContent("Label");
        setTextStyle(JStyle.CONSTANT);
        setAlign(PConstants.LEFT);
    }


    public void display() {
        //drawing the background
        super.display(); /*modified April 22nd*/
        displayText();
    }

    @Override
    public Label setContent(String content) {
        super.setContent(content);
        return this;
    }
}