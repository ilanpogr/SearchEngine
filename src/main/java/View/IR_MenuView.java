package View;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.Observable;

/**
 *  the controller of the fxml -> ir_menu.
 *  observed by his controller, for each button pressed in the scene
 *  the class notifies the controller.
 */
public class IR_MenuView extends Observable {
    public Button start_bttn;
    public CheckBox stemmer_checkBox;
    public Label data_textField;
    public Label save_textField;
    public Label summary_lbl;
    public Label progress_lbl;
    public Button dict_btn;
    public Button save_btn;
    public Button browse_btn;
    public Button reset_btn;
    public ProgressBar progressbar;

    public void Start(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("start");
    }

    public void BrowseDataSet(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("browse");
    }

    public void savePostingFile_DictionaryFile(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("save");
    }

    public void Reset(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("reset");
    }

    public void showDictionary(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("show");
    }

    public void setStemMode(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("stem");
    }
}
