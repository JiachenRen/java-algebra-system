

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jmc.cas.*;
import jmc.cas.Compiler;

import java.util.NoSuchElementException;

/**
 * Created by Jiachen on 3/2/18.
 * JMC Computer Algebra System user interface model
 */
public class CAS extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cas.fxml"));
        Parent root = fxmlLoader.load();
        CasController controller = fxmlLoader.getController();

        SimpleStringProperty expression = new SimpleStringProperty("");
        expression.bind(controller.input.textProperty());
        expression.addListener((observable, oldValue, newValue) -> {
            try {
                Operable operable = Compiler.compile(newValue);
                String addExp = "";
                if (operable instanceof Operation) {
                    Operation operation = (Operation) operable;
                    addExp = operation.copy().toAdditionOnly().toExponentialForm().toString();
                }
                controller.additionOnlyExp.setText(addExp);

                int nodes1 = operable.numNodes(), complexity1 = operable.complexity();
                controller.nodesBefore.setText(String.valueOf(nodes1));
                controller.complexityBefore.setText(String.valueOf(complexity1));

                operable = operable.simplify();
                controller.simplified.setText(operable.toString()); //should conform to MVC, setText should be in controller.

                controller.expanded.setText(operable.copy().expand().simplify().beautify().toString());

                operable = operable.beautify();
                controller.beautified.setText(operable.toString());

                try {
                    controller.vars.setText(Operable.extractVariables(operable)
                            .stream().map(Variable::getName)
                            .reduce((a, b) -> a + ", " + b).get());
                } catch (NoSuchElementException e) {
                    controller.vars.setText("None");
                }


                controller.val.setText(String.valueOf(operable.val()));

                controller.complexityAfter.setText(String.valueOf(operable.complexity()));
                controller.nodesAfter.setText(String.valueOf(operable.numNodes()));
                controller.errMsg.setText("\"\"");
            } catch (JMCException e) {
                controller.error(e.getMessage());
            }
        });
        primaryStage.setTitle("JMC Computer Algebra System");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.show();
    }

}
