package Model;

import Controller.PropertiesFile;
import Master.Master;
import Searcher.QueryDic;
import Searcher.QuerySol;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.split;

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
        removeDicsDir();
        master_of_puppets.indexCorpus();
        setChanged();
        notifyObservers("index_done");
    }

    /**
     * Removes all files created by this program (depends on stem mode)
     *
     * @return true if removed
     */
    private boolean removeDicsDir() {
        return master_of_puppets.removeDicsDir();
    }

    /**
     * Removes all files created by this program
     */
    public void reset() { // TODO: 01/01/2019 check that all files are deleted as needed
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
     *
     * @param query  - the query title
     * @param cities - an Array containing the cities the user chose
     *               - if the user didn't chose any, the Array is empty
     */
    public void search(String query, ArrayList<String> cities) {
        ArrayList<QuerySol> querySols = new ArrayList<>();
        StringBuilder q = new StringBuilder("000|");
        q.append(query).append("|s|s|");
        querySols.add(new QuerySol(q.toString(),-1));
        master_of_puppets.multiSearch(querySols, cities);
        docsResult = querySols;
        readEntities();
        setChanged();
        notifyObservers("search_done");
    }

    /**
     * search a query file that may contain more than one query (containing: title, description, and nerrative)
     * preparing the queries file as the master need to the input.
     * saving the search results to docResult class's object.
     * notifies the observer when search is finished
     *
     * @param cities - an Array containing the cities the user chose
     *               - if the user didn't chose any, the Array is empty
     */
    public void multiSearch(ArrayList<String> cities) {
        File file = new File(PropertiesFile.getProperty("queries.file.path"));
        ArrayList<QuerySol> querySols = QueryDic.getInstance().readQueries(file.getAbsolutePath());
        master_of_puppets.multiSearch(querySols, cities);
        docsResult = querySols;
        readEntities();
        setChanged();
        notifyObservers("search_done");
    }

    /**
     * returns HashMap- key: docNum, value: string array containing the entities.
     *
     * @return : docsEntities containing the entities info
     */
    public HashMap<String, String[]> getDocsEntities() {
        return docsEntitites;
    }

    /**
     * returns a list of all the corpus documents's cities representation
     *
     * @return : list of the cities
     */
    public ArrayList<String> getCitiesList() {
        ArrayList<String> cities =  master_of_puppets.getCitiesList();
        ArrayList<String> citiesToRemove = new ArrayList<>();
        for (String s : cities){
            if (!isAlpha(String.valueOf(s.charAt(0)))){
                citiesToRemove.add(s);
            }
        }
        for (String s: citiesToRemove){
            cities.remove(s);
        }
        return cities;
    }

    /**
     * creates a list representing the query as first index that containing list of relevant docs as the result
     *
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
     *
     * @return : list result
     */
    public ArrayList<QuerySol> getDocsResult() {
        return docsResult;
    }

    /**
     * reading the docs entities from a saved file in the relevant path
     * depending on the path the master will give (from properties file)
     */
    private void readEntities() {
        // TODO: 31/12/2018 : finish the implementation
        File file = new File(getDicsPath() + "\\Entities");
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            for (QuerySol querySol : docsResult) {
                ArrayList<String> docRes = querySol.getSols();
                for (String doc : docRes) {
                    String pointer = master_of_puppets.getEntitiesPointerFromDocNum(doc);
                    int jumpInBytes = Integer.parseInt(pointer, 36);
                    raf.seek(jumpInBytes);
                    String line = raf.readLine();
                    String[] entitiesAndTF = split(line, '|');
                    String[] entities = new String[entitiesAndTF.length / 2];
                    int j = 0;
                    for (int i = 0; i < entitiesAndTF.length; i = i + 2) {
                        entities[j++] = entitiesAndTF[i] + " --> frequency: " + entitiesAndTF[i + 1];
                    }
                    docsEntitites.put(doc,entities);
                }
            }
        } catch (IOException e) {
            System.out.println("problem with reading entities file");
        }
    }

    /**
     * reads Languages file that saved after indexing,
     * and creating a list containing the languages from the corpus.
     *
     * @return : languages list
     */
    public TreeSet<String> getLanguages() {
        File file = new File(getDicsPath() + "\\Languages");
        TreeSet<String> languages = new TreeSet<>();
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

    public void readFailed() {
        setChanged();
        notifyObservers("read_done");
    }

    public void readDone() {
        setChanged();
        notifyObservers("read_fail");
    }
}
