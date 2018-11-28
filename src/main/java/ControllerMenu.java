import javafx.fxml.FXMLLoader;
import View.IR_MenuView;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class ControllerMenu implements Observer {

    private FXMLLoader fxmlLoader;
    private Stage stage;
    private Parent root;

    private IR_MenuView ir_menuView;
    private ModelMenu modelMenu;
    private PropertiesFile propertiesFile;
    private String[] propertyKeys = {"data.set.path", "save.files.path"};

    private boolean isStemmer = false;

    public boolean isStemmer() {
        return isStemmer;
    }


    public ControllerMenu(String properties_file_name) {
        stage = new Stage();
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("menu/ir_menu.fxml"));
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add("menu/style_menu.css");
        stage.setScene(scene);
//        modelMenu = new ModelMenu();
        ir_menuView = fxmlLoader.getController();
        ir_menuView.addObserver(this);
        propertiesFile = new PropertiesFile(properties_file_name);
    }

    /**
     * Opens the directory chooser
     *
     * @param operation : 1 -> Save; 0 Load
     */
    private void loadPathFromDirectoryChooser(int operation) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            return;
        } else {
            if (operation == 0) {
                propertiesFile.putProperty(propertyKeys[0], selectedDirectory.getAbsolutePath());
            } else {
                propertiesFile.putProperty(propertyKeys[1], selectedDirectory.getAbsolutePath());
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o.equals(ir_menuView)) {
            if (arg.equals("start")) {
                if (ir_menuView.stemmer_checkBox.isSelected()){
                    isStemmer = true;
                    System.out.println("Stemmer option is: ON");
                } else {
                    System.out.println("Stemmer option is: OFF");
                }
            } else if (arg.equals("browse")) {
                loadPathFromDirectoryChooser(0);
                ir_menuView.start_bttn.setDisable(false);
            } else if (arg.equals("save")) {
                loadPathFromDirectoryChooser(1);
            } else if (arg.equals("reset")) {
                propertiesFile.resetProperties(propertyKeys);
                ir_menuView.start_bttn.setDisable(true);
            }
        }
    }

    public void showStage() {
        stage.show();
    }
}
