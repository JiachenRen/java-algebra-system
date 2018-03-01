package jui;

import processing.core.PApplet;
import processing.core.PConstants;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

/**
 * Class ProgressBar. Written by Jiachen on 29/04/2017.
 */
public class ProgressBar extends Contextual implements Scalable {
    private float scalingFactor;
    private float widthScalingFactor;
    private float barHeight;
    private float barWidth;
    private float barY;
    private int progressBackgroundColor;
    private PApplet parent;
    private float percentageCompleted;
    private boolean completed;
    private boolean percentageVisible;
    private Style percentageTextStyle = Style.END;
    private Label percentageLabel;
    private String formattedPercentage;

    public enum Style {
        END, MIDDLE, LEFT,RIGHT
    }

    public ProgressBar(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
        resize(w, h);
        relocate(x, y);
    }

    public ProgressBar(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public ProgressBar() {
        super();
        init();
    }

    private void init() {
        percentageLabel = (Label) new Label().setVisible(false);
        setScalingFactor(0.6f);
        setPercentageVisible(true);
        setPercentageTextStyle(Style.END);
        percentageLabel.setAlign(CENTER);
        this.setAlign(CENTER);
        setProgressBackgroundColor(mouseOverBackgroundColor);
        parent = JNode.getParent();
        setCompletedPercentage(0.0f);
        setWidthScalingFactor(0.85f);
    }

    public ProgressBar setScalingFactor(float scale) {
        this.scalingFactor = scale;
        return this;
    }

    @Override
    public void display() {
        parent.pushStyle();
        applyContourStyle();
        applyBackgroundStyle();
        if (isRounded) parent.rect(x, barY, barWidth, barHeight, rounding);
        else parent.rect(x, barY, barWidth, barHeight);
        parent.popStyle();

        parent.fill(progressBackgroundColor);
        parent.noStroke();
        parent.rectMode(PConstants.CORNER);
        if (isRounded) parent.rect(x, barY, barWidth * percentageCompleted, barHeight, rounding);
        else parent.rect(x, barY, barWidth * percentageCompleted, barHeight);

        if (percentageVisible) {
            if (percentageTextStyle.equals(Style.END)) {

            } else if (percentageTextStyle.equals(Style.LEFT)) {

            }
            switch (percentageTextStyle){
                case END:
                    this.applyTextColor();
                    percentageLabel.displayRawText(getContent());
                    break;
                case MIDDLE:
                    setAlign(CENTER);
                    displayText(formattedPercentage);
                    break;
                case LEFT:
                    setAlign(LEFT);
                    displayText(formattedPercentage);
                    break;
                case RIGHT:
                    setAlign(RIGHT);
                    displayText(formattedPercentage);
                    break;
            }
        }
    }

    private String formatPercentage(float input) {
        input *= 100;
        String temp = Float.toString(input);
        if (!temp.contains(".")) return "%";
        else {
            int index = temp.indexOf(".") + 2;
            int t = temp.length();
            return temp.substring(0, index > t ? t : index) + "%";
        }
    }

    @Override
    public void resize(float w, float h) {
        super.resize(w, h);
        if (percentageTextStyle.equals(Style.END) && percentageVisible) {
            this.barWidth = w * widthScalingFactor;
        } else {
            this.barWidth = w;
        }
        this.barHeight = h * scalingFactor;
        /*TODO should the height of the label be h or barHeight?*/
        percentageLabel.resize(w * (1.0f - widthScalingFactor), h);
    }

    @Override
    public void relocate(float x, float y) {
        super.relocate(x, y);
        barY = y + h / 2 - barHeight / 2;
        percentageLabel.relocate(x + barWidth, y);
    }

    public ProgressBar setProgressBackgroundColor(int c) {
        progressBackgroundColor = c;
        return this;
    }

    public ProgressBar setProgressBackgroundColor(int r, int g, int b) {
        progressBackgroundColor = parent.color(r, g, b);
        return this;
    }

    public ProgressBar setProgressBackgroundColor(int r, int g, int b, int a) {
        progressBackgroundColor = parent.color(r, g, b, a);
        return this;
    }

    public ProgressBar setCompletedPercentage(float temp) {
        if (temp >= 1.0f) {
            temp = 1.0f;
            completed = true;
        }
        percentageCompleted = temp;
        formattedPercentage = formatPercentage(percentageCompleted);
        percentageLabel.setContent(formattedPercentage);
        return this;
    }

    public boolean hasCompleted() {
        return completed;
    }

    /**
     * only applicable if Style is .END
     */
    public ProgressBar setWidthScalingFactor(float scale) {
        scale = scale > 0.9f ? 0.9f : scale;
        widthScalingFactor = scale;
        resize(w, h);
        return this;
    }

    public ProgressBar setPercentageTextStyle(Style style) {
        this.percentageTextStyle = style;
        resize(w, h);
        return this;
    }

    public ProgressBar setPercentageVisible(boolean temp) {
        percentageVisible = temp;
        resize(w, h);
        return this;
    }

    @Override
    public ProgressBar setAlign(int align) {
        super.setAlign(align);
        return this;
    }

    public float getPercentageCompleted(){
        return percentageCompleted;
    }

    public Label getPercentageLabel() {
        return percentageLabel;
    }
}
