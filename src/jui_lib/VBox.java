package jui_lib;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * The layout of the displayables using HBox ADT would result in horizontal arrangement.
 * Never instantiate this class as relative if you are using it at the first level, a.k
 * the top level container should never be relative. Assign it with a pair of absolute
 * position and dimension.
 */
public class VBox extends Container {

    public VBox(float x, float y, float w, float h) {
        super(x, y, w, h);
        setAlignV(PConstants.DOWN);
    }

    public VBox(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        setAlignV(PConstants.DOWN);
    }

    public VBox() {
        super();
        setAlignV(PConstants.DOWN);
    }

    /**
     * This method along with arrange() are the hearts of JUI. They are the things that
     * differ JUI from all of the other user interface libraries in processing. The width
     * and height of each individual displayable objects are calculated from their relative
     * width and height designated by the user or automatically according to the available
     * space left in the container. This offers tremendous benefits over the conventional
     * ui design routines in processing where the absolute size and location of a UI obj is
     * used as opposed to relative position. Relative positioning of UI objects ensures that
     * it works no matter how large the display is; and it doesn't matter if the window gets
     * resized. It also offers tremendous convenience and makes the code more manageable as
     * the UIs are wrapped into one another with a container structure.
     */
    public void syncSize() {
        int num = 0;
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || displayable.isUndeclared())
                continue;
            if (shouldCollapse(displayable))
                continue;
            num++;
            if (undeclaredSpace() == -1 && getWidth() >= 0) {
                displayable.setHeight(0);
                continue;
            }
            displayable.setWidth((this.getWidth() - marginX * 2) * displayable.getRelativeW());
            displayable.setHeight(availableHeight() * displayable.getRelativeH());
        }
        int remaining = collapseInvisible ? visibleDisplayables() - num : displayables.size() - num;
        if (remaining <= 0) return;
        float h = undeclaredSpace() / (float) remaining;
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || !displayable.isUndeclared()) continue;
            if (shouldCollapse(displayable)) continue;
            displayable.setWidth(this.getWidth() - marginX * 2);
            displayable.setHeight(h);
        }
    }

    /**
     * this method arranges the Displayable objects that this VBox contains according to their dimension;
     * there are multiple available options for alignment. Note that this method should only be invoked
     * each time if and only if the height and width of the displayables are properly set, possibly by
     * calling syncSize();
     *
     * @since April 29th Vertical Align UP fixed.
     */
    public void arrange() {
        float cur_x;
        float cur_y = alignV == PConstants.UP ? this.getY() + getHeight() - marginY : this.getY() + marginY;
        for (Displayable displayable : displayables) {
            if (shouldCollapse(displayable))
                continue;
            switch (alignH) {
                case PConstants.LEFT:
                    cur_x = this.getX() + marginX;
                    break;
                case PConstants.RIGHT:
                    cur_x = this.getX() + getWidth() - marginX - displayable.getWidth();
                    break;
                case PConstants.CENTER:
                    float temp = displayable.getWidth() / 2.0f;
                    cur_x = this.getX() + getWidth() / 2.0f - temp;
                    break;
                default:
                    cur_x = this.getX() + marginX;
            }
            switch (alignV) {
                case PConstants.DOWN:
                    displayable.relocate(cur_x, cur_y);
                    cur_y += displayable.getHeight() + spacing;
                    break;
                case PConstants.UP:
                    cur_y -= displayable.getHeight();
                    displayable.relocate(cur_x, cur_y);
                    cur_y -= spacing;
                    break;
                default:
                    PApplet.println(id + ": default alignment applied.");
                    displayable.relocate(cur_x, cur_y);
                    cur_y += displayable.getHeight() + spacing;
                    break;
            }
        }
    }

    /**
     * @return the available height(raw) remained in this VBox.
     * @since April 29th code refactored to accommodate collapseInvisible();
     * the available height is calculated as container height - margins - all spacings.
     */
    private float availableHeight() {
        int sp = collapseInvisible ? visibleDisplayables() : displayables.size();
        return this.getHeight() - (sp - 1) * spacing - 2 * marginY;
    }

    /**
     * calculates the percentage of space left in the container; the user claim certain
     * percentage of the total available space in the container by assigning a designated
     * displayable instance with relative height and relative width.
     *
     * @return the total space - declared space(refers to the displayables that have
     * been assigned relative width and relative height.
     */
    public float undeclaredSpace() {
        if (h <= 0) return -1;
        float occupied = 0;
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || displayable.isUndeclared()) continue;
            if (shouldCollapse(displayable))
                continue;
            occupied += availableHeight() * displayable.getRelativeH();
        }
        if (occupied > availableHeight()) {
            String errorMessage = ": no enough space for all declared displayables." +
                    " Check relative height assignments and make sure that the sum doesn't exceed 1.0f";
            System.err.println(id + errorMessage);
            float relativeVal[] = printDisplayables();
            System.err.println("occupied relative height: " + relativeVal[1]);
            return -1;
        }
        return availableHeight() - occupied;
    }
}