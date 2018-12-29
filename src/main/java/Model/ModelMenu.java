package Model;

import Controller.PropertiesFile;
import Master.Master;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    public String getDicsPath() {
        return new StringBuilder(PropertiesFile.getProperty("save.files.path")).append("Dictionaries with").append(PropertiesFile.getProperty("stem.mode").equals("0") ? "out " : " ").append("stemming").toString();

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
    public boolean readDictionaries(String dicPath) {
        return Master.readDictionaries(dicPath);
    }

    public void bm25bnkChecker(){
        try{
            CSVFormat csvFormat;
            CSVPrinter csvPrinter;
            for (int i = 0; i < 10; i++) {
                csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withRecordSeparator(" ").withTrim();
                File file = new File("BM25 tester"+i);
                if (!file.exists()) Files.createFile(Paths.get(getDicsPath()));
                csvPrinter = new CSVPrinter(new FileWriter(file),csvFormat);
                csvPrinter.printRecord("k","b","delta","grade");
                for (int k = 0; k < 1000; k++) {

                }

            }

        }catch (Exception e){

        }
    }

    public void search(ArrayList<String> cities){
        master_of_puppets.search(cities);
        setChanged();
        notifyObservers("search_done");
    }

    public ArrayList<String> getCitiesSet() {
        return master_of_puppets.getCitiesSet();
    }
}
