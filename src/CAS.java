

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        Parent root = FXMLLoader.load(getClass().getResource("cas.fxml"));
        primaryStage.setTitle("JMC Computer Algebra System");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
