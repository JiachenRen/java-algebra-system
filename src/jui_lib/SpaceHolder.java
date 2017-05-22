package jui_lib;

/**
 * Created by Jiachen on 26/04/2017.
 */
public class SpaceHolder extends Displayable {
    public SpaceHolder(String id, float relativeW, float relativeH) {
        super(id, relativeW, relativeH);
        init();
    }

    public SpaceHolder(String id) {
        super(id);
        init();
    }

    public SpaceHolder(String id, float x, float y, float w, float h) {
        super(id, x, y, w, h);
        init();
    }

    private void init(){
        setVisible(false);
    }

    @Override
    public void display(){

    }

}
