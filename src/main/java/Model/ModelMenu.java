package Model;

import Controller.PropertiesFile;
import Master.Master;

import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable {

    private StringBuilder pathBuilder;
    private Master master_of_puppets;

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
        master_of_puppets.indexCorpus();
        setChanged();
        notifyObservers("done");
    }

    public boolean removeAllFiles() {
        return master_of_puppets.removeAllFiles();
    }

    public void reset() {
        master_of_puppets.reset();
    }

    public String getDicPath() {
        pathBuilder = new StringBuilder(PropertiesFile.getProperty("save.files.path")).append("Dictionaries without stemming\\1. Term Dictionary with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming");
        return pathBuilder.toString();
    }
}
