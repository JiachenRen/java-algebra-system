package jui;

public class Switch extends Button {
    private String contentOff;
    private boolean isOn;
    private int backgroundColorOff;
    private int contourColorOff;
    private int textColorOff;
    private boolean pressedOnTarget;
    private String contentOn;

    public Switch(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public Switch(float relativeW, float relativeH) {
        super(relativeW, relativeH);
    }

    public Switch() {
        super();
    }

    @Override
    public void init() {
        super.init();
        backgroundColorOff = backgroundColor;
        contourColorOff = contourColor;
        textColorOff = getTextColor();
        this.addEventListener("@RESERVED", Event.MOUSE_PRESSED, () -> pressedOnTarget = true);
        this.addEventListener("@RESERVED", Event.MOUSE_LEFT, () -> pressedOnTarget = false);
        this.addEventListener("@RESERVED", Event.MOUSE_RELEASED, () -> {
            if (pressedOnTarget) setState(!isOn);
            pressedOnTarget = false;
        });
    }

    public Switch setContentOff(String contentOff) {
        this.contentOff = contentOff;
        super.setContent(contentOff);
        return this;
    }

    public boolean isOn() {
        return isOn;
    }

    public Switch setBackgroundColorOff(int color) {
        this.backgroundColorOff = color;
        return this;
    }

    public Switch setContourColorOff(int color) {
        this.contourColorOff = color;
        return this;
    }

    public Switch setTextColorOff(int color) {
        this.textColorOff = color;
        return this;
    }

    @SuppressWarnings("deprecation")
    private Switch updateState() {
        setContent(isOn ? contentOn : contentOff);
        setBackgroundColor(isOn ? backgroundColor : backgroundColorOff);
        setTextColor(isOn ? getTextColor() : textColorOff);
        setContourColor(isOn ? contourColor : contourColorOff);
        return this;
    }

    public Switch setState(boolean isOn) {
        this.isOn = isOn;
        updateState();
        return this;
    }

    public Switch setContentOn(String s) {
        super.setContent(s);
        this.contentOn = s;
        return this;
    }

    @Override
    public Switch inheritOutlook(Displayable other) {
        super.inheritOutlook(other);
        return this;
    }

    @Override
    public Switch inheritMode(Displayable other) {
        super.inheritMode(other);
        return this;
    }

    @Deprecated
    public Switch setContent(String s) {
        super.setContent(s);
        return this;
    }

}
