package Master;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Model.ModelMenu;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.substring;

/**
 * The Master Controls the whole Process of indexing the corpus
 */
public class Master {
    private static int fileNum = getPropertyAsInt("number.of.files");
    private static int tmpFileNum = getPropertyAsInt("number.of.temp.files");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String termSeparator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static String targetPath;
    private static StringBuilder stringBuilder = new StringBuilder();
    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static TreeMap<String, String> termDictionary = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> cache = new TreeMap<>(String::compareToIgnoreCase);
    private static ArrayList<Doc> filesList;
    private static boolean isStemMode = setStemMode();

    private static String currDocName;
    private static DoubleProperty currentStatus = new SimpleDoubleProperty(0);

    /**
     * sets the stem mode from properties file
     */
    private static boolean setStemMode() {
        String stem = PropertiesFile.getProperty("stem.mode");
        if (stem.equalsIgnoreCase("0")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * is there a need to use stemmer?
     *
     * @return yes or no.
     */
    public static boolean isStemMode() {
        return isStemMode;
    }

    /**
     * get a Property from properties file and convert it to int.
     * if it can't convert to Integer, it will return 5.
     *
     * @param s - the value of the property
     * @return the value of the property
     */
    private static int getPropertyAsInt(String s) {
        try {
            return Integer.parseInt(PropertiesFile.getProperty(s));
        } catch (Exception e) {
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 5;
        }
    }

    public static void setCurrentStatus(double indexStatus) {
        if (currentStatus.get() - 1 < indexStatus)
            currentStatus.set(indexStatus + 1);
    }

    /**
     * the Main program of the master.
     * this is where the master manages the other classes and indexes the corpus.
     */
    public void indexCorpus() {
        double tmpFileIndex = 0;
        double i = 0;
        try {
            ModelMenu.setProgress();
            isStemMode = setStemMode();
            String s = PropertiesFile.getProperty("data.set.path") + "corpus\\";
            targetPath = PropertiesFile.getProperty("save.files.path");
            ReadFile readFile = new ReadFile(s);
            fileNum = getPropertyAsInt("number.of.files");
            tmpFileNum = getPropertyAsInt("number.of.temp.files");
            double tmpChunkSize = Double.min(Integer.max(fileNum / tmpFileNum, 1),fileNum);
            Indexer indexer = new Indexer();
            filesList = new ArrayList<>();
            Parse p = new Parse();
            System.out.print("READING, PARSING, ");
            tmpFileIndex++;
            int nextTmpFileIndex = (int) (tmpFileIndex * tmpChunkSize);
            while (ReadFile.hasNextFile()) {
                i++;
                filesList = readFile.getFileList();
                for (Doc aFilesList : filesList) {
                    currDocName = aFilesList.docNum();
                    HashMap<String, String> map = p.parse(aFilesList.text());
                    handleFile(map);
                }
                currentStatus.set(i/fileNum);
                if ((i == nextTmpFileIndex && tmpFileIndex < tmpFileNum) || i == fileNum) {
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    tmpTermDic.clear();
                    tmpFileIndex++;
                    nextTmpFileIndex = (int) (tmpFileIndex * (tmpChunkSize));
                }
            }
            System.out.println("MERGING");
            indexer.mergePostingTempFiles();
            indexer.writeToDictionary(new TreeMap<>(DocDic), "3. Documents Dictionary");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PropertiesFile.putProperty("save.files.path", targetPath);
        }
    }

    /**
     * handles each document,(if checked) Stemmers it and Merges it.
     *
     * @param parsedDic - A Dictionary of a single parsed document
     */
    private static void handleFile(HashMap<String, String> parsedDic) {
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            HashMap<String, String> stemmed = stemmer.stem(parsedDic);
            mergeDicts(stemmed);
        } else {
            parsedDic.replaceAll((key, value) -> value = substring(value, 2));
            mergeDicts(parsedDic);
        }
    }

    /**
     * Merging the Dictionary of a single Document into the Main Dictionaries
     *
     * @param map - the Dictionary that will be merged
     */
    private static void mergeDicts(HashMap<String, String> map) {
        int maxTf = 0, length = 0, docNum = 0;
        for (Map.Entry<String, String> term : map.entrySet()
        ) {
            stringBuilder.setLength(0);

            if (term.getKey().length() < 1) continue;

            int termFrequency = countMatches(term.getValue(), Stemmer.getStemDelimiter().charAt(0));
            String termKey = lowerCase(term.getKey());
            boolean isUpperCase = false;
            if (termFrequency == 0)
                termFrequency++;
            termFrequency += countMatches(term.getValue(), Parse.getGapDelimiter().charAt(0));
            if (Character.isUpperCase(term.getKey().charAt(0)) || Character.isUpperCase(term.getKey().charAt(termKey.length() - 1))) {
                isUpperCase = true;
            }
            if (tmpTermDic.containsKey(termKey)) {
                if (!isUpperCase) {
                    tmpTermDic.replace(termKey, "0" + substring(tmpTermDic.get(termKey), 1));
                }
                stringBuilder.append(tmpTermDic.get(termKey)).append(fileDelimiter);
            } else {
                stringBuilder.append(isUpperCase ? "1" : "0").append(termSeparator);
            }

            stringBuilder.append(currDocName).append(fileDelimiter).append(term.getValue());
            tmpTermDic.put(termKey, stringBuilder.toString());
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currDocName, "" + maxTf + "," + length + "," + filesList.get(docNum++).getFileName());
        map.clear();
    }

    /**
     * get how many terms are currently in the Dictionary
     *
     * @return the size of the dictionary
     */
    public static int getTermCount() {
        return Indexer.getTermCounter();
    }

    /**
     * removes a single term from the dictionary
     *
     * @param term
     */
    public static void removeFromDictionary(String term) {
        termDictionary.remove(term);
    }

    /**
     * get the number of documents
     * @return int - number of documents
     */
    public static int getDocCount() {
        return DocDic.size();
    }

    /**
     * add a term to cache
     * @param term - the term
     * @param mostRelevantPartOfPosting - the Most Relevant Part Of the term's Posting
     */
    public static void addToCacheDictionary(String term, String mostRelevantPartOfPosting) {
        cache.put(term, mostRelevantPartOfPosting);
    }

    /**
     * add a term to the dictionary
     * @param term - the term
     * @param details - the term's details
     */
    public static void addToFinalTermDictionary(String term, String details) {
        termDictionary.put(term, details);
    }

    /**
     * removes All files associated with the current stem mode
     *
     * @return true iff deleted the directory
     */
    public boolean removeAllFiles() {
        clear();
        return new Indexer().removeAllFiles();
    }

    /**
     * deletes all of the saved files by the Master
     */
    public void reset() {
        clear();
        termDictionary = new TreeMap<>(String::compareToIgnoreCase);
        cache = new TreeMap<>(String::compareToIgnoreCase);
        Indexer.reset();
    }

    /**
     * clears memory. returns all states back to the beginning. (except stemmer cache..)
     */
    public void clear() {
        fileNum = getPropertyAsInt("number.of.files");
        tmpFileNum = getPropertyAsInt("number.of.temp.files");
        stringBuilder = new StringBuilder();
        DocDic = new LinkedHashMap<>();
        tmpTermDic = new LinkedHashMap<>();
        isStemMode = setStemMode();
        currentStatus.set(0);
        Indexer.clear();
        ReadFile.clear();
    }

    /**
     * get the status Property from the master
     * @return Status as DoubleProperty
     */
    public static DoubleProperty getProgress() {
        return currentStatus;
    }

    /**
     * Read Dictionary to RAM
     * @param dicPath - dictionary's path
     * @return true if was able to read
     */
    public static boolean readDictionary(String dicPath) {
        termDictionary = ReadFile.readDictionary(dicPath,termSeparator);
        return termDictionary!=null;
    }
}
