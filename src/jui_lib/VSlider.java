package jui_lib;

import processing.core.PConstants;

public class VSlider extends Slider implements MouseControl {

    {
        setRollerScalingHeight(.5f);
        setRollerScalingWidth(1.5f);
        setRollerScalingRadius(.6f);
        syncSettings();
    }

    public VSlider(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public VSlider(float relativeW, float relativeH) {
        super(relativeW, relativeH);
    }

    public VSlider(String id) {
        super();
    }

    /**
     * Jan 4th, Roller Shape Ellipse needs to be added
     * updates the position of the roller.
     *
     * @since April 24th roller ellipse shape is considered.
     */
    public void updateRollerPos() {
        if (val > valueHigh || val < valueLow) {
            if (val != valueLow && val != valueHigh)
                System.err.println(id + " : slider value cannot be set to " + val + ", out of range(" + valueLow + "->" + valueHigh + ")");
            return;
        }
        float temp = roller.shape == PConstants.ELLIPSE ? roller.r * 2 : roller.h;
        float offset = (val - valueLow) / (valueHigh - valueLow) * (barHeight - temp);
        roller.setY(this.y + barHeight - temp / 2.0f - offset);
    }


    /**
     * @since May 2nd bug fixes. A bunch of unnecessary int casts were taken out
     * Synchronizes the appearance and dimension of the rectangular slider bar and
     * the roller according to the new dimension of the displayable object.
     */
    public void syncSettings() {
        barWidth = w * barScalingFactor;
        if (roller.shape == PConstants.RECT) roller.y = y + h - roller.h / 2;
        else roller.y = y + h - roller.r;
        roller.x = x + w / 2;
        roller.setEllipse(barWidth * getRollerScalingRadius());
        roller.setRect(barWidth * getRollerScalingWidth(), barWidth * getRollerScalingHeight());
        barHeight = h;
    }

    public VSlider setScalingFactor(float temp) {
        barScalingFactor = temp;
        syncSettings();
        return this;
    }

    public void mouseDragged() {
        if (isLockedOn) {
            roller.y = getParent().mouseY;
            roller.x = x + w / 2;
        }
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                roller.y = roller.y < y + roller.r ? y + roller.r : roller.y;
                roller.y = roller.y > y + h - roller.r ? y + h - roller.r : roller.y;
                break;
            case PConstants.RECT:
                roller.y = roller.y < y + roller.h / 2 ? y + roller.h / 2 : roller.y;
                roller.y = roller.y > y + h - roller.h / 2 ? y + h - roller.h / 2 : roller.y;
                break;
        }
    }

    @Override
    public void mousePressed() {
        super.mousePressed();
        if (mouseOverBar())
            roller.y = getParent().mouseY;
        if (mouseOverRoller() && onFocusMethod != null)
            onFocusMethod.run();
    }

    public void drawGrid() {
        getParent().pushStyle();
        getParent().strokeWeight(gridDotSize);
        getParent().stroke(gridDotColor);
        float d = 0;
        float curX = x + getWidth() / 2, curY = 0;
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                curY = y + roller.r;
                d = gridInterval / (valueHigh - valueLow) * (h - roller.r * 2);
                break;
            case PConstants.RECT:
                curY = y + roller.h / 2;
                d = gridInterval / (valueHigh - valueLow) * (h - roller.h);
                break;
        }
        while (curY < (roller.shape == PConstants.ELLIPSE ? y + h - roller.r : y + h - roller.h / 2)) {
            getParent().point(curX, curY);
            curY += d;
        }
        getParent().popStyle();
    }

    public float getFloatValue() {
        float val = 0.0f;
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                val = ((y + h - roller.r) - roller.y) / (h - roller.r * 2) * (valueHigh - valueLow) + valueLow;
                break;
            case PConstants.RECT:
                val = ((y + h - roller.h / 2) - roller.y) / (h -
                        roller.h) * (valueHigh - valueLow) + valueLow;
                break;
        }
        val = val < valueLow ? valueLow : val;
        return val > valueHigh ? valueHigh : val;
    }

}