package jui_lib;

import processing.core.PConstants;

import java.util.ArrayList;

public class MenuDropdown extends Contextual implements MouseControl {
    //add set dropdown contour & rounded
    private ArrayList<MenuItem> menuItems;
    public float itemHeight;
    public boolean triggered; //to determine if the action is driven by its parent. 3 hours of debugging!!!
    public boolean mouseTriggering, keyTriggering;
    public int triggeringMouseButton;
    public ArrayList<Integer> downKeyCodes, triggeringKeys;
    private Runnable triggeringEvent;


    public MenuDropdown(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public MenuDropdown(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public MenuDropdown(String id) {
        super(0, 0, 0, 0);
        init();
    }

    public MenuDropdown onTrigger(Runnable r) {
        triggeringEvent = r;
        return this;
    }

    public MenuDropdown setTriggeringMouseButton(int mouseButton_) {
        this.triggeringMouseButton = mouseButton_;
        return this;
    }

    public MenuDropdown setMouseTriggering(boolean temp) {
        mouseTriggering = temp;
        return this;
    }

    public MenuDropdown setKeyTriggering(boolean temp) {
        keyTriggering = temp;
        return this;
    }

    public MenuDropdown setTriggeringKeys(int[] tempTriggeringKeys) {
        triggeringKeys.clear();
        for (int i = 0; i < tempTriggeringKeys.length; i++)
            triggeringKeys.add(tempTriggeringKeys[i]);
        return this;
    }

    public MenuDropdown onClick(String id, Runnable r) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getId().equals(id)) {
                menuItem.onClick(r);
            }
        }
        return this;
    }

    public MenuDropdown bind(String id, MenuDropdown m) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getId().equals(id)) {
                menuItem.bind(m);
            }
        }
        return this;
    }

    public boolean hasFocus() {
        return !(triggered && !isMouseOver()) && triggered;
/*
        for (MenuItem m : menuItems) {
            if (m.hasFocus())
                return true;
        }
        return false;
        */
    }

    @Override
    public boolean isMouseOver() {
        for (MenuItem m : menuItems) {
            if (m.hasFocus())
                return true;
        }
        return false;
    }

    public boolean containsAll(ArrayList<Integer> inv, ArrayList<Integer> trig) {
        for (Integer i : trig) {
            boolean contains = false;
            for (Integer m : inv) {
                if (i.intValue() == m.intValue())
                    contains = true;
            }
            if (!contains) return false;
        }
        return true;
    }

    public MenuDropdown setItemHeight(int temp) {
        itemHeight = temp;
        return this;
    }

    public void mousePressed() {
        if (mouseTriggering && triggeringMouseButton == getParent().mouseButton) {
            //System.out.println("MouseReleased: "+getParent().mouseButton+" Current: "+triggeringMouseButton);
            this.setVisible(!this.isVisible());
            if (isVisible()&&triggeringEvent!=null)
                triggeringEvent.run();

        }
        if (!isVisible()) return;
        for (MenuItem m : menuItems) {
            m.mousePressed();
        }
    }

    public void mouseReleased() {

        if (triggeringMouseButton != getParent().mouseButton) {
            //does not work with FX2D, since the mouseReleased button is always 0!
            //System.out.println("MouseReleased: "+getParent().mouseButton+" Current: "+triggeringMouseButton);

            for (MenuItem menuItem : menuItems) {
                //if(hasFocus())
                menuItem.mouseReleased();
            }
            setVisible(false);
        }
        if (!hasFocus()) return;
        for (MenuItem m : menuItems) {
            m.mouseReleased();
        }
    }

    //this needs to be re-written later.
    public boolean isFrontMost() {
        for (TextInput textInput : JNode.getTextInputs())
            if (textInput.isFocusedOn()) return false;
        return true;
    }

    public void keyPressed() {
        if (keyTriggering) {
            if (this.isFrontMost())
                downKeyCodes.add(getParent().keyCode);
            if (containsAll(downKeyCodes, triggeringKeys)) {
                this.setVisible(!this.isVisible());
                if (isVisible()&&triggeringEvent!=null)
                    triggeringEvent.run();
            }
        }
        if (!isVisible()) return;
        for (MenuItem m : menuItems) {
            m.keyPressed();
        }
    }

    public void keyReleased() {
        if (keyTriggering)
            downKeyCodes.remove(new Integer(getParent().keyCode));
        if (!isVisible()) return;
        for (MenuItem m : menuItems) {
            m.keyReleased();
        }
    }

    @Deprecated
    public void mouseDragged() {

    }

    private void init() {
        menuItems = new ArrayList<MenuItem>();
        downKeyCodes = new ArrayList<Integer>();

        //the default keyTriggering events
        triggeringKeys = new ArrayList<Integer>();
        triggeringKeys.add(16); //shift
        triggeringKeys.add(77); //'m'

        //defaults to right click trigger. Defaults to not enabled.
        triggeringMouseButton = PConstants.RIGHT;

        setVisible(false);
        alignment = PConstants.LEFT;
        setTextSize(JNode.UNI_MENU_TEXT_SIZE);
        //modified Jan 35th
    }

    public void display() {
        for (MenuItem menuItem : menuItems) {
            menuItem.run();
        }
    }

    public MenuDropdown add(MenuItem menuItem) {
        this.menuItems.add(menuItem);
        arrange();
        return this;
    }

    private void arrange() {
        Container.applyOutlookToNodes(this,menuItems,false);
        itemHeight =getTextHeight();
        float itemWidth = 0;
        for (MenuItem menuItem : menuItems) {
            if (getTextWidth(menuItem.getContent()) > itemWidth)
                itemWidth = getTextWidth(menuItem.getContent());
        }
        this.w = itemWidth;
        for (MenuItem menuItem : menuItems) {
            menuItem.resize(this.w, this.itemHeight);
        }
        float curX = getX(), curY = getY();
        for (MenuItem menuItem : menuItems) {
            menuItem.relocate(curX, curY);
            curY += menuItem.getHeight();
        }
    }

    @Override
    public MenuDropdown setTextSize(float temp) {
        super.setTextSize(temp);
        arrange();
        return this;
    }

    @Deprecated
    public void resize(float w, float h) {
        //deprecated. The size of the menu should automatically adjust itself with the textSize.
    }

    @Override
    public void relocate(float x, float y) {
        super.relocate(x,y);
        arrange();
    }

    @Override
    public MenuDropdown setVisible(boolean temp) {
        this.isVisible = temp;
        for (MenuItem m : menuItems) {
            m.setVisible(temp);
        }
        return this;
    }
}
