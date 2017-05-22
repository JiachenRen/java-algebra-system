package jui_lib;

/**
 * Created by Jiachen on 30/04/2017.
 */
public interface MouseControl extends Controllable {
    boolean isMouseOver();

    void mouseReleased();

    void mousePressed();

    void mouseDragged();

    void mouseWheel();
}
