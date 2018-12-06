package Master;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
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
    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static StringBuilder stringBuilder = new StringBuilder();
    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static TreeMap<String, String> termDictionary = new TreeMap<>();
    private static TreeMap<String, String> cache = new TreeMap<>();
    private static ArrayList<Doc> filesList;
    private static boolean isStemMode = setStemMode();

    private static String currDocName;

    private static boolean setStemMode() {
        String stem = PropertiesFile.getProperty("stem.mode");
        if (stem.equalsIgnoreCase("0")){
            return false;
        } else {
            return true;
        }
    }
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

    public void indexCorpus() {
        int i=0;
        try {
//            PropertiesFile.putProperty("save.files.path", "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\tmp-run\\writerDir\\");
//            PropertiesFile.putProperty("data.set.path", "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus");
            String s = PropertiesFile.getProperty("data.set.path")+"corpus\\";
            ReadFile readFile = new ReadFile(PropertiesFile.getProperty("data.set.path")+"corpus\\");
            Indexer indexer = new Indexer();
            filesList = new ArrayList<>();
            Parse p = new Parse();
            System.out.println("READING, PARSING..");
            while (ReadFile.hasNextFile()) {
                i++;
                filesList = readFile.getFileList();
                for (Doc aFilesList : filesList) {
                    currDocName = aFilesList.docNum();
                    HashMap<String, String> map = p.parse(aFilesList.text());
                    handleFile(map);
                }
                if (i % (fileNum / tmpFileNum) == 0 || i == fileNum) {
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    tmpTermDic.clear();
                }
            }
            System.out.println("MERGING..");
            indexer.mergePostingTempFiles();
            indexer.writeToDictionary(new TreeMap<>(DocDic), "3. Documents Dictionary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * handles each document,(if checked) Stemmers it and Merges it.
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
                stringBuilder.append(isUpperCase ? "1" : "0").append(termSeperator);
            }

            stringBuilder.append(currDocName).append(fileDelimiter).append(term.getValue());
            tmpTermDic.put(termKey, stringBuilder.toString());
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currDocName, "" + maxTf + "," + length+ "," + filesList.get(docNum++).getFileName());
        map.clear();
    }

    /**
     * get how many terms are currently in the Dictionary
     * @return the size of the dictionary
     */
    public static int getTermCount() {
        return termDictionary.size();
    }

    /**
     * removes a single term from the dictionary
     * @param term
     */
    public static void removeFromDictionary(String term) {
        termDictionary.remove(term);
    }

    public int getNumOfTerms() {
        return termDictionary.size();
    }

    public static int getNumOfDocs() {
        return DocDic.size();
    }





    public static int getDocCount() {
        return DocDic.size();
    }

    public static void addToCacheDictionary(String minTerm, String s) {
        cache.put(minTerm, s);
    }

    public static void addToFinalTermDictionary(String minTerm, String s) {
        termDictionary.put(minTerm, s);

    }
    //    public static void writeToFreeSpace(Indexer indexer) {
//        indexer.writeToDictionary(termDictionary, "1. Term Dictionary");
//        indexer.writeToDictionary(cache, "2. Cache Dictionary");
//        cache.clear();
//        termDictionary.clear();
//    }
}
