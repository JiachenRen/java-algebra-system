package jui;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.*;
import java.util.ArrayList;

//code refactored Jan 18,the Displayable interface is changed into a superclass. Modified by Jiachen Ren
//add setBackground. Task completed. Background enum added April 22nd.
//modified April 22nd. Took me half an hour, I eliminated all rounding errors for containers!
//primitive type for coordinate and dimension is changed from int to float. Proved to be helpful!
//refresh requesting technique applied April 23rd
//TODO: add inheritStyle(), clone();

/**
 * add mousePressedTextColor(), mousePressedContourColor(), mouseOverTextColor(), mouseOverContourColor();
 * completed April 30th.
 */
public class Displayable implements MouseControl, Serializable {
    public boolean displayContour = JNode.DISPLAY_CONTOUR;
    public boolean isVisible = true;

    public int colorMode = JNode.COLOR_MODE;
    public int backgroundColor = JNode.BACKGROUND_COLOR;
    public int mouseOverBackgroundColor = JNode.MOUSE_OVER_BACKGROUND_COLOR;
    public int mousePressedBackgroundColor = JNode.MOUSE_PRESSED_BACKGROUND_COLOR;
    public int contourColor = JNode.CONTOUR_COLOR;
    public int mousePressedContourColor = JNode.MOUSE_PRESSED_CONTOUR_COLOR;
    public int mouseOverContourColor = JNode.MOUSE_OVER_CONTOUR_COLOR;

    public float contourThickness = JNode.CONTOUR_THICKNESS;
    public float rounding = JNode.ROUNDING;

    public float x, y, w, h;
    public float relativeW = 1, relativeH = 1;

    public JStyle backgroundStyle = JStyle.CONSTANT;
    public JStyle contourStyle = JStyle.CONSTANT;
    public ImgStyle imgStyle = ImgStyle.RESERVED;

    public PImage backgroundImg;
    private Runnable attachedMethod;

    private boolean refreshRequested;

    public boolean isRounded = JNode.ROUNDED;
    public boolean isRelative, isUndeclared;

    public String id;

    private ArrayList<EventListener> eventListeners;

    private boolean mouseIsInScope;

    {
        eventListeners = new ArrayList<>();
    }

    public enum ImgStyle {
        RESERVED, STRETCH
    }

    public Displayable(float relativeW, float relativeH) {
        setRelativeW(relativeW);
        setRelativeH(relativeH);
        this.isRelative = true;
    }

    public Displayable() {
        this.isRelative = true;
        this.isUndeclared = true;
    }

