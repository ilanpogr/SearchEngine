package Model;

import Controller.PropertiesFile;
import Master.Master;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable {

    private Master master_of_puppets;
    private static DoubleProperty progress;

    /**
     * Ctor
     */
    public ModelMenu() {
        master_of_puppets = new Master();
    }

    /**
     * get the number of terms in corpus
     * @return integer
     */
    public int getNumOfTerms() {
        return Master.getTermCount();
    }

    /**
     * get the number of documents in corpus
     * @return integer
     */
    public int getNumOfDocs() {
        return Master.getNumOfDocs();
    }

    /**
     * Start indexing the corpus
     */
    public void start() {
        removeAllFiles();
        master_of_puppets.indexCorpus();
        setChanged();
        notifyObservers("done");
    }

    /**
     * Removes all files created by this program (depends on stem mode)
     * @return true if removed
     */
    private boolean removeAllFiles() {
        return master_of_puppets.removeAllFiles();
    }

    /**
     * Removes all files created by this program
     */
    public void reset() {
        master_of_puppets.reset();
    }

    /**
     * get path for the Dictionaries directory (depends on stem mode)
     * @return the new path
     */
    public String getDicPath() {
        return new StringBuilder(PropertiesFile.getProperty("save.files.path")).append("Dictionaries with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming\\1. Term Dictionary with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming").toString();

    }

    /**
     * getter for Model Progress property
     * @return DoubleProperty
     */
    public DoubleProperty getProgress() {
        if (progress==null){
            progress = new SimpleDoubleProperty(0);
        }
        return progress;
    }

    /**
     * sets a listener from the master's progress property
     */
    public static void setProgress() {
        Master.getProgress().addListener((observable, oldValue, newValue) -> progress.set(newValue.doubleValue()));
    }

    /**
     * Reads dictionary to RAM
     * @param dicPath - path to the dictionary
     * @return true if was able to read
     */
    public boolean readDictionary(String dicPath) {
        return Master.readDictionary(dicPath);
    }
}
