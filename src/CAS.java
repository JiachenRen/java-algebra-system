

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jas.core.*;
import jas.core.Compiler;
import jas.core.Node;
import jas.core.operations.Operation;

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
                Node node = Compiler.compile(newValue);
                String addExp = "";
                if (node instanceof Operation) {
                    Operation operation = (Operation) node;
                    addExp = operation.copy().toAdditionOnly().toExponentialForm().toString();
                }
                controller.additionOnlyExp.setText(addExp);

                int nodes1 = node.numNodes(), complexity1 = node.complexity();
                controller.nodesBefore.setText(String.valueOf(nodes1));
                controller.complexityBefore.setText(String.valueOf(complexity1));

                node = node.simplify();
                controller.simplified.setText(node.toString()); //should conform to MVC, setText should be in controller.

                controller.expanded.setText(node.copy().expand().simplify().beautify().toString());

                node = node.beautify();
                controller.beautified.setText(node.toString());

                try {
                    controller.vars.setText(node.extractVariables()
                            .stream().map(Nameable::getName)
                            .reduce((a, b) -> a + ", " + b).get());
                } catch (NoSuchElementException e) {
                    controller.vars.setText("None");
                }


                controller.val.setText(String.valueOf(node.val()));

                controller.complexityAfter.setText(String.valueOf(node.complexity()));
                controller.nodesAfter.setText(String.valueOf(node.numNodes()));
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
