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

    private StringBuilder pathBuilder;
    private Master master_of_puppets;
    private static DoubleProperty progress;


    public ModelMenu() {
        master_of_puppets = new Master();
    }

    public int getNumOfTerms() {
        return master_of_puppets.getTermCount();
    }

    public int getNumOfDocs() {
        return master_of_puppets.getNumOfDocs();
    }

    public void start() {
        removeAllFiles();
        master_of_puppets.indexCorpus();
        setChanged();
        notifyObservers("done");
    }

    private boolean removeAllFiles() {
        return master_of_puppets.removeAllFiles();
    }

    public void reset() {
        master_of_puppets.reset();
    }

    public String getDicPath() {
        pathBuilder = new StringBuilder(PropertiesFile.getProperty("save.files.path")).append("Dictionaries with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming\\1. Term Dictionary with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming");
        return pathBuilder.toString();
    }

    public DoubleProperty getProgress() {
        if (progress==null){
            progress = new SimpleDoubleProperty(0);
        }
        return progress;
    }

    public static void setProgress() {
        Master.getProgress().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                progress.set(newValue.doubleValue());
            }
        });
    }
}
