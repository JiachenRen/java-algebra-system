

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jmc.cas.Expression;
import jmc.cas.Operable;
import jmc.cas.Operation;

/**
 * Created by Jiachen on 3/2/18.
 * JMC Computer Algebra System user interface model
 */
public class CAS extends Application {
    private SimpleStringProperty expression;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cas.fxml"));
        Parent root = fxmlLoader.load();
        CasController controller = fxmlLoader.getController();

        expression = new SimpleStringProperty("");
        expression.bind(controller.input.textProperty());
        expression.addListener((observable, oldValue, newValue) -> {
            System.out.println(oldValue + " " + newValue);
            Operable operable = Expression.interpret(newValue);
            controller.addition.setText(operable instanceof Operation ? ((Operation) operable).copy().toAdditionOnly().toString() : operable.toString());
            controller.exponential.setText(operable instanceof Operation ? ((Operation) operable).copy().toExponentialForm().toString() : operable.toString());
            controller.simplified.setText(operable.copy().simplify().toString()); //should conform to MVC, setText should be in controller.
            controller.beautified.setText(operable.copy().simplify().beautify().toString());
        });
        primaryStage.setTitle("JMC Computer Algebra System");
        primaryStage.setScene(new Scene(root, 400, 200));
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(400);
        primaryStage.show();
    }
}
