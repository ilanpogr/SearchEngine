package Model;

import Controller.PropertiesFile;
import Indexer.WrieFile;
import Master.Master;
import Searcher.QueryDic;
import Searcher.QuerySol;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable {

    private Master master_of_puppets = new Master();
    private static DoubleProperty progress;

    /**
     * get the number of terms in corpus
     *
     * @return integer
     */
    public int getNumOfTerms() {
        return Master.getTermCount();
    }

    /**
     * get the number of documents in corpus
     *
     * @return integer
     */
    public int getDocCount() {
        return Master.getDocCount();
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
     *
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
     *
     * @return the new path
     */
    public String getDicsPath() {
        return new StringBuilder(PropertiesFile.getProperty("save.files.path")).append("Dictionaries with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming").toString();

    }

    /**
     * getter for Model Progress property
     *
     * @return DoubleProperty
     */
    public DoubleProperty getProgress() {
        if (progress == null) {
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
     *
     * @param dicPath - path to the dictionary
     * @return true if was able to read
     */
    public boolean readDictionaries(String dicPath) {
        return Master.readDictionaries(dicPath);
    }


    public ArrayList<QuerySol> search(String query, ArrayList<String> lang) {
        setChanged();
        notifyObservers("search_done");
        ArrayList<QuerySol> querySols = new ArrayList<>();
        StringBuilder q = new StringBuilder("000|");
        q.append(query).append("|s|s|");
        querySols.add(new QuerySol(q.toString()));
        master_of_puppets.freeLangSearch(querySols,lang);
        return querySols;
    }

    public ArrayList<QuerySol> multiSearch(ArrayList<String> lang) {
        String path = PropertiesFile.getProperty("queries.file.path");
        ArrayList<QuerySol> querySols = QueryDic.getInstance().readQueries(path);
        master_of_puppets.multiSearch(querySols, lang);
        WrieFile.writeQueryResults(querySols, file.getParent(), "results.txt");
        return querySols;
    }



    public ArrayList<String> getCitiesList() {
        return master_of_puppets.getCitiesList();
    }
}
