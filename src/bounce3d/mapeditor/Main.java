package bounce3d.mapeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        MainController controller = new MainController(primaryStage);
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);

        Parent root = loader.load(ClassLoader.getSystemClassLoader().getResourceAsStream("main.fxml"));
        primaryStage.setTitle("Rhythm Planet Note Editor");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        controller.init();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
