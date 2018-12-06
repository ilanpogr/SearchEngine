package Controller;

import Model.ModelMenu;
import javafx.fxml.FXMLLoader;
import View.IR_MenuView;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private ModelMenu ir_modelMenu;
    private String[] propertyKeys = {"data.set.path", "save.files.path"};

    private boolean savePath = false;
    private boolean dataPath = false;


    public ControllerMenu() {
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
        ir_menuView = fxmlLoader.getController();
        ir_menuView.summary_lbl.setText("Summary:");
        ir_modelMenu = new ModelMenu();
        ir_modelMenu.addObserver(this);
        ir_menuView.addObserver(this);
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
                PropertiesFile.putProperty(propertyKeys[0], selectedDirectory.getAbsolutePath());
                boolean corpusCheck = new File(selectedDirectory.getAbsolutePath(), "corpus").exists();
                boolean stopWords = new File(selectedDirectory.getAbsolutePath(), "stop_words.txt").exists();
                if ((!corpusCheck && !stopWords)) {
                    ir_menuView.data_textField.setText("stop_words.txt and corpus folder are not existing in this path");
                    ir_menuView.start_bttn.setDisable(false);
                } else if (!corpusCheck) {
                    ir_menuView.data_textField.setText("corpus folder not existing in this path");
                    ir_menuView.start_bttn.setDisable(false);
                } else if (!stopWords) {
                    ir_menuView.data_textField.setText("stop_words.txt not existing in this path");
                    ir_menuView.start_bttn.setDisable(false);
                } else {
                    if (selectedDirectory.getAbsolutePath().endsWith("\\"))
                        PropertiesFile.putProperty(propertyKeys[0], selectedDirectory.getAbsolutePath());
                    else
                        PropertiesFile.putProperty(propertyKeys[0], selectedDirectory.getAbsolutePath() + "\\");
                    ir_menuView.data_textField.setText(PropertiesFile.getProperty(propertyKeys[0]));
                    dataPath = true;
                    checkIfCanStart();
                }
            } else {
                if (selectedDirectory.getAbsolutePath().endsWith("\\"))
                    PropertiesFile.putProperty(propertyKeys[1], selectedDirectory.getAbsolutePath());
                else
                    PropertiesFile.putProperty(propertyKeys[1], selectedDirectory.getAbsolutePath() + "\\");
                ir_menuView.save_textField.setText(PropertiesFile.getProperty(propertyKeys[1]));
                savePath = true;
                checkIfCanStart();
            }
        }
    }

    private void checkIfCanStart() {
        if (dataPath && savePath) {
            ir_menuView.start_bttn.setDisable(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o.equals(ir_menuView)) {
            if (arg.equals("start")) {
                if (ir_menuView.stemmer_checkBox.isSelected()) {
                    PropertiesFile.putProperty("stem.mode", "1");
                }
                setSceneBeforeStart();
                Thread thread = new Thread(){
                    public void run(){
                        ir_modelMenu.start();
                    }
                };
                thread.start();
            } else if (arg.equals("browse")) {
                loadPathFromDirectoryChooser(0);
            } else if (arg.equals("save")) {
                loadPathFromDirectoryChooser(1);
            } else if (arg.equals("reset")) {
                PropertiesFile.resetProperties(propertyKeys);
                ir_menuView.save_textField.setText("");
                ir_menuView.data_textField.setText("");
                ir_menuView.start_bttn.setDisable(true);
            }
        } if (o.equals(ir_modelMenu)){
            if (arg.equals("done"))
            addSummaryToLabel();
        }
    }

    private void setSceneBeforeStart() {
        ir_menuView.summary_lbl.setAlignment(Pos.CENTER);
        ir_menuView.summary_lbl.setText("IN PROCESS!!");
        ir_menuView.start_bttn.setDisable(true);
        ir_menuView.dict_btn.setDisable(true);
        ir_menuView.browse_btn.setDisable(true);
        ir_menuView.save_btn.setDisable(true);
    }

    private void addSummaryToLabel() {
        ir_menuView.start_bttn.setDisable(false);
        ir_menuView.dict_btn.setDisable(false);
        ir_menuView.browse_btn.setDisable(false);
        ir_menuView.save_btn.setDisable(false);
        ir_menuView.summary_lbl.setAlignment(Pos.TOP_LEFT);
        String time = "Time for the whole operation: " + ir_modelMenu.getElapsedTime() + " seconds";
        String numOfterms = "Total number of term: " + ir_modelMenu.getNumOfTerms();
        String numOfDocs = "Total number of Documents: " + ir_modelMenu.getNumOfDocs();
        String summary = "Summary:\n" +
                "\t" + time + "\n" +
                "\t" + numOfterms + "\n" +
                "\t" + numOfDocs;
        ir_menuView.summary_lbl.setText(summary);

    }

    public void showStage() {
        stage.show();
    }
}
