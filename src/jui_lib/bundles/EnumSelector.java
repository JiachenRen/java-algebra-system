package jui_lib.bundles;

import jui_lib.Button;
import jui_lib.Displayable;
import jui_lib.HBox;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jiachen on 30/04/2017.
 */
public class EnumSelector extends HBox {
    private ArrayList<Button> enumStates;
    private Button currentEnumState;

    public EnumSelector(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public EnumSelector(float relativeW, float relativeH) {
        super(relativeW, relativeH);
    }

    public EnumSelector() {
        super();
        this.setMargins(0, 0);
    }

    public void init() {
        super.init();
    }

    public EnumSelector setEnumStates(String... names) {
        enumStates = new ArrayList<>();
        this.removeAll();
        Arrays.stream(names).forEach((name) -> {
            enumStates.add(new Button().setId(name).setContent(name));
        });
        this.addAll(enumStates);
        return this;
    }

    public EnumSelector addEnumState(String name) {
        Button newEnumState = new Button().setId(name).setContent(name);
        enumStates.add(newEnumState);
        this.add(newEnumState);
        return this;
    }

    /**
     * don't know if this is legal yet. TODO debug
     *
     * @param name the name of the enum state to be removed
     * @return this instance of EnumSelector
     */
    public EnumSelector removeEnumState(String name) {
        enumStates.forEach((e) -> {
            if (e.getId().equals(name)) {
                enumStates.remove(e);
                this.displayables.remove(e);
            }
        });
        return this;
    }

    public void setFocus(String name) {
        enumStates.forEach((e) -> {
            if (e.getId().equals(name))
                setFocus(e);
        });
    }

    private void setFocus(Button enumState) {
        currentEnumState = enumState;
    }

    public Button getCurrentEnumState() {
        return currentEnumState;
    }
}
