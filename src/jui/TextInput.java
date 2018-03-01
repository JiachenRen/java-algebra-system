package jui;

import processing.core.PApplet;
import processing.core.PConstants;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

//code refactored Jan 27th, class name changed to TextInput
//refactored Feb 4th. Debugged the issue where the textSize won't refresh, which is caused by the abstracting of the displayText() method.
//add setAlign and shift down. Done. Jan 21.
//add onEditing method. Idea Jan 26th. Resolved.
//considering allowing the user to change textSize with a ratio that factors into the total height. No. discouraged. Jan 26th.
//using event listeners instead of Runnable? Resolved May 21st.
//TODO: add key event listeners idea May 24th, last night before last day of class
public class TextInput extends Contextual implements MouseControl, KeyControl, Serializable {
    private boolean isFocusedOn;
    private boolean isLockedOn;
    private boolean displayCursor;
    private boolean shiftDown;
    private String staticContent;
    private String contentOnScreen;
    private String defaultContent;
    private int timer;
    private int cursorColor;
    private float cursorThickness;
    private int timesSubmitted;
    private Runnable submitMethod;
    private Runnable onEditingMethod;
    private Runnable onKeyTypedMethod;
    private Runnable onFocusMethod;
    private static String sketchRenderer;
    private boolean commandDown;
    private boolean controlDown;
    private boolean cursorIsMobile;
    private Cursor cursor;
    private static final String fx2dKeyboardDict;
    private static final String fx2dKeyboardMismatchDict;

    static {
        sketchRenderer = JNode.getParent().sketchRenderer();
        fx2dKeyboardDict = "þ\"à~1!2@3#4$5%6^7&8*9(0)-_=+[{]}\\|;:'\",<.>/?"; //improved May 16th
        fx2dKeyboardMismatchDict = "à`þ'";
    }

