package Model;

import Controller.PropertiesFile;
import Master.Master;

import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable{

    private Master master_of_puppets;

    public ModelMenu(){
        master_of_puppets = new Master();
    }

    public int getNumOfTerms() {
        return master_of_puppets.getTermCount();
    }

    public int getNumOfDocs() {
        return master_of_puppets.getNumOfDocs();
    }

    public void start(){
        master_of_puppets.indexCorpus();
        setChanged();
        notifyObservers("done");
    }

    public void removeAllFiles() {
        master_of_puppets.removeAllFiles();
    }
}
