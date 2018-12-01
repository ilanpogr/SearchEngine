package view;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import java.util.Observable;

public class IR_MenuView extends Observable {
    public Button start_bttn;
    public CheckBox stemmer_checkBox;

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
}