    public TextInput(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public TextInput(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public TextInput() {
        super();
        init();
    }

    private void init() {
        setBackgroundStyle(JStyle.VOLATILE);
        setTextStyle(JStyle.VOLATILE);
        setDefaultContent("text");
        setContent(defaultContent);
        staticContent = "";
        timer = getParent().millis();
        cursorColor = contourColor;
        cursorThickness = contourThickness;
        alignment = PConstants.LEFT;
        cursor = new Cursor();
        cursorIsMobile = true;
    }

    public boolean isFocusedOn() {
        return isFocusedOn;
    }

    @Override
    public void run() {
        super.run();
        if (isFocusedOn() && onEditingMethod != null) {
            onEditingMethod.run();
        }
    }

    public void display() {
        if (font != null) getParent().textFont(font);
        super.display();

        getParent().pushMatrix();
        updateContentOnScreen();

        if (isFocusedOn) {
            if (displayCursor)
                if (cursorIsMobile)
                    cursor.display();
                else
                    displayCursor();
            if (onEditingMethod != null)
                onEditingMethod.run();
        }

        super.displayText(contentOnScreen);

        getParent().popMatrix();

        if (getParent().millis() - timer >= 500) {
            timer = getParent().millis();
            displayCursor = !displayCursor;
        }
    }

    private void updateContentOnScreen() {
        contentOnScreen = getContent();
        for (int i = 0; i < getContent().length(); i++) {
            contentOnScreen = (alignment == PApplet.LEFT ? " " : "") + getContent().substring(getContent().length() - i - 1, getContent().length());
            if (getTextWidth(contentOnScreen) > w) {
                contentOnScreen = getContent().substring(getContent().length() - i, getContent().length());
                break;
            }
        }
    }

    public TextInput onSubmit(Runnable temp_method) {
        submitMethod = temp_method;
        return this;
    }

    public TextInput setCursorColor(int c) {
        cursorColor = JNode.getParent().color(c);
        return this;
    }

    public TextInput setCursorColor(int r, int g, int b) {
        cursorColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public TextInput setCursorColor(int r, int g, int b, int t) {
        cursorColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public TextInput setCursorThickness(int strokeWeight) {
        cursorThickness = strokeWeight;
        return this;
    }

    public void displayCursor() {
        getParent().stroke(cursorColor);
        getParent().strokeWeight(cursorThickness);
        float y2 = y + h - 1;
        float xv1 = x + w / 2;
        float xv2 = x + w - 2;
        float xv3 = x + 2;
        float tw = getTextWidth(contentOnScreen);
        if (contentOnScreen.equals("")) {
            switch (alignment) {
                case PConstants.LEFT:
                    getParent().line(xv3, y, xv3, y2);
                    break;
                case PConstants.CENTER:
                    getParent().line(xv1, y, xv1, y2);
                    break;
                case PConstants.RIGHT:
                    getParent().line(xv2, y, xv2, y2);
                    break;
            }
        } else {
            switch (alignment) {
                case PConstants.LEFT:
                    getParent().line(x + tw, y, x + tw, y2);
                    break;
                case PConstants.CENTER:
                    getParent().line(xv1 + tw / 2, y, xv1 + tw / 2, y2);
                    break;
                case PConstants.RIGHT:
                    getParent().line(xv2, y, xv2, y2);
                    break;
            }
        }
    }

    /**
     * keyPressed event listener. The key is used to acclimate the content of this
     * instance of the text field.
     *
     * @since April 28th java FX2D renderer issue has been taken into consideration.
     * now the clipboard for macOs works fine with all of processing's renderer.
     */
    public void keyPressed() {
        super.keyPressed();
        if (!this.isFocusedOn || handleModifierKeys(false)) return;
        switch (getParent().keyCode) {
            case 8:
                if (getContent().length() > 0)
                    setContent(getContent().substring(0, getContent().length() - 1));
                break;
            case 10:
                staticContent = getContent();
                timesSubmitted++;
                if (submitMethod != null)
                    submitMethod.run();
                break;
            default:
                char c = Character.toLowerCase(getParent().key);
                String processed = Character.toString(c);
                boolean isFx2d = sketchRenderer.toLowerCase().contains("fx2d");
                if (shiftDown) {
                    /*if (key <= 'z' && key >= 'a')*/
                    if (isFx2d && fx2dKeyboardDict.contains(processed)) {
                        int index = fx2dKeyboardDict.indexOf(c);
                        setContent(getContent() + fx2dKeyboardDict.charAt(index + 1));
                    } else {
                        setContent(getContent() + processed.toUpperCase());
                    }
                } else if (commandDown) {
                    handleCommandDownActions(c);
                } else if (controlDown) {
                    if (!JNode.OS.contains("mac"))
                        handleCommandDownActions(c);
                } else {
                    if (isFx2d && fx2dKeyboardMismatchDict.contains(processed)) {
                        int index = fx2dKeyboardMismatchDict.indexOf(c);
                        setContent(getContent() + fx2dKeyboardMismatchDict.charAt(index + 1));
                    } else
                        setContent(getContent() + processed);
                }
                break;
        }
        if (onKeyTypedMethod != null) onKeyTypedMethod.run();
    }

    /**
     * @param keyReleased whether or not it is receiving from keyReleased().
     *                    otherwise, this method is invoked for keyPressed().
     * @return whether or not this specific call for keyPressed() or keyReleased() is executed;
     * if so, return true.
     * @since April 28th
     */
    private boolean handleModifierKeys(boolean keyReleased) {
        if (sketchRenderer.contains("FX2D")) {
            switch (this.getParent().keyCode) {
                case 16:
                    shiftDown = !keyReleased;
                    return true;
                case 768:
                    commandDown = !keyReleased;
                    return true;
                case 17:
                    controlDown = !keyReleased;
                    return true;
            }
        } else {
            switch (this.getParent().keyCode) {
                case 16:
                    shiftDown = !keyReleased;
                    return true;
                case 157:
                    commandDown = !keyReleased;
                    return true;
                case 17:
                    controlDown = !keyReleased;
                    return true;
            }
        }
        return false;
    }

    /**
     * @param _key the key pressed by the user
     * @since April 28th
     * this method handles common clipboard actions such as copy and paste.
     * TODO add select all
     */
    private void handleCommandDownActions(char _key) {
        switch (_key) {
            case 'v':
                acclimateContentFromClipboard();
                break;
            case 'c':
                setClipboard(getContent());
                break;
        }
    }

    /**
     * this method sets the content of the system clipboard to the content that it takes in.
     *
     * @param content the content in which the system clipboard is going to be set to.
     * @since April 28th
     */
    public static void setClipboard(String content) {
        StringSelection selection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * @param preserveLines whether or not to preserve the lines. If this
     *                      is set to false, then the String returned would be
     *                      consisted of a single line.
     * @return the String extracted from the system clipboard.
     * @since April 28th
     */
    public static String getStringFromClipboard(boolean preserveLines) {
        String incrementer = "";
        try {
            ArrayList<String> storedInputs = new ArrayList<>();
            String data = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
            Scanner scanner = new Scanner(data);
            while (scanner.hasNext()) {
                storedInputs.add(scanner.nextLine());
            }
            for (String s : storedInputs) {
                incrementer += s + (preserveLines ? "\n" : "");
            }
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return incrementer;
    }

    private void acclimateContentFromClipboard() {
        /*modified March 8th.*/
        setContent(getContent() + getStringFromClipboard(false));
    }

    public void keyReleased() {
        super.keyReleased();
        handleModifierKeys(true);
    }

    public void mousePressed() {
        super.mousePressed();
        if (isMouseOver()) this.isLockedOn = true;
        else {
            this.isLockedOn = false;
            this.isFocusedOn = false;
        }
    }

    public void mouseReleased() {
        super.mouseReleased();
        if (isMouseOver() && isLockedOn) {
            if (this.getContent().equals(defaultContent))
                this.setContent("");
            this.isFocusedOn = true;
            if (onFocusMethod != null) onFocusMethod.run();
        } else {
            this.isLockedOn = false;
            this.isFocusedOn = false;
            if (this.getContent().equals("")) this.setContent(defaultContent);
        }
    }

    public TextInput setStaticContent(String temp) {
        staticContent = temp;
        setContent(temp);
        return this;
    }

    public TextInput setDefaultContent(String temp) {
        this.setContent(temp);
        this.setStaticContent(temp);//TODO DEBUG added May 17th
        this.defaultContent = temp;
        return this;
    }

    public String getStaticContent() {
        return staticContent;
    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public int getIntValue() {
        try {
            return Integer.valueOf(getStaticContent());
        } catch (NumberFormatException e) {
            System.out.println("id = " + id);
            System.err.println("\"" + getStaticContent() + "\" can not be converted to a number.");
            return 0;
        }
    }

    public float getFloatValue() {
        try {
            return Float.valueOf(getContent()); //TODO modified for convenience May 17th
        } catch (NumberFormatException e) {
            System.out.println("id = " + id);
            System.err.println("\"" + getContent() + "\" can not be converted to a number.");
            return 0;
        }
    }

    public int getTimesSubmitted() {
        return timesSubmitted;
    }

    @Override
    public TextInput setMousePressedBackgroundColor(int r, int g, int b) {
        //deprecated.
        return this;
    }

    /**
     * @param align the alignment for the text
     * @return the current instance of TextInput. This would be useful for chained access.
     * @since April 28th. Alignment left is recommended since the cursor
     * would only be mobile in this scenario.
     */
    @Override
    public TextInput setAlign(int align) {
        switch (align) {
            case PConstants.LEFT:
                cursorIsMobile = true;
                break;
            case PConstants.RIGHT:
                cursorIsMobile = false;
            case PConstants.CENTER:
                cursorIsMobile = false;
        }
        super.setAlign(align);
        return this;
    }

    /**
     * TODO to be completed
     */
    private class Cursor {

        Cursor() {

        }

        private void display() {
            getParent().strokeWeight(cursorThickness);
            getParent().stroke(cursorColor);

            if (contentOnScreen.length() == 0) {
                displayCursor();
                return;
            }
            float y2 = y + h - 1;
            float tw = getTextWidth(contentOnScreen);
            getParent().line(x + tw, y, x + tw, y2);
        }
    }

    @Override
    public void calculateTextSize() {
        if (h <= 0) return;
        setTextSize(h * maxTextPercentage);
    }

    public TextInput onEditing(Runnable runnable) {
        this.onEditingMethod = runnable;
        return this;
    }

    public TextInput onKeyTyped(Runnable runnable) {
        this.onKeyTypedMethod = runnable;
        return this;
    }

    public TextInput onFocus(Runnable runnable) {
        this.onFocusMethod = runnable;
        return this;
    }

    public TextInput setIsFocusedOn(boolean temp) {
        this.isFocusedOn = temp;
        return this;
    }

    public Runnable getSubmitMethod() {
        return submitMethod;
    }

    @Override
    public TextInput clone() {
        return (TextInput) super.clone();
    }
}