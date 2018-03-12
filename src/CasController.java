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
    public Label nodesBefore;
    public Label complexityBefore;
    public Label complexityAfter;
    public Label nodesAfter;
    public Label errMsg;
    public Label val;
    public Label vars;

    public void textFieldInput(KeyEvent keyEvent) {
        System.out.println(keyEvent.getEventType());
        simplified.setVisible(true);
        beautified.setVisible(true);
        addition.setVisible(true);
        exponential.setVisible(true);
        nodesBefore.setVisible(true);
        complexityBefore.setVisible(true);
        complexityAfter.setVisible(true);
        nodesAfter.setVisible(true);
        errMsg.setVisible(true);
        val.setVisible(true);
        vars.setVisible(true);
        System.out.println(input.textProperty().get());
        System.out.println(input.textProperty().get());
    }

    void error(String msg) {
        String str = "...";
        this.addition.setText(str);
        this.exponential.setText(str);
        this.nodesBefore.setText(str);
        this.complexityBefore.setText(str);
        this.simplified.setText(str);
        this.beautified.setText(str);
        this.complexityAfter.setText(str);
        this.nodesAfter.setText(str);
        errMsg.setText(msg);
    }
}