    public Displayable(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean isMouseOver() {
        return isInScope(getParent().mouseX, getParent().mouseY);
    }

    public boolean isInScope(float px, float py) {
        return px >= x && (px <= x + w && (py >= y && (py <= y + h)));
    }

    //public boolean isAboveScope

    public boolean isUndeclared() {
        return isUndeclared;
    }

    public Displayable setUndeclared(boolean temp) {
        isUndeclared = temp;
        return this;
    }

    public Displayable setRounded(boolean temp) {
        isRounded = temp;
        return this;
    }

    public Displayable setRounding(float temp) {
        rounding = temp;
        //setRounded(true); removed April 26th.
        return this;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public float getRelativeW() {
        return relativeW;
    }

    public float getRelativeH() {
        return relativeH;
    }

    public Displayable setRelative(boolean temp) {
        isRelative = temp;
        refreshRequested = true;
        return this;
    }

    public Displayable setRelativeW(float temp) {
        relativeW = temp;
        isUndeclared = false;
        refreshRequested = true;/*this might take long. Consider optimization.*/
        return this;
    }

    public Displayable setRelativeH(float temp) {
        relativeH = temp;
        isUndeclared = false;
        refreshRequested = true;
        return this;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isDisplayingContour() {
        return displayContour;
    }

    public Displayable setVisible(boolean temp) {
        isVisible = temp;
        return this;
    }

    public Displayable setId(String temp) {
        id = temp;
        return this;
    }

    public String getId() {
        return id;
    }

    public float[] getDimension() {
        return new float[]{w, h};
    }

    public float[] getCoordinate() {
        return new float[]{x, y};
    }

    public float getWidth() {
        return w;
    }

    public float getHeight() {
        return h;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Displayable setContourColor(int r, int g, int b) {
        contourColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Displayable setContourColor(int c) {
        contourColor = c;
        return this;
    }

    public Displayable setContourColor(int r, int g, int b, int t) {
        contourColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setMousePressedContourColor(int r, int g, int b) {
        mousePressedContourColor = JNode.getParent().color(r, g, b);

        return this;
    }

    public Displayable setMousePressedContourColor(int c) {
        mousePressedContourColor = c;
        return this;
    }

    public Displayable setMousePressedContourColor(int r, int g, int b, int t) {
        mousePressedContourColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setMouseOverContourColor(int r, int g, int b) {
        mouseOverContourColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Displayable setMouseOverContourColor(int c) {
        mouseOverContourColor = c;
        return this;
    }

    public Displayable setMouseOverContourColor(int r, int g, int b, int t) {
        mouseOverContourColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setContourThickness(float thickness) {
        contourThickness = thickness;
        return this;
    }

    public Displayable setContourVisible(boolean temp) {
        displayContour = temp;
        return this;
    }

    public Displayable setBackgroundColor(int r, int g, int b) {
        backgroundColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Displayable setBackgroundColor(int c) {
        backgroundColor = c;
        return this;
    }

    public Displayable setBackgroundColor(int color, int alpha) {
        backgroundColor = getParent().color(getParent().red(color), getParent().green(color), getParent().blue(color), alpha);
        return this;
    }

    public Displayable setBackgroundColor(int r, int g, int b, int t) {
        backgroundColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setMouseOverBackgroundColor(int r, int g, int b, int t) {
        mouseOverBackgroundColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setMouseOverBackgroundColor(int c) {
        mouseOverBackgroundColor = c;
        return this;
    }

    public Displayable setMouseOverBackgroundColor(int r, int g, int b) {
        mouseOverBackgroundColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Displayable setMousePressedBackgroundColor(int r, int g, int b, int t) {
        mousePressedBackgroundColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Displayable setMousePressedBackgroundColor(int c) {
        mousePressedBackgroundColor = c;
        return this;
    }

    public Displayable setMousePressedBackgroundColor(int r, int g, int b) {
        mousePressedBackgroundColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Displayable setColorMode(int colorMode) {
        this.colorMode = colorMode;
        return this;
    }

    /*modified March 8th*/
    public Displayable attachMethod(Runnable runnable) {
        attachedMethod = runnable;
        return this;
    }

    public Runnable getAttachedMethod() {
        return attachedMethod;
    }

    public void run() {
        display();
        if (attachedMethod != null) {
            attachedMethod.run();
        }
        updateEventListeners();
    }

    private void updateEventListeners() {
        if (eventListeners.size() == 0) return;
        if (isMouseOver()) {
            activateEventListeners(Event.MOUSE_OVER);
            if (!mouseIsInScope) {
                mouseIsInScope = true;
                activateEventListeners(Event.MOUSE_ENTERED);
            }
        } else {
            if (mouseIsInScope) {
                mouseIsInScope = false;
                activateEventListeners(Event.MOUSE_LEFT);
            }
        }
    }

    public void mousePressed() {
        if (isMouseOver())
            this.activateEventListeners(Event.MOUSE_PRESSED);
    }

    public void mouseDragged() {
        if (isMouseOver())
            this.activateEventListeners(Event.MOUSE_DRAGGED);
    }

    public void mouseHeld() {
        if (isMouseOver())
            this.activateEventListeners(Event.MOUSE_HELD);
    }

    public void mouseReleased() {
        if (isMouseOver())
            this.activateEventListeners(Event.MOUSE_RELEASED);
    }

    /**
     * TODO not working yet. Debug April 30th.
     */
    public void mouseWheel() {
        if (isMouseOver())
            this.activateEventListeners(Event.MOUSE_WHEEL);
    }

    public void activateEventListeners(Event event) {
        eventListeners.forEach(eventListener -> {
            if (eventListener.getEvent().equals(event))
                eventListener.invoke();
        });
    }

    public void applyContourStyle() {
        getParent().strokeWeight(contourThickness);
        switch (contourStyle) {
            case VOLATILE:
                if (displayContour) {
                    if (isMouseOver()) {
                        getParent().stroke(getParent().mousePressed ? mousePressedContourColor : mouseOverContourColor);
                    } else getParent().stroke(contourColor);
                } else {
                    getParent().noStroke();
                }
                break;
            case CONSTANT:
                if (displayContour) getParent().stroke(contourColor);
                else getParent().noStroke();
                break;
        }
    }

    public void applyBackgroundStyle() {
        switch (backgroundStyle) {
            case CONSTANT:
                getParent().fill(backgroundColor);
                break;
            case VOLATILE:
                if (isMouseOver()) {
                    getParent().fill(getParent().mousePressed ? mousePressedBackgroundColor : mouseOverBackgroundColor);
                } else {
                    getParent().fill(backgroundColor);
                }
                break;
            case DISABLED:
                break;
        }
    }

    public void drawRect() {
        getParent().rectMode(PConstants.CORNER);
        if (isRounded) {
            getParent().rect(x, y, w, h, rounding);
        } else {
            getParent().rect(x, y, w, h);
        }
    }

    public void displayImg() {
        if (backgroundImg != null) {
            getParent().imageMode(PConstants.CENTER);
            float tx = x + w / 2, ty = y + h / 2;
            switch (imgStyle) {
                case RESERVED:
                    float imgWidth = backgroundImg.width;
                    float imgHeight = backgroundImg.height;
                    if (imgWidth > w) {
                        float scale = w / imgWidth;
                        imgWidth = w;
                        imgHeight *= scale;
                    }
                    if (imgHeight > h) {
                        float scale = h / imgHeight;
                        imgHeight = h;
                        imgWidth *= scale;
                    }
                    getParent().image(backgroundImg, tx, ty, imgWidth, imgHeight);
                    break;
                case STRETCH:
                    break;
            }
        }
    }

    public void display() {
        /*default displaying method. Overriding recommended*/
        getParent().pushStyle();

        applyContourStyle();
        applyBackgroundStyle();
        drawRect();

        displayImg();

        getParent().popStyle();
    }

    public void relocate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void resize(float w, float h) {
        this.w = w;
        this.h = h;
    }

    boolean refreshRequested() {
        return refreshRequested;
    }

    void requestProcessed() {
        refreshRequested = false;
    }

    public Displayable setBackgroundStyle(JStyle backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
        return this;
    }

    public Displayable setContourStyle(JStyle contourStyle) {
        this.contourStyle = contourStyle;
        return this;
    }

    public Displayable setWidth(float temp) {
        resize(temp, h);
        return this;
    }

    public Displayable setHeight(float temp) {
        resize(w, temp);
        return this;
    }

    public Displayable setX(float temp) {
        relocate(temp, y);
        return this;
    }

    public Displayable setY(float temp) {
        relocate(x, temp);
        return this;
    }

    public PApplet getParent() {
        return JNode.getParent();
    }

    public PImage getBackgroundImg() {
        return backgroundImg;
    }

    public Displayable setBackgroundImg(PImage backgroundImg) {
        this.backgroundImg = backgroundImg;
        return this;
    }

    public ArrayList<EventListener> getEventListeners() {
        return eventListeners;
    }

    public ArrayList<EventListener> getEventListeners(Event event) {
        ArrayList<EventListener> matched = new ArrayList<>();
        eventListeners.forEach(eventListener -> {
            if (eventListener.getEvent().equals(event))
                matched.add(eventListener);
        });
        return matched;
    }

    public Displayable addEventListener(String id, Event event, Runnable attachedMethod) {
        EventListener eventListener = new EventListener(id, event);
        eventListener.attachMethod(attachedMethod);
        this.addEventListener(eventListener);
        return this;
    }

    public EventListener getEventListener(String id) {
        for (EventListener eventListener : eventListeners)
            if (eventListener.getId().equals(id))
                return eventListener;
        return null;
    }

    public Displayable removeEventListener(String id) {
        for (int i = eventListeners.size() - 1; i >= 0; i--) {
            if (eventListeners.get(i).getId().equals(id))
                eventListeners.remove(i);
        }
        return this;
    }

    public Displayable removeEventListeners(Event event) {
        for (int i = eventListeners.size() - 1; i >= 0; i--) {
            if (eventListeners.get(i).getEvent().equals(event))
                if (!eventListeners.get(i).getId().startsWith("@"))
                    eventListeners.remove(i);
        }
        return this;
    }

    public Displayable addEventListener(Event event, Runnable attachedMethod) {
        addEventListener("", event, attachedMethod);
        return this;
    }

    public Displayable addEventListener(EventListener eventListener) {
        this.eventListeners.add(eventListener);
        return this;
    }

    public Displayable inheritOutlook(Displayable other) {
        this.setContourVisible(other.displayContour);
        this.setBackgroundImg(other.backgroundImg);
        this.setBackgroundColor(other.backgroundColor);
        this.setMouseOverBackgroundColor(other.backgroundColor);
        this.setContourColor(other.contourColor);
        this.setMouseOverContourColor(other.mouseOverContourColor);
        this.setContourThickness(other.contourThickness);
        this.setColorMode(other.colorMode);
        this.setRounding(other.rounding);
        this.setRounded(other.isRounded);
        return this;
    }

    public Displayable inheritStyle(Displayable other) {
        this.backgroundStyle = other.backgroundStyle;
        this.contourStyle = other.contourStyle;
        this.imgStyle = other.imgStyle;
        return this;
    }

    public Displayable inheritDisplayProperties(Displayable other) {
        this.setVisible(other.isVisible);
        this.resize(other.w, other.h);
        this.setRelativeH(other.relativeH);
        this.setRelativeW(other.relativeW);
        this.setRelative(other.isRelative);
        this.setUndeclared(other.isUndeclared);
        return this;
    }

    /**
     * TODO: does not work for now.
     *
     * @return
     */
    public Displayable clone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Displayable) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}