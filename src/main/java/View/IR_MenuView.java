package View;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

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
    public Button read_dict_btn;
    public CheckBox entities_checkBox;
    public CheckBox semantic_checkBox;
    public ImageView search_btn;
    public Button browse_queries_btn;
    public Button cities_btn;
    public ComboBox docs_language;
    public Label result_lbl;
    public ImageView save_results_img;
    public TreeView result_treeView;
    public TextArea entities_textArea;

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

    /**
     * when Entities CheckBox is clicked
     * @param actionEvent - Entities CheckBox is clicked
     */
    public void setEntitiesMode(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("entities");
    }

    /**
     * when Semantic CheckBox is clicked
     * @param actionEvent - Semantic CheckBox is clicked
     */
    public void setSemantic(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("semantic");
    }

    /**
     * when Choose Specific Cities Button is clicked
     * @param actionEvent - Choose Specific Cities Button is clicked
     */
    public void chooseCities(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("cities");
    }

    /**
     * when Search Image is clicked
     * @param mouseEvent - Search Image is clicked
     */
    public void searchSingleQuery(MouseEvent mouseEvent) {
        setChanged();
        notifyObservers("search_single");
    }

    /**
     * when Save Image is clicked for saving the search results
     * @param mouseEvent - Save Image is clicked
     */
    public void saveResults(MouseEvent mouseEvent) {
        setChanged();
        notifyObservers("save_results");
    }

    /**
     * when BrowseQueriesFile button is clicked for loading the queries file and start the multiSearch
     * @param actionEvent - BrowseQueriesFile button is clicked
     */
    public void BrowseQueriesFileAndSearch(ActionEvent actionEvent) {
        setChanged();
        notifyObservers("queryFile");
    }
}
