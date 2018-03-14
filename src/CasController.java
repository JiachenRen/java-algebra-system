import javafx.application.Platform;
import javafx.event.ActionEvent;
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
    public Label additionOnlyExp;
    public Label nodesBefore;
    public Label complexityBefore;
    public Label complexityAfter;
    public Label nodesAfter;
    public Label errMsg;
    public Label val;
    public Label vars;
    public Label expanded;

    public void textFieldInput(KeyEvent keyEvent) {
        System.out.println(keyEvent.getEventType());
        simplified.setVisible(true);
        beautified.setVisible(true);
        additionOnlyExp.setVisible(true);
        nodesBefore.setVisible(true);
        complexityBefore.setVisible(true);
        complexityAfter.setVisible(true);
        nodesAfter.setVisible(true);
        errMsg.setVisible(true);
        val.setVisible(true);
        vars.setVisible(true);
        expanded.setVisible(true);
        System.out.println(input.textProperty().get());
        System.out.println(input.textProperty().get());
    }

    public void launchJGrapher(ActionEvent actionEvent) {
        System.out.println(actionEvent);
        Platform.runLater(() -> JGrapher.main(new String[]{"processing.awt.PGraphicsJava2D"}));
    }

    void error(String msg) {
        String str = "...";
//        this.addition.setText(str);
//        this.exponential.setText(str);
        this.nodesBefore.setText(str);
        this.complexityBefore.setText(str);
//        this.simplified.setText(str);
//        this.beautified.setText(str);
        this.complexityAfter.setText(str);
        this.nodesAfter.setText(str);
        errMsg.setText(msg);
    }
}
