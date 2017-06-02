package jui_lib;

import processing.core.PApplet;
import processing.core.PConstants;

/*
created Jan 22. Designed by Jiachen Ren. All Rights Reserved.
still needs to be implemented: confine the size of the bar, the roller cannot go over;
VSlider, a bunch of accessors and mutators.
adjust roller width
code refactored Jan 29th. VSlider Class created.
Idea Jan 29th: add slider progress bar.
code refactored Feb 4th. Added progress bar modified for both VSlider and VSlider.
*/
public abstract class Slider extends Displayable implements MouseControl, Scalable {
    public float barScalingFactor = 1;
    public boolean snapToGrid, displayGrid, displayNumericScale;
    public boolean isLockedOn;
    public Roller roller;
    public float valueLow, valueHigh;
    public float gridInterval;
    public float barWidth, barHeight;
    public int rollerBackgroundColor;
    public boolean rollerVisible = false;
    //create getter/setter for the following:
    public int gridDotSize = 2;
    public int gridDotColor = contourColor;
    private float rollerScalingWidth;
    private float rollerScalingHeight;
    private float rollerScalingRadius;
    float val;
    private boolean isDisplayingProgress;
    private int progressBackgroundColor;
    public Runnable onFocusMethod;

    public Slider(float x, float y, float w, float h) {
        super( x, y, w, h);
        init();
    }

