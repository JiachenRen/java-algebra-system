package jui_lib;

import processing.core.PApplet;
import processing.core.PConstants;

import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

/**
 * The layout of the displayables using HBox ADT would result in horizontal arrangement.
 * Never instantiate this class as relative if you are using it at the first level, a.k
 * the top level container should never be relative. Assign it with a pair of absolute
 * position and dimension.
 */
public class HBox extends Container {

    public HBox(float x, float y, float w, float h) {
        super(x, y, w, h);
        setAlignH(LEFT);
    }

    public HBox(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        setAlignH(LEFT);
    }

    public HBox() {
        super();
        setAlignH(LEFT);
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
                displayable.setWidth(0);
                continue;
            }
            displayable.setHeight((this.getHeight() - marginY * 2) * displayable.getRelativeH());
            displayable.setWidth(availableWidth() * displayable.getRelativeW());
        }
        int remaining = collapseInvisible ? visibleDisplayables() - num : displayables.size() - num;
        if (remaining <= 0) return;
        float w = undeclaredSpace() / (float) remaining;
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || !displayable.isUndeclared()) continue;
            if (shouldCollapse(displayable)) continue;
            displayable.setHeight(this.getHeight() - marginY * 2);
            displayable.setWidth(w);
        }
    }

    /**
     * this method arranges the Displayable objects that this HBox contains according to their dimension;
     * there are multiple available options for alignment. Note that this method should only be invoked
     * each time if and only if the height and width of the displayables are properly set, possibly by
     * calling syncSize();
     */
    public void arrange() {
        float cur_y;
        float cur_x = alignH == RIGHT ? this.getX() + getWidth() - marginX : this.getX() + marginX;
        for (Displayable displayable : displayables) {
            if (shouldCollapse(displayable))
                continue;
            switch (alignV) {
                case PConstants.UP:
                    cur_y = this.getY() + marginY;
                    break;
                case PConstants.DOWN:
                    cur_y = this.getY() + getHeight() - marginY - displayable.getHeight();
                    break;
                case PConstants.CENTER:
                    float temp = displayable.getHeight() / 2.0f;
                    cur_y = this.getY() + getHeight() / 2.0f - temp;
                    break;
                default:
                    cur_y = this.getY() + marginY;
            }
            switch (alignH) {
                case LEFT:
                    displayable.relocate(cur_x, cur_y);
                    cur_x += displayable.getWidth() + spacing;
                    break;
                case RIGHT:
                    cur_x -= displayable.getWidth();
                    displayable.relocate(cur_x, cur_y);
                    cur_x -= spacing;
                    break;
                default:
                    PApplet.println(id + ": default alignment applied.");
                    displayable.relocate(cur_x, cur_y);
                    cur_x += displayable.getWidth() + spacing;
                    break;
            }
        }
    }

    /**
     * @return the available space for formatting the displayables. (minus all spacing and margins)
     * @since April 25th differentiated between collapse invisible and retain space.
     */
    private float availableWidth() {
        /*modified Jan 26th by Jiachen Ren*/
        int sp = collapseInvisible ? visibleDisplayables() : displayables.size(); // occurrence of spacing
        return this.getWidth() - (sp - 1) * spacing - 2 * marginX;
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
        if (w <= 0) return -1;
        float occupied = 0;
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || displayable.isUndeclared()) continue;
            if (shouldCollapse(displayable))
                continue;
            occupied += availableWidth() * displayable.getRelativeW();
        }
        if (occupied > availableWidth()) {
            String errorMessage = ": no enough space for all declared displayables." +
                    " Check relative width assignments and make sure that the sum doesn't exceed 1.0f";
            System.err.println(id + errorMessage);
            float relativeVal[] = printDisplayables();
            System.err.println("occupied relative width: " + relativeVal[1]);
            return -1;
        }
        return availableWidth() - occupied;
    }
}
