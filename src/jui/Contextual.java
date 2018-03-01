package jui;

import processing.core.PFont;

import static processing.core.PConstants.*;

//code refactored Jan 18,the Displayable interface is changed into a superclass. Modified by Jiachen Ren
//code refactored Jan 20,the superclass Displayable remained as the parent, the actual parent for all the text based objects are now changed to Contextual.
//add mouseOverTextColor & mousePressedTextColor
public abstract class Contextual extends Displayable implements KeyControl {
    private String content;
    private int textColor = JNode.TEXT_COLOR;
    private int mouseOverTextColor = JNode.MOUSE_OVER_TEXT_COLOR;
    private int mousePressedTextColor = JNode.MOUSE_PRESSED_TEXT_COLOR;
    private JStyle textStyle = JStyle.CONSTANT;
    private boolean applyTextDescent;
    private float textSize;
    private float textIndent;
    private boolean autoTextDescentCompensation = JNode.AUTO_TEXT_DESCENT_COMPENSATION;
    public float maxTextPercentage = JNode.CONTEXTUAL_INIT_TEXT_PERCENTAGE;
    public PFont font = JNode.UNI_FONT;
    public String defaultContent = "";
    public int alignment;

    private static String[] applyTextDescentLetters;

    static {
        applyTextDescentLetters = new String[]{"g", "j", "p", "q", "y"};
    }

    public Contextual(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public Contextual(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public Contextual() {
        super();
        init();
    }

    public Contextual setAlign(int alignment) {
        this.alignment = alignment;
        return this;
    }

    private void init() {
        content = defaultContent;
        textSize = h * maxTextPercentage;
    }

    public float getTextWidth(String temp) {
        if (textSize > 0.0) getParent().textSize(textSize);
        return getParent().textWidth(temp);
    }

    /**
     * @return the height of the text rounded to nearest pixel
     * @since April 28th I finally got this figured out!
     * y+h+textAscent()-(y+h+textDescent()) = ascent - descent
     */
    public float getTextHeight() {
        if (textSize > 0.0) getParent().textSize(textSize);
        return getParent().textAscent() - getParent().textDescent();
    }

    public Contextual setTextColor(int r, int g, int b) {
        textColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Contextual setTextColor(int c) {
        textColor = c;
        return this;
    }

    public Contextual setTextColor(int color, int alpha) {
        textColor = JNode.resetAlpha(color, alpha);
        return this;
    }

    public Contextual setTextColor(int r, int g, int b, int t) {
        textColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Contextual setMousePressedTextColor(int r, int g, int b) {
        mousePressedTextColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Contextual setMousePressedTextColor(int c) {
        mousePressedTextColor = c;
        return this;
    }

    public Contextual setMousePressedTextColor(int r, int g, int b, int t) {
        mousePressedTextColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Contextual setMouseOverTextColor(int r, int g, int b) {
        mouseOverTextColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Contextual setMouseOverTextColor(int c) {
        mouseOverTextColor = c;
        return this;
    }

    public Contextual setMouseOverTextColor(int r, int g, int b, int t) {
        mouseOverTextColor = JNode.getParent().color(r, g, b, t);
        return this;
    }


    public int getTextColor() {
        return textColor;
    }

    ;

    public Contextual setTextSize(float temp) {
        textSize = temp;
        return this;
    }

    public Contextual setTextFont(PFont pf) {
        font = pf;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Contextual setContent(String temp) {
        content = temp;
        this.applyTextDescent = shouldApplyTextDescent(content);
        calculateTextSize();
        super.activateEventListeners(Event.CONTENT_CHANGED);
        return this;
    }

    public static boolean shouldApplyTextDescent(String temp) {
        for (String s : applyTextDescentLetters)
            if (temp.contains(s))
                return true;
        return false;
    }

    public void displayText(String s) {
        getParent().pushMatrix();
        if (textSize > 0.0) getParent().textSize(textSize);
        if (font != null) getParent().textFont(font);

        applyTextColor();
        displayRawText(s);
        getParent().popMatrix();
    }

    public void displayRawText(String textToDisplay) {
        float halfTextHeight = getTextHeight() / 2;
        float descent = getParent().textDescent();
        float ty;
        int align;

        if (applyTextDescent && autoTextDescentCompensation) {
            ty = y + h / 2 - descent;
            align = CENTER;
        } else {
            ty = y + h / 2 + halfTextHeight;
            align = DOWN;
        }

        switch (alignment) {
            case LEFT:
                getParent().textAlign(LEFT, align);
                getParent().text(textToDisplay, x, ty);
                break;
            case CENTER:
                getParent().textAlign(CENTER, align);
                getParent().text(textToDisplay, x + w / 2, ty);
                break;
            case RIGHT:
                getParent().textAlign(RIGHT, align);
                getParent().text(textToDisplay, x + w, ty);
                break;
            default:
                System.err.println(id + ": align-" + alignment + " cannot be applied to Label. Default alignment applied.");
                getParent().textAlign(LEFT, align);
                getParent().text(textToDisplay, x, ty);
        }

    }

    /**
     * @since April 27th an absolutely ridiculous error has been fixed. It appears to
     * me that the error happened inside of processing's PApplet. It keeps throwing a bug
     * where the pushMatrix() call has been called more than 32 times... Nevertheless, the
     * bug is actually caused by a null pointer exception as the JStyle instance's not yet
     * being initialized.
     */
    public void applyTextColor() {
        if (textStyle.equals(JStyle.CONSTANT)) {
            getParent().fill(textColor);
        } else if (textStyle.equals(JStyle.VOLATILE)) {
            if (isMouseOver()) {
                int color = getParent().mousePressed ? mousePressedTextColor : mouseOverTextColor;
                getParent().fill(color);
            } else {
                getParent().fill(textColor);
            }
        }
    }

    public void displayText() {
        displayText(content);
    }

    public Contextual setTextStyle(JStyle textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    public void calculateTextSize() {
        if (h <= 0) return;
        textSize = h * maxTextPercentage;
        float[] dim = new float[]{getTextWidth(getContent()), getTextHeight()};
        while (dim[0] > w || dim[1] > maxTextPercentage * h) {
            if (textSize < 3) break;
            textSize--;
            dim = new float[]{getTextWidth(getContent()), getTextHeight()};
        }
    }

    @Override
    public void resize(float w, float h) {
        super.resize(w, h);
        calculateTextSize();
    }

    public float getTextSize() {
        return textSize;
    }

    public Contextual setAutoTextDescentCompensation(boolean temp) {
        autoTextDescentCompensation = temp;
        return this;
    }

    public boolean isApplyingTextDescent() {
        return applyTextDescent;
    }

    /**
     * sets the maximum percentage of the text relative to the height.
     *
     * @param temp the percentage that the height of the text is going to take;
     *             it is calculated as textSize/h. Has to be a value that is less
     *             than 1.0f.
     * @return this contextual instance. Useful for chained mutation
     */
    public Contextual setMaxTextPercentage(float temp) {
        this.maxTextPercentage = temp > 1.0f ? 1.0f : temp;
        return this;
    }


    /**
     * key pressed call back listener. only executes once
     *
     * @since April 30th
     */
    public void keyPressed() {
        for (EventListener eventListener : getEventListeners()) {
            if (eventListener.getEvent().equals(Event.KEY_PRESSED))
                eventListener.invoke();
        }
    }

    /**
     * key released call back listener. only executes once
     *
     * @since April 30th
     */
    public void keyReleased() {
        for (EventListener eventListener : getEventListeners()) {
            if (eventListener.getEvent().equals(Event.KEY_RELEASED))
                eventListener.invoke();
        }
    }

    /**
     * key held call back listener. executes continuously
     *
     * @since April 30th
     */
    @Override
    public void run() {
        super.run();
        if (getParent().keyPressed) {
            getEventListeners().forEach(eventListener -> {
                if (eventListener.getEvent().equals(Event.KEY_HELD))
                    eventListener.invoke();
            });
        }
    }

    public int getMouseOverTextColor() {
        return mouseOverTextColor;
    }

    public int getMousePressedTextColor() {
        return mousePressedTextColor;
    }

    /**
     * TODO: to be implemented
     *
     * @param textIndent
     * @return
     */
    public Contextual setTextIndent(float textIndent) {
        this.textIndent = textIndent;
        return this;
    }

    @Override
    public Contextual inheritStyle(Displayable other) {
        super.inheritStyle(other);
        if (other instanceof Contextual)
            this.setTextStyle(((Contextual) other).textStyle);
        return this;
    }

    @Override
    public Contextual inheritOutlook(Displayable other) {
        super.inheritOutlook(other);
        if (!(other instanceof Contextual)) return this;
        this.setTextColor(((Contextual) other).textColor);
        this.setMouseOverTextColor(((Contextual) other).mouseOverTextColor);
        this.setMousePressedTextColor(((Contextual) other).mousePressedTextColor);
        this.setAutoTextDescentCompensation(((Contextual) other).autoTextDescentCompensation);
        this.setMaxTextPercentage(((Contextual) other).maxTextPercentage);
        this.setTextFont(((Contextual) other).font);
        this.setAlign(((Contextual) other).alignment);
        return this;
    }
}