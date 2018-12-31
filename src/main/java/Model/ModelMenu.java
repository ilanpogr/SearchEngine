package Model;

import Controller.PropertiesFile;
import Master.Master;
import Searcher.QueryDic;
import Searcher.QuerySol;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

/**
 * This class is linking between the controller and the brain of this project (Master Class)
 */
public class ModelMenu extends Observable {

    private HashMap<String, String[]> docsEntitites = new HashMap<>();
    private ArrayList<QuerySol> docsResult = new ArrayList<>();

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
        notifyObservers("index_done");
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

    /**
     * search a single query (only title), preparing the query as the master need to the input.
     * saving the search results to docResult class's object.
     * notifies the observer when search is finished
     * @param query - the query title
     * @param cities - an Array containing the cities the user chose
     *               - if the user didn't chose any, the Array is empty
     */
    public void search(String query, ArrayList<String> cities) {
        ArrayList<QuerySol> querySols = new ArrayList<>();
        StringBuilder q = new StringBuilder("000|");
        q.append(query).append("|s|s|");
        querySols.add(new QuerySol(q.toString()));
        master_of_puppets.freeLangSearch(querySols.get(0), cities);
        docsResult = querySols;
        setChanged();
        notifyObservers("search_done");
    }

    /**
     * search a query file that may contain more than one query (containing: title, description, and nerrative)
     * preparing the queries file as the master need to the input.
     * saving the search results to docResult class's object.
     * notifies the observer when search is finished
     * @param cities - an Array containing the cities the user chose
     *               - if the user didn't chose any, the Array is empty
     */
    public void multiSearch(ArrayList<String> cities) {
        File file = new File(PropertiesFile.getProperty("queries.file.path"));
        ArrayList<QuerySol> querySols = QueryDic.getInstance().readQueries(file.getAbsolutePath());
        master_of_puppets.multiSearch(querySols, cities);
        docsResult = querySols;
        setChanged();
        notifyObservers("search_done");
    }

    /**
     * returns HashMap- key: docNum, value: string array containing the entities.
     * @return : docsEntities containing the entities info
     */
    public HashMap<String, String[]> getDocsEntities() {
        return docsEntitites;
    }

    /**
     * returns a list of all the corpus documents's cities representation
     * @return : list of the cities
     */
    public ArrayList<String> getCitiesList() {
        return master_of_puppets.getCitiesList();
    }

    /**
     * creates a list representing the query as first index that containing list of relevant docs as the result
     * @return : the list of lists
     */
    public ArrayList<ArrayList<String>> getDocsResultsAsArray() {
        ArrayList<ArrayList<String>> res = new ArrayList<>(docsResult.size());
        for (QuerySol querySol : docsResult) {
            ArrayList<String> docRes = querySol.getSols();
            res.add(docRes);
        }
        return res;
    }

    /**
     * returns list of QuerySols (containing all the solution info without any filtering)
     * as result for the queries file/single search
     * @return : list result
     */
    public ArrayList<QuerySol> getDocsResult() {
        return docsResult;
    }

    /**
     * reading the docs entities from a saved file in the relevant path
     * depending on the path the master will give (from properties file)
     */
    public void readEntities() {
        String path = ""; // TODO: change the path for the file after shawn will create it
        File file = new File(path);
        try {
            // work with docsEntitites
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                System.out.println("implement ModelMenu.readEntities");
            }
        } catch (IOException e) {
            System.out.println("Entities file not found");
        }
    }

    /**
     * reads Languages file that saved after indexing,
     * and creating a list containing the languages from the corpus.
     * @return : languages list
     */
    public ArrayList<String> getLanguages() {
        File file = new File(getDicsPath() + "\\Languages");
        ArrayList<String> languages = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                languages.add(st);
            }
        } catch (IOException e) {
            System.out.println("Languages file not found");
        }
        return languages;
    }
}
