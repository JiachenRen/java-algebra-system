package jui;

import processing.core.PConstants;

import java.util.ArrayList;
import java.util.Arrays;

//code refactored Jan 20th
//idea: Jan 21th, granting both the VBox and HBox the ability to actualize both relative width and height.
//TODO April 23rd: add setCollapseInvisible() method
//TODO April 26th add display title option.
public abstract class Container extends Displayable {
    public ArrayList<Displayable> displayables;
    public boolean containerVisible = JNode.CONTAINER_VISIBLE; // the container is visible only if the objects it contains are visible.
    public float marginX = JNode.CONTAINER_MARGIN_X, marginY = JNode.CONTAINER_MARGIN_Y;
    public float spacing = JNode.CONTAINER_SPACING; //modified Jan 26th. Refactored April 26th.
    public int alignH, alignV;
    public boolean collapseInvisible;
    private boolean matchWindowDimension;

    {
        matchWindowDimension = false;
    }

    public Container(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public Container(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public Container() {
        super();
        init();
    }

    public void init() {
        displayables = new ArrayList<>();
        this.setAlign(PConstants.LEFT, PConstants.UP);
    }

    public void display() {
        if (containerVisible) {
            /*code cleaned up April 22nd*/
            super.display();
        }
        int i = 0, displayablesSize = displayables.size();
        while (i < displayablesSize) {
            Displayable displayable = displayables.get(i);
            if (displayable.isVisible())
                displayable.run();
            if (displayable.refreshRequested()) {
                displayable.requestProcessed();
                syncSize();
                arrange();
            }
            i++;
        }
        //listen to change in the window dimension.
        if (matchWindowDimension && (h != getParent().height || w != getParent().width)) {
            this.resize(getParent().width, getParent().height);
        }
    }

    public Container setContainerVisible(boolean temp) {
        containerVisible = temp;
        return this;
    }

    public Container setMarginX(float temp) {
        setMargins(temp, marginY);
        return this;
    }

    public Container setMarginY(float temp) {
        setMargins(marginX, temp);
        return this;
    }

    public Container setMargins(float marginX, float marginY) {
        this.marginX = marginX;
        this.marginY = marginY;
        syncSize();
        arrange();
        return this;
    }

    public Container setSpacing(float temp) {
        this.spacing = temp;
        syncSize();
        arrange();
        return this;
    }

    public Container setAlign(int horizontal, int vertical) {
        this.alignH = horizontal;
        this.alignV = vertical;
        arrange();
        return this;
    }

    public Container setAlignH(int horizontal) {
        this.alignH = horizontal;
        arrange();
        return this;
    }

    public Container setAlignV(int vertical) {
        this.alignV = vertical;
        arrange();
        return this;
    }

    /**
     * TODO this method should takes in account for space holders.
     *
     * @param temp the boolean in which collapseInvisible corresponds to.
     * @return this instance of Container.
     */
    public Container setCollapseInvisible(boolean temp) {
        collapseInvisible = temp;
        syncSize();
        arrange();
        return this;
    }

    public float getMarginX() {
        return marginX;
    }

    public float getMarginY() {
        return marginY;
    }

    public boolean containerIsVisible() {
        return containerVisible;
    }

    public abstract float undeclaredSpace();

    public Container add(Displayable d) {
        d.setRelative(true);
        if (!JNode.getDisplayables().contains(d))
            JNode.add(d);
        this.displayables.add(d);
        d.parent = this;
        syncSize();
        arrange();
        return this;
    }

    public abstract void syncSize();

    public abstract void arrange();

    public boolean contains(Displayable temp) {
        return displayables.contains(temp);
    }
    public boolean contains(String id) {
        for (Displayable d: displayables) {
            if (d.getId().equals(id))
                return true;
        }
        return false;
    }

    public Displayable get(String id) {
        for (Displayable d: displayables) {
            if (d.getId().equals(id))
                return d;
        }
        return null;
    }

    /**
     * Method refactored April 24th. Removed an error where an ArrayIndexOutOfBounds
     * would have been thrown. The method goes through all stacks of containers and
     * removes all of the displayable instances that qualify for removal, including
     * the sub-containers themselves.
     *
     * @param obj the generic displayable obj to be removed from the stack
     */
    public Container remove(Displayable obj) {
        if (displayables == null) return this; /*again, this is here to prevent an error thrown by Table!*/
        for (int i = displayables.size() - 1; i >= 0; i--) {
            Displayable displayable = displayables.get(i);
            if (displayable instanceof Container) {
                Container c = (Container) displayable;
                c.remove(obj);
            }
            displayables.remove(obj);
            JNode.getDisplayables().remove(obj);
        }
        syncSize();
        arrange();
        return this;
    }

    /**
     * removes all displayables contained in this container;
     * all reference to this displayable is then removed from JNode.
     *
     * @since April 24th code cleaned up by Jiachen Ren
     */
    public Container removeAll() {
        for (int i = displayables.size() - 1; i >= 0; i--) {
            Displayable displayable = displayables.get(i);
            if (displayable instanceof Container) {
                Container c = (Container) displayable;
                c.removeAll();
            }
            this.remove(displayable);
        }
        syncSize();
        arrange();
        return this;
    }

    /**
     * the inherited method is overridden as the displayable objects
     * contained within this container would also need to be resized.
     *
     * @param w the new width of the container
     * @param h the new height of the container
     */
    @Override
    public void resize(float w, float h) {
        super.resize(w, h);
        syncSize();
        arrange();
    }

    @Override
    public void relocate(float x, float y) {
        super.relocate(x, y);
        this.arrange();
    }

    @Override
    public Displayable setVisible(boolean temp) {
        super.setVisible(temp);
        if (displayables == null) return this; /*this is here to prevent null pointer exception thrown by Table.*/
        for (int i = displayables.size() - 1; i >= 0; i--) {
            Displayable displayable = displayables.get(i);
            if (displayable instanceof Container) {
                Container c = (Container) displayable;
                c.setVisible(temp);
            } else {
                displayable.setVisible(temp);
            }
        }
        return this;
    }

    public static void refresh() {
        for (Container container : JNode.getContainers()) {
            container.syncSize();
            container.arrange();
        }
    }

    public Container setDisplayables(ArrayList<Displayable> displayables) {
        this.displayables = displayables;
        return this;
    }

    /**
     * Applies all the applicable layouts specific to this container to all of
     * the sub-containers/displayables. All matter considering the alignment settings
     * specific to containers objects are not applied since the distinction
     * between VBox and HBox.
     */
    public Container applyLayoutToNodes() {
        for (int i = displayables.size() - 1; i >= 0; i--) {
            Displayable displayable = displayables.get(i);
            /*instanceof, learned April 22nd.*/
            if (displayable instanceof Container) {
                Container container = (Container) displayable;
                container.setContainerVisible(containerVisible);
                container.setSpacing(spacing);
                container.setMargins(marginX, marginY);
                container.applyLayoutToNodes();
            }
        }
        return this;
    }

    /**
     * Apply the outlook of this container to all of its nodes. Overloading method of the root method
     * applyOutlookToNodes(Displayable root, ArrayList<Displayable> nodes, boolean recursive, Class... omits);
     * see details below.
     *
     * @return this instance of Container for chained access.
     */
    public Container applyOutlookToNodes() {
        Container.applyOutlookToNodes(this, displayables, true);
        return this;
    }


    /**
     * Overloading method of the root method
     * applyOutlookToNodes(Displayable root, ArrayList<Displayable> nodes, boolean recursive, Class... omits);
     * see details below.
     *
     * @since May 2nd.
     */
    public static void applyOutlookToNodes(Displayable root, ArrayList<?> nodes, boolean recursive) {
        Container.applyOutlookToNodes(root, nodes, recursive, null, null);
    }


    /**
     * apply the outlook of the root displayable to all of the displayable instances in the nodes. Type specific
     * appearances are transferred where possible. The method automatically determines the type of root and the
     * type of the instances in the nodes ArrayList and transfer compatible appearances where possible.
     *
     * @param root      the root displayable instance, i.e. the displayable instance in which the outlook of the
     *                  displayable instances in the nodes would be based on.
     * @param nodes     a displayable ArrayList that contains a list of displayable objects whose outlook are
     *                  going to be altered according to the root displayable obj.
     * @param recursive if this boolean value is set to true, then the effect would propagate to the very last
     *                  leaf nodes in any of the containers contained within the nodes ArrayList and for any
     *                  containers contained within these containers.
     * @param omits     the class types to be omitted from this specific operation. for example, if you only want to
     *                  change the outlook of buttons in the nodes of ArrayList and not the labels, then you can pass
     *                  in Label.class as an omitted class. The post condition in this case would be that none of the
     *                  label instances contained in nodes would be altered.
     * @since May 2nd.
     */
    public static void applyOutlookToNodes(Displayable root, ArrayList<?> nodes, boolean recursive, Class... omits) {
        nodes.forEach(unknownDisplayable -> {
            Displayable displayable = (Displayable) unknownDisplayable;
            for (Class class_ : omits) {
                if (class_ == null) continue;
                String temp = class_.getSimpleName();
                if (displayable.getClass().getSimpleName().equals(temp))
                    return;
            }
            displayable.setBackgroundColor(root.backgroundColor);
            displayable.setMouseOverBackgroundColor(root.mouseOverBackgroundColor);
            displayable.setMousePressedBackgroundColor(root.mousePressedBackgroundColor);
            displayable.setContourColor(root.backgroundColor);
            displayable.setMouseOverContourColor(root.mouseOverBackgroundColor);
            displayable.setMousePressedContourColor(root.mousePressedBackgroundColor);
            displayable.setContourVisible(root.displayContour);
            displayable.setContourThickness(root.contourThickness);
            displayable.setRounded(root.isRounded);
            displayable.setRounding(root.rounding);
            if (displayable instanceof Contextual && root instanceof Contextual) {
                ((Contextual) displayable).setTextColor(((Contextual) root).getTextColor());
                ((Contextual) displayable).setMouseOverTextColor(((Contextual) root).getMouseOverTextColor());
                ((Contextual) displayable).setMousePressedTextColor(((Contextual) root).getMousePressedTextColor());
            }
            if (recursive && displayable instanceof Container) {
                Container container = (Container) displayable;
                applyOutlookToNodes(root, container.getDisplayables(), true, omits);
            }
        });
    }

    /**
     * @return the displayable ArrayList.
     */
    public ArrayList<Displayable> getDisplayables() {
        return displayables;
    }

    /**
     * This method does not go through the stacks of containers that it contains
     *
     * @param id the id of the requested displayable obj
     * @return the first displayable obj with the correct id
     */
    public Displayable getDisplayableById(String id) {
        for (Displayable displayable : displayables)
            if (displayable.getId().equals(id))
                return displayable;
        return null;
    }

    /**
     * recursively search for Displayable objects in the subContainers
     *
     * @param id the id for the Displayable
     * @return the first Displayable with the id in the front-most stack
     */
    public Displayable search(String id) {
        for (Displayable displayable : displayables) {
            if (displayable.getId().equals(id))
                return displayable;
            else if (displayable instanceof Container) {
                Displayable displayable1 = ((Container) displayable).search(id);
                if (displayable1 != null)
                    return displayable1;
            }
        }
        return null;
    }

    public int visibleDisplayables() {
        int c = 0;
        for (Displayable displayable : displayables)
            if (displayable.isVisible() || displayable instanceof SpaceHolder)
                c++;
        return c;
    }

    public boolean shouldCollapse(Displayable displayable) {
        return collapseInvisible && !displayable.isVisible() && !(displayable instanceof SpaceHolder);
    }

    /**
     * prints the id of each displayable contained accordingly with the
     * relative width and height assignment to each.
     *
     * @return the sum of relative width and relative height
     */
    public float[] printDisplayables() {
        float relativeVal[] = new float[]{0, 0};
        for (Displayable displayable : displayables) {
            if (!displayable.isRelative() || displayable.isUndeclared())
                continue;
            String id = displayable.getId();
            String relativeW = "relative width: " + Float.toString(displayable.getRelativeW()) + ";";
            String relativeH = "relative height: " + Float.toString(displayable.getRelativeH()) + ";";
            System.out.println("id = \"" + id + "\";" + relativeW + relativeH);
            relativeVal[0] += displayable.getRelativeW();
            relativeVal[1] += displayable.getRelativeH();
        }
        return relativeVal;
    }

    public Container addAll(ArrayList displayables) {
        for (Object obj : displayables) {
            if (!(obj instanceof Displayable))
                throw new IllegalArgumentException("argument is not an instance of Displayable");
            this.add((Displayable) obj);
        }
        return this;
    }

    public Container addAll(Displayable... displayables) {
        Arrays.stream(displayables).forEach(this::add);
        return this;
    }

    public void matchWindowDimension(boolean temp) {
        this.matchWindowDimension = temp;
    }

    @Override
    public Container setId(String id) {
        super.setId(id);
        return this;
    }

    public void remove(String id) {
        for (int i = displayables.size() - 1; i >= 0; i--) {
            Displayable displayable = displayables.get(i);
            if (displayable.getId().equals(id))
                this.remove(displayable);
        }
    }
}


