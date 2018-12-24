package Master;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Model.ModelMenu;
import Parser.Parser;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * The Master Controls the whole Process of indexing the corpus
 */
public class Master {
    private static int fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
    private static int tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String termSeparator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static String targetPath;
    private static StringBuilder stringBuilder = new StringBuilder();
    private static TreeMap<String, String> docDic = new TreeMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static TreeMap<String, String> termDictionary = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> cache = new TreeMap<>(String::compareToIgnoreCase);
    private static ArrayList<Doc> filesList;
    private static boolean isStemMode = setStemMode();
    private static double avrageDocLength = 0;

    public static double getAvrageDocLength() {
        return avrageDocLength;
    }

    public static void setAvrageDocLength() {
        if (docDic == null) avrageDocLength = 0;
        try {
            ArrayList<String> vals = new ArrayList<>(docDic.values());
            double lenSum = 0;
            int i = 0;
            for (; i < vals.size(); i++) {
                lenSum += Integer.parseInt(split(vals.get(i), ",")[1]);
            }
            avrageDocLength = lenSum / i;
        } catch (Exception e) {
            //nothing
        }
    }

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
     * set the status of external class
     * @param indexStatus - the status from the indexer
     */
    public static void setCurrentStatus(double indexStatus) {
        if (currentStatus.get() - 1 < indexStatus)
            currentStatus.set(indexStatus + 1);
    }

    /**
     * Makes a Dictionary of <Term , Weight_in_Query> to the given query
     * after Parsing and (maybe) Stemming each term in the query.
     *
     * @param query - the given single query
     * @return the mentioned above dictionary
     */
    public static HashMap<String, Integer> makeQueryDic(String query) {
        Parser parser = new Parser();
        return handleQuery(parser.parse(new String[]{query}));
    }

    /**
     * cleaning the query after parsing and returns the Query-Dictionary.
     *
     * @param parsed - the map after parsing the query.
     * @return the cleaned dictionary mentioned above.
     */
    private static HashMap<String, Integer> handleQuery(HashMap<String, String> parsed) {
        HashMap<String, Integer> queryDic = new HashMap<>();
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            parsed = stemmer.stem(parsed);
        } else {
            parsed.replaceAll((key, value) -> value = substring(value, 2));
        }
        int freq;
        for (Map.Entry<String, String> term : parsed.entrySet()) {
            String word = term.getKey();
            freq = getFrequencyFromPosting(term);
            queryDic.put(word,freq);
        }
        return queryDic;
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
            fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
            tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
            double tmpChunkSize = Double.min(Integer.max(fileNum / tmpFileNum, 1), fileNum);
            Indexer indexer = new Indexer();
            filesList = new ArrayList<>();
            Parser p = new Parser();
            System.out.print("READING, PARSING, ");
            tmpFileIndex++;
            int nextTmpFileIndex = (int) (tmpFileIndex * tmpChunkSize);
            while (ReadFile.hasNextFile()) {
                i++;
                filesList = readFile.getFileList();
                for (Doc aFilesList : filesList) {
                    currDocName = aFilesList.docNum();
                    HashMap<String, String> map = p.parse(aFilesList.getAttributesToIndex());
                    handleFile(map);
                }
                currentStatus.set(i / fileNum);
                if ((i == nextTmpFileIndex && tmpFileIndex < tmpFileNum) || i == fileNum) {
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    tmpTermDic.clear();
                    tmpFileIndex++;
                    nextTmpFileIndex = (int) (tmpFileIndex * (tmpChunkSize));
                }
            }
            System.out.println("MERGING");
            indexer.mergePostingTempFiles();
            indexer.writeToDictionary(docDic, "3. Documents Dictionary");
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
     * takes a term from a map and returns the frequency of it by the positions in the value
     * @param term - the counted term from the map
     * @return the number of times the term appears
     */
    private static int getFrequencyFromPosting(Map.Entry<String, String> term) {
        return getFrequencyFromPosting(term.getValue());
    }

    public static int getFrequencyFromPosting(String positions) {
        int termFrequency = countMatches(positions, Stemmer.getStemDelimiter().charAt(0));
        if (termFrequency == 0)
            termFrequency++;
        termFrequency += countMatches(positions, Parser.getGapDelimiter().charAt(0));
        return termFrequency;
    }

    /**
     * Merging the Dictionary of a single Document into the Main Dictionaries
     *
     * @param map - the Dictionary that will be merged
     */
    private static void mergeDicts(HashMap<String, String> map) {
        int maxTf = 0, length = 0, docNum = 0;
        Doc doc =  filesList.get(docNum++);
        for (Map.Entry<String, String> term : map.entrySet()
        ) {
            stringBuilder.setLength(0);

            if (term.getKey().length() < 1) continue;

            String termKey = lowerCase(term.getKey());
            boolean isUpperCase = false;
            int termFrequency = getFrequencyFromPosting(term);
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
//            if (isUpperCase && termKey.length()>2 && isAlphanumericSpace(termKey) && !containsAny(termKey, "1234567890")) doc.addEntity(termKey,termFrequency);
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }

        doc.setMax_tf(maxTf);
        doc.setLength(length);
        stringBuilder.setLength(0);
        stringBuilder.append(maxTf).append(",").append(length).append(",").append(doc.getFileName())/*.append(",")*/;
//        stringBuilder.append(doc.appendPersonas());
        docDic.put(currDocName, stringBuilder.toString());
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
     *
     * @return int - number of documents
     */
    public static int getDocCount() {
        return docDic.size();
    }

    /**
     * add a term to cache
     *
     * @param term                      - the term
     * @param mostRelevantPartOfPosting - the Most Relevant Part Of the term's Posting
     */
    public static void addToCacheDictionary(String term, String mostRelevantPartOfPosting) {
        cache.put(term, mostRelevantPartOfPosting);
    }

    /**
     * add a term to the dictionary
     *
     * @param term    - the term
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
        fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
        tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
        stringBuilder = new StringBuilder();
        docDic = new TreeMap<>();
        tmpTermDic = new LinkedHashMap<>();
        isStemMode = setStemMode();
        currentStatus.set(0);
        Indexer.clear();
        ReadFile.clear();
    }

    /**
     * get the status Property from the master
     *
     * @return Status as DoubleProperty
     */
    public static DoubleProperty getProgress() {
        return currentStatus;
    }

    /**
     * Read Dictionaries to RAM
     *
     * @param dicPath - dictionaries' path
     * @return true if was able to read
     */
    public static boolean readDictionaries(String dicPath) {
        TreeMap<Character, TreeMap<String, String>> treeMaps = ReadFile.readDictionaries(dicPath, termSeparator);
        termDictionary = treeMaps.remove('1');
        cache = treeMaps.remove('2');
        docDic = treeMaps.remove('3');
        //todo-read cities dictionary
        setAvrageDocLength();
        return (termDictionary != null && cache != null && docDic != null);
    }

}
