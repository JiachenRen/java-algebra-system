package jui;


import processing.core.PConstants;

import java.util.ArrayList;

//modified Jan 12 by Jiachen Ren
//modified Jan 16
//idea: universal theme.
public class ScrollField extends Contextual {
    private int spacing;
    private int maxCapacity;
    private float textHeight;
    private int ruledLinesColor = getParent().color(0);
    private float ruledLinesThickness = 0.5f;
    private boolean displayRuledLines;
    private String buffer;
    private ArrayList<String> buffered_lines;

    /* deprecated
        public ScrollField(float x, float y) {
            this(Integer.toString(JNode.scrollFieldIdentifier+1), x, y);
            JNode.scrollFieldIdentifier++;
        }
    */

    public ScrollField(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    public ScrollField(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public ScrollField(String id) {
        super();
        init();
    }

    private void init() {
        setTextSize(15); //setting the default text size to 15
        buffered_lines = new ArrayList<String>();
        textHeight = this.getTextHeight();
        alignment = PConstants.LEFT;
        buffer = "";
        spacing = 5;
        maxCapacity = (int) (h / (textHeight + spacing));
    }

    public void display() {
        //drawing the background
        super.display(); /*code cleaned up April 22nd*/

        getParent().fill(getTextColor());
        if (getTextSize() > 0.0) getParent().textSize(getTextSize());
        getParent().textAlign(alignment);

        //determine the lines to be displayed.
        ArrayList<String> temp = new ArrayList<String>();
        int to_be_displayed = (int) (h / (textHeight + spacing));
        for (int i = buffered_lines.size() - to_be_displayed - 1; i <= buffered_lines.size() - 1; i++) {
            if (i < 0) continue;
            temp.add(buffered_lines.get(i));
        }

        //render each line
        for (int i = 0; i <= temp.size() - 1; i++) {
            switch (alignment) {
                case PConstants.LEFT:
                    getParent().text(temp.get(i), x, y + (textHeight + spacing) * (i + 1));
                    break;
                case PConstants.RIGHT:
                    getParent().text(temp.get(i), x + w, y + (textHeight + spacing) * (i + 1));
                    break;
                case PConstants.CENTER:
                    getParent().text(temp.get(i), x + w / 2, y + (textHeight + spacing) * (i + 1));
                    break;
            }
        }

        //draw the ruled lines
        if (displayRuledLines) displayRuledLines();
    }

    private void displayRuledLines() {
        int lines = (int) (h / (textHeight + spacing));
        getParent().stroke(ruledLinesColor);
        getParent().strokeWeight(ruledLinesThickness);
        for (int i = 0; i < lines; i++) {
            getParent().line(x, y + (textHeight + spacing) * (i + 1) + 2, w + x, y + (textHeight + spacing) * (i + 1) + 2);
        }
        getParent().strokeWeight(1);
    }

    public ScrollField setRuledLinesThickness(float temp) {
        ruledLinesThickness = temp;
        return this;
        //to be implemented
    }

    public ScrollField setRuledLines(boolean temp) {
        displayRuledLines = temp;
        return this;
        //to be implemented
    }

    public ScrollField setRuledLinesColor(int r, int g, int b) {
        ruledLinesColor = JNode.getParent().color(r, g, b);
        return this;
    }

    public ScrollField setRuledLinesColor(int r, int g, int b, int t) {
        ruledLinesColor = JNode.getParent().color(r, g, b, t);
        return this;
    }

    public ScrollField setRuledLinesColor(int c) {
        ruledLinesColor = c;
        return this;
    }

    public ScrollField setSpacing(int temp) {
        this.spacing = temp;
        return this;
    }

    public ScrollField setMaxCapacity(int temp) {
        this.maxCapacity = temp;
        return this;
    }

    public ScrollField println(String temp) {
        this.buffer += (temp + "\n");
        processBuffer();
        return this;
    }

    public ScrollField println(float temp) {
        this.buffer += (temp + "\n");
        processBuffer();
        return this;
    }

    public ScrollField println(int temp) {
        this.buffer += (temp + "\n");
        processBuffer();
        return this;
    }

    public ScrollField println(char temp) {
        this.buffer += (temp + "\n");
        processBuffer();
        return this;
    }

    public ScrollField println() {
        this.buffer += "\n";
        processBuffer();
        return this;
    }

    public ScrollField print(String temp) {
        this.buffer += temp;
        processBuffer();
        return this;
    }

    public ScrollField print(float temp) {
        this.buffer += temp;
        processBuffer();
        return this;
    }

    public ScrollField print(int temp) {
        this.buffer += temp;
        processBuffer();
        return this;
    }

    public ScrollField print(char temp) {
        this.buffer += temp;
        processBuffer();
        return this;
    }

    private void processBuffer() {
        if (w <= 0 || h <= 0) return; //fixed Jan 25th.
        int maxLength = (int) (maxCapacity * w / getTextWidth("-")) + 10;
        try {
            buffer = buffer.length() < maxLength ? buffer : buffer.substring(buffer.length() - maxLength, buffer.length());
        } catch (RuntimeException e) {
            //System.out.println("operation failed");
        }
        this.buffered_lines = formulate(buffer);
        trimBuffer();
    }

    private void trimBuffer() {
        ArrayList<String> preserved = new ArrayList<String>();
        for (int i = buffered_lines.size() - maxCapacity - 1; i <= buffered_lines.size() - 1; i++) {
            if (i < 0) continue;
            preserved.add(buffered_lines.get(i));
        }
        buffered_lines = preserved;
    }

    private ArrayList<String> formulate(String temp) {
        ArrayList<String> lines = new ArrayList<String>();
        String buf = "";
        for (int i = 0; i < temp.length(); i++) {
            char c = temp.charAt(i);
            if (c == '\n') {
                lines.add(buf);
                buf = "";
                continue;
            }
            if (this.getTextWidth(buf + c + c) < w) {
                buf += c;
            } else {
                lines.add(buf + c);
                buf = "";
            }
        }
        lines.add(buf);
        return lines;
    }

    @Override
    public void resize(float w, float h) {
        this.w = w;
        this.h = h;
        maxCapacity = (int) (h / (textHeight + spacing));
        processBuffer();
    }
}