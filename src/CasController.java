import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * Created by Jiachen on 3/2/18.
 * CAS Controller
 */
public class CasController {


    public Label simplified;
    public TextField input;
    public Label beautified;
    public Label addition;
    public Label exponential;

    public void textFieldInput(KeyEvent keyEvent) {
        System.out.println(keyEvent.getEventType());
        simplified.setVisible(true);
        beautified.setVisible(true);
        addition.setVisible(true);
        exponential.setVisible(true);
        System.out.println(input.textProperty().get());
        System.out.println(input.textProperty().get());
    }
}