    public Slider(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public Slider() {
        super();
        init();
    }

    public boolean mouseOverBar() {
        if (!mouseOverRoller()) {
            if (getParent().mouseX <= x + w && getParent().mouseX >= x)
                if (getParent().mouseY <= y + h / 2 + barHeight / 2 && getParent().mouseY >= y + h / 2 - barHeight / 2)
                    return true;
        }
        return false;
    }

    public boolean mouseOverRoller() {
        if (roller.isMouseOver()) return true;
        return false;
    }

    /*TODO should I keep this here?*/
    public abstract void mouseDragged();

    /**
     * @since May 2nd
     * the mouseOverRoller() || mouseOverRoller() is changed for mouseOverBar() for better interface experience.
     * this way it supports click and dragging simultaneously as opposed to discretely.
     */
    public void mousePressed() {
        super.mousePressed();
        if (mouseOverBar()||mouseOverRoller())
            isLockedOn = true;
    }

    public void mouseReleased() {
        super.mouseReleased();
        isLockedOn = false;
    }

    public Slider setRange(float low, float high) {
        if (high > low) {
            valueLow = low;
            valueHigh = high;
        } else {
            valueLow = high;
            valueHigh = low;
            System.err.println("Error: range out of bound. High must be larger than low. Not recommended");
        }
        return this;
    }

    public Slider setRollerShape(int shape) {
        roller.setShape(shape);
        return this;
    }

    public Slider setSnapToGrid(boolean temp) {
        snapToGrid = temp;
        return this;
    }

    public Slider setGridInterval(float val) {
        gridInterval = val;
        return this;
    }

    public Slider setGridVisible(boolean temp) {
        displayGrid = temp;
        return this;
    }

    public Slider setDisplayNumericScale(boolean temp) {
        displayNumericScale = temp;
        return this;
    }

    public Slider setRollerBackgroundColor(int c) {
        rollerBackgroundColor = c;
        return this;
    }

    public Slider setRollerBackgroundColor(int r, int g, int b) {
        rollerBackgroundColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public Slider setRollerBackgroundColor(int r, int g, int b, int t) {
        rollerBackgroundColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public Slider onFocus(Runnable r) {
        onFocusMethod = r;
        return this;
    }

    public abstract void drawGrid();

    public abstract Slider setScalingFactor(float num);

    public void display() {
        //for both the slider bar and the roller
        if (onFocusMethod != null && isLockedOn) {
            onFocusMethod.run();
        }
        this.applyContourStyle();

        //drawing the slider bar
        getParent().pushStyle();
        getParent().rectMode(PConstants.CENTER);
        getParent().fill(backgroundColor);
        if (isRounded) {
            getParent().rect(x + w / 2, y + h / 2, barWidth, barHeight, rounding);
            if (isDisplayingProgress) {
                getParent().fill(progressBackgroundColor);
                if (this.getClass().getSimpleName().equals("HSlider"))
                    getParent().rect(x + (roller.x - x) / 2, y + h / 2, roller.x - x, barHeight, rounding);
                else
                    getParent().rect(x + w / 2, y + (roller.y - y) / 2 + barHeight / 2, barWidth, y + barHeight - roller.y, rounding);

            } // modified Feb 4th.
        } else {
            getParent().rect(x + w / 2, y + h / 2, barWidth, barHeight);
            if (isDisplayingProgress) {
                getParent().fill(progressBackgroundColor);
                if (this.getClass().getSimpleName().equals("HSlider"))
                    getParent().rect(x + (roller.x - x) / 2, y + h / 2, roller.x - x, barHeight);
                else
                    getParent().rect(x + w / 2, y + (roller.y - y) / 2 + barHeight / 2, barWidth, y + barHeight - roller.y);
            }
            //this is why math is important!!! Feb 4th.
        }
        getParent().popStyle();

        //drawing the dot grid
        if (displayGrid) drawGrid();

        //for roller only
        if (!rollerVisible) return;
        if (roller.isMouseOver()) {
            getParent().fill(getParent().mousePressed ? mousePressedBackgroundColor : mouseOverBackgroundColor);
        } else {
            getParent().fill(isLockedOn ? mousePressedBackgroundColor : rollerBackgroundColor);
        }
        roller.display();
    }

    public int getIntValue() {
        return Math.round(getFloatValue());
    }

    public abstract float getFloatValue();


    public Slider setValue(float val) {
        if (val < valueLow || val > valueHigh)
            return this;
        this.val = val;
        updateRollerPos();
        return this;
    }//idea Feb 4th, fixed April 22.

    /*added April 22nd*/
    public abstract void updateRollerPos();

    public abstract void syncSettings();

    public void init() {
        roller = new Roller(x, y);
        barWidth = w;
        setScalingFactor(barScalingFactor);
        //setRounded(true);
        rollerBackgroundColor = backgroundColor;
        setProgressBackgroundColor(mouseOverBackgroundColor);
        setDisplayingProgress(true); // created Feb 4th. [optional]
    }

    public float getRollerScalingWidth() {
        return rollerScalingWidth;
    }

    public Slider setRollerScalingWidth(float rollerScalingWidth) {
        this.rollerScalingWidth = rollerScalingWidth;
        return this;
    }

    public float getRollerScalingHeight() {
        return rollerScalingHeight;
    }

    public Slider setRollerScalingHeight(float rollerScalingHeight) {
        this.rollerScalingHeight = rollerScalingHeight;
        return this;
    }

    public float getRollerScalingRadius() {
        return rollerScalingRadius;
    }

    public Slider setRollerScalingRadius(float rollerScalingRadius) {
        this.rollerScalingRadius = rollerScalingRadius;
        return this;
    }

    public boolean isDisplayingProgress() {
        return isDisplayingProgress;
    }

    public Slider setDisplayingProgress(boolean displayingProgress) {
        isDisplayingProgress = displayingProgress;
        return this;
    }

    public int getProgressBackgroundColor() {
        return progressBackgroundColor;
    }

    public Slider setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
        return this;
    }

    public class Roller {
        int shape;
        public float x, y;
        float r, w, h; // r is only available when using ELLIPSE

        Roller(float x, float y) {
            w = 20;
            h = 10;
            r = 10;

            shape = PConstants.ELLIPSE;
            this.x = x;
            this.y = y;
            //other options are RECT, ...
        }

        public void setShape(int shape) {
            this.shape = shape;
            syncSettings();
        }

        public boolean isMouseOver() {
            switch (shape) {
                case PConstants.ELLIPSE:
                    if (PApplet.dist(JNode.getParent().mouseX, JNode.getParent().mouseY, x, y) <= r)
                        return true;
                    break;
                case PConstants.RECT:
                    if (JNode.getParent().mouseX <= x + w / 2 && JNode.getParent().mouseX >= x - w / 2)
                        if (JNode.getParent().mouseY <= y + h / 2 && JNode.getParent().mouseY >= y - h / 2)
                            return true;
                    break;
            }
            return false;
        }

        //make accessible for the users??
        public void setRect(float w, float h) {
            this.w = w;
            this.h = h;
        }

        public void setEllipse(float r) {
            this.r = r;
        }

        public void display() {
            getParent().pushStyle();
            switch (shape) {
                case PConstants.ELLIPSE:
                    getParent().ellipseMode(PConstants.CENTER);

                    getParent().ellipse(x, y, r * 2, r * 2);
                    break;
                case PConstants.RECT:
                    getParent().rectMode(PConstants.CENTER);
                    if (isRounded) {
                        getParent().rect(x, y, w, h, rounding);
                    } else {
                        getParent().rect(x, y, w, h);
                    }
                    break;
            }
            getParent().popStyle();
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    /**
     * This method is overridden so that each component of the slider would be
     * resized properly according to the dimension of this displayable instance.
     * @param w the new width for the slider
     * @param h the new height for the slider.
     */
    @Override
    public void resize(float w, float h) {
        super.resize(w, h);
        syncSettings();
        updateRollerPos(); //April 22nd
    }

    /**
     * The relocation mechanism is overridden so that the change that need
     * to be made to the position of the roller could also be taken into
     * consideration.
     * @param x x-coordinate on screen.
     * @param y y-coordinate on screen.
     */
    @Override
    public void relocate(float x, float y) {
        super.relocate(x, y);
        roller.x = x;
        roller.y = y;
        syncSettings(); // modified Jan 26th
        updateRollerPos(); //Feb 4th
    }

    /**
     * Even if the roller is not visible, it would still be in place.
     * @param rollerVisible boolean value
     * @return this instance of slider
     */
    public Slider setRollerVisible(boolean rollerVisible) {
        this.rollerVisible = rollerVisible;
        return this;
    }
}
