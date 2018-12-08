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
    public Button start_btn;
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
    public ComboBox docs_language;
    public Button read_dict_btn;

    /**
     * when start button is clicked
     * @param actionEvent - start button is clicked
     */
    public void Start(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("start");
    }

    /**
     * when Browse Data Set button is clicked
     * @param actionEvent - Browse Data Set button is clicked
     */
    public void BrowseDataSet(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("browse");
    }

    /**
     * when Save button is clicked
     * @param actionEvent - Save button is clicked
     */
    public void savePostingFile_DictionaryFile(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("save");
    }

    /**
     * when Reset button is clicked
     * @param actionEvent - Reset button is clicked
     */
    public void Reset(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("reset");
    }

    /**
     * when Show button is clicked
     * @param actionEvent - Show button is clicked
     */
    public void showDictionary(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("show");
    }

    /**
     * when Stem CheckBox is clicked
     * @param actionEvent - Stem CheckBox is clicked
     */
    public void setStemMode(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("stem");
    }

    /**
     * when load dictionary button is clicked
     * @param actionEvent - load dictionary button is clicked
     */
    public void loadDictToMemory(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("read");
    }
}
