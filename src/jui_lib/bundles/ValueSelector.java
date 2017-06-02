package jui_lib.bundles;

import jui_lib.*;
import processing.core.PConstants;

/**
 * Created on April 23rd. Value selector bundle
 * TODO add setSuffix(), like km, mm, or $, %
 */
public class ValueSelector extends VBox {
    private float titlePercentage = .35f;
    private Label titleLabel;
    private TextInput textInput;
    private HSlider valueSlider;
    private int roundIndex = 1;
    private Runnable linkedMethod;
    public Style style = Style.COMPOSITE;

    public enum Style {
        VERTICAL(0), COMPOSITE(1), HORIZONTAL(2);
        private int val;

        Style(int i) {
            val = i;
        }

        public int getValue() {
            return val;
        }
    }

    public ValueSelector(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        initialize();
    }

    public ValueSelector() {
        super();
        initialize();
    }

    public ValueSelector(float x, float y, float w, float h) {
        super(x, y, w, h);
        initialize();
    }

    private void initialize() {

        this.setMargins(0, 0);
        this.setAlignV(PConstants.DOWN);
        this.removeAll();
        switch (style) {
            case COMPOSITE:
                HBox titleWrapper = new HBox();
                titleWrapper.setId("titleWrapper");
                titleWrapper.setMargins(0, 0);
                this.add(titleWrapper);

                titleLabel = new Label(titlePercentage, 1.0f);
                titleLabel.setContent("Var");
                titleWrapper.add(titleLabel);

                valueSlider = new HSlider();
                valueSlider.setId("valueSlider");
                this.add(valueSlider);

                textInput = new TextInput();
                titleWrapper.add(textInput);
                break;
            case HORIZONTAL:
                HBox wrapper = new HBox();
                wrapper.setId("wrapper");
                wrapper.setMargins(0, 0);
                this.add(wrapper);

                titleLabel = new Label(titlePercentage, 1.0f);
                titleLabel.setContent("Var");
                wrapper.add(titleLabel);

                textInput = new TextInput(0.1f, 1.0f);
                wrapper.add(textInput);

                valueSlider = new HSlider();
                valueSlider.setId("valueSlider");
                wrapper.add(valueSlider);

                break;
            case VERTICAL:
                break;
        }


        valueSlider.setScalingFactor(0.5f);
        valueSlider.setRollerShape(PConstants.RECT);
        valueSlider.setRange(0, 1);
        valueSlider.setValue(0.5f);
        valueSlider.onFocus(() -> {
            String formatted = this.round(valueSlider.getFloatValue());
            textInput.setStaticContent(formatted);
            if (linkedMethod != null)
                linkedMethod.run();
        });

        textInput.setContent(round(0.5f));
        textInput.onSubmit(() -> {
            if (textInput.getFloatValue() < valueSlider.valueLow) {
                textInput.setStaticContent(valueSlider.valueLow + "");
                valueSlider.setValue(valueSlider.valueLow);
                return;
            } else if (textInput.getFloatValue() > valueSlider.valueHigh) {
                textInput.setStaticContent(valueSlider.valueHigh + "");
                valueSlider.setValue(valueSlider.valueHigh);
                return;
            }
            valueSlider.setValue(textInput.getFloatValue());
            if (linkedMethod != null)
                linkedMethod.run();
        });
    }

    /**
     * only invoke this method immediately after declaration.
     * TODO not the best way to handle this issue. To be FIXED
     *
     * @param style the new default
     */
    public ValueSelector setStyle(Style style) {
        this.style = style;
        initialize();
        return this;
    }

    public ValueSelector setTitlePercentage(float temp) {
        titlePercentage = temp;
        titleLabel.setRelativeW(temp);
        return this;
    }

    public ValueSelector setTitle(String title) {
        titleLabel.setContent(title);
        return this;
    }

    /**
     * @param val the value that is going to be applied to both the
     *            slider and the text input. If the value is less than
     *            lower bound or upper bound, then the value is default
     *            to the min/max value of the slider.
     */
    public ValueSelector setValue(float val) {
        valueSlider.setValue(val);
        if (val < valueSlider.valueLow)
            textInput.setStaticContent(valueSlider.valueLow + "");
        else if (val > valueSlider.valueHigh)
            textInput.setStaticContent(valueSlider.valueHigh + "");
        textInput.setStaticContent(val + "");
        return this;
    }

    public ValueSelector setRange(float low, float high) {
        valueSlider.setRange(low, high);
        return this;
    }

    /**
     * @param val the float value to be rounded
     * @return string the formatted String to the proper decimal place
     */
    private String round(float val) {
        String str = Float.toString(val);
        if (str.contains(".")) {
            int index = str.indexOf(".") + 1;
            return str.substring(0, index + roundIndex);
        } else {
            return str;
        }
    }

    /**
     * @param digit round to number of digits after decimal point
     *              if digit is set to -1, then the decimal point
     *              is truncated. Else if it is set to other negative
     *              values then the number itself is truncated.
     */
    public ValueSelector roundTo(int digit) {
        roundIndex = digit;
        return this;
    }

    /**
     * @return the calculated float value from the slider
     */
    public float getFloatValue() {
        return valueSlider.getFloatValue();
    }

    /**
     * @return the calculated int value from the slider
     */
    public int getIntValue() {
        return valueSlider.getIntValue();
    }

    /**
     * @param runnable a segment of code that applies the value of this
     *                 value selector to a given variable.
     */
    public ValueSelector link(Runnable runnable) {
        linkedMethod = runnable;
        return this;
    }

    public TextInput getTextInput() {
        return textInput;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

}
