import Controller.ControllerMenu;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Class of the Search Engine
 */
public class Main extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ControllerMenu controllerMenu = new ControllerMenu();
        controllerMenu.showStage();
    }
}
