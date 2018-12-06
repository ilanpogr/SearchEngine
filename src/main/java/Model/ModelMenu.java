package Model;

import Controller.PropertiesFile;
import Master.Master;

import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable{

    private long start;
    private long end;

    private Master master_of_puppets;

    public ModelMenu(){
        master_of_puppets = new Master();
    }

    public int getNumOfTerms() {
        return master_of_puppets.getNumOfTerms();
    }

    public int getNumOfDocs() {
        return master_of_puppets.getNumOfDocs();
    }

    public void start(){
        start = System.currentTimeMillis();
        master_of_puppets.indexCorpus();
        end = System.currentTimeMillis();
        setChanged();
        notifyObservers("done");
    }

    public long getElapsedTime() {
        return (end - start) / 10000;
    }

}
