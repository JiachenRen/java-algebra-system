package jui_lib;

import processing.core.PConstants;

public class HSlider extends Slider implements MouseControl {
    {
        setRollerScalingHeight(1.5f);
        setRollerScalingWidth(.5f);
        setRollerScalingRadius(.6f);
        syncSettings();
    }

    public HSlider(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public HSlider(float relativeW, float relativeH) {
        super(relativeW, relativeH);
    }

    public HSlider() {
        super();
    }

    public void syncSettings() {
        barHeight = (int) (h * barScalingFactor);
        if (roller.shape == PConstants.RECT) roller.x = x + roller.w / 2;
        else roller.x = x + roller.r;
        roller.y = y + h / 2;
        roller.setEllipse(barHeight * getRollerScalingRadius());
        roller.setRect(barHeight * getRollerScalingWidth(), barHeight * getRollerScalingHeight());
        gridDotSize = (int) (barHeight / 3);
        barWidth = w;
    }

    //TODO, to be implemented
    public void updateRollerPos() {
        if (val > valueHigh || val < valueLow) {
            if (val != valueLow && val != valueHigh)
                System.err.println("ERROR: slider value cannot be set to " + val + "," +
                        " out of range(" + valueLow + "->" + valueHigh + ")");
            return;
        }
        float temp = roller.shape == PConstants.ELLIPSE ? roller.r * 2 : roller.w;
        float offset = (val - valueLow) / (valueHigh - valueLow) * (barWidth - temp);
        roller.setX(this.x + temp / 2.0f + offset);
    }

    public HSlider setScalingFactor(float temp) {
        barScalingFactor = temp;
        syncSettings();
        return this;
    }


    public void mouseDragged() {
        if (isLockedOn)
            roller.x = JNode.getParent().mouseX;
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                roller.x = roller.x < x + roller.r ?
                        x + roller.r : roller.x;
                roller.x = roller.x > x + w - roller.r ?
                        x + w - roller.r : roller.x;
                break;
            case PConstants.RECT:
                roller.x = roller.x < x + roller.w / 2 ?
                        x + roller.w / 2 : roller.x;
                roller.x = roller.x > x + w - roller.w / 2 ?
                        x + w - roller.w / 2 : roller.x;
                break;
        }
    }

    public void drawGrid() {
        //not yet implemented!!! draw a circle if ELLIPSE, rect otherwise.
        getParent().pushStyle();
        getParent().strokeWeight(gridDotSize);
        getParent().stroke(gridDotColor);
        float d = 0;
        float curX = 0, curY = y + getHeight() / 2;
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                curX = x + roller.r;
                d = gridInterval / (valueHigh - valueLow) * (w - roller.r * 2);
                break;
            case PConstants.RECT:
                curX = x + roller.w / 2;
                d = gridInterval / (valueHigh - valueLow) * (w - roller.w);
                break;
        }
        while (curX < (roller.shape == PConstants.ELLIPSE ? x + w - roller.r : x + w - roller.w / 2)) {
            getParent().point(curX, curY);
            curX += d;
        }
        getParent().popStyle();
    }

    public float getFloatValue() {
        float val = 0.0f;
        switch (roller.shape) {
            case PConstants.ELLIPSE:
                val = (roller.x - x - roller.r) / ((w) - roller.r * 2) * (valueHigh - valueLow) + valueLow;
                break;
            case PConstants.RECT:
                val = ((float) ((roller.x - x - roller.w / 2.0) / (w - roller.w) * (valueHigh - valueLow))) + valueLow;
                break;
        }
        val = val < valueLow ? valueLow : val;
        return val > valueHigh ? valueHigh : val; //fixed Jan 25
    }

    @Override //moved from Slider to VSlider. April 22nd
    public void mousePressed() {
        super.mousePressed();
        if (mouseOverBar()) {
            roller.x = JNode.getParent().mouseX;
        }
        if (mouseOverRoller() && onFocusMethod != null)
            onFocusMethod.run();
    }
}