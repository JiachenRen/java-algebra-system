package jui_lib;

/**
 * Created by Jiachen on 26/04/2017.
 */
public class SpaceHolder extends Displayable {
    public SpaceHolder(float relativeW, float relativeH) {
        super(relativeW, relativeH);
        init();
    }

    public SpaceHolder() {
        super();
        init();
    }

    public SpaceHolder(float x, float y, float w, float h) {
        super(x, y, w, h);
        init();
    }

    private void init() {
        setVisible(false);
    }

    @Override
    public void display() {

    }
}
