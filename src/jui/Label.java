package jui;

import processing.core.PConstants;

//add public abstract void setValue(float val);
//Jan 29th: deprecated all textHeight dividend variable in TextInput, Button, and Label.
//TODO the adjustTextSize method should be replaced with a better one. April 22nd.
public class Label extends Contextual {
    public Label(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public Label(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public Label() {
        super();
        init();
    }

    public Label(String content) {
        super();
        init();
        setContent(content);
    }

    public void init() {
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

    @Override
    public Label setId(String id) {
        super.setId(id);
        return this;
    }

    @Override
    public Label clone() {
        return (Label) super.clone();
    }
}