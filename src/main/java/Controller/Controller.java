package Controller;

import Indexer.Indexer;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.CityInfo;
import TextContainers.Doc;
import TextContainers.LanguagesInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Controller {
    private static int fileNum = getPropertyAsInt("number.of.files");
    private static int tmpFileNum = getPropertyAsInt("number.of.temp.files");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static StringBuilder stringBuilder = new StringBuilder();
    private static TreeMap<String, String> cache = new TreeMap<>();
    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static TreeMap<String, String> termDictionary = new TreeMap<>();
    private static boolean isStemMode = false;
    private static ArrayList<Doc> filesList;

    private static String targetDirPath;
    private static String corpusPath;
    private static String currPath;

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

    /**
     * get the Path to the Directory that file will be written to
     *
     * @return String path
     */
    public static String getTargetDirPath() {
        return targetDirPath;
    }

    public static void main(String[] args) {
        double mainStartTime = System.currentTimeMillis();
        int j = 0, f = 0, ii = 0, term_count = 0;
        try {
            targetDirPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\tmp-run\\writerDir\\";
            corpusPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
            filesList = new ArrayList<>();
            ReadFile readFile = new ReadFile(corpusPath);
            Parse p = new Parse();
            Indexer indexer = new Indexer();
            double fileparse = 0;
            double singleparse = 0;
            while (readFile.hasNextFile()) {
                f++;
                double read = System.currentTimeMillis();
                filesList = readFile.getFileList();
                for (int i = 0; i < filesList.size(); i++) {
                    double parsestart = System.currentTimeMillis();
                    currPath = filesList.get(i).docNum();
                        HashMap<String, String> map = p.parse(filesList.get(i).text());

                        handleFile(map);

                    double parseend = System.currentTimeMillis();
                    singleparse = (parseend - read) / 1000;
                    fileparse += (parseend - parsestart) / 1000;
                    ii = i;
                    j++;

                }
                if (f % (fileNum / tmpFileNum) == 0 || f == fileNum) {
                    term_count += tmpTermDic.size();
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    tmpTermDic.clear();
                    System.out.println("Time took to read and parse file: " + currPath + ": " + singleparse + " seconds. \t Total read and parse time: " + (int) fileparse / 60 + ":" + ((fileparse % 60 < 10) ? "0" : "") + (int) fileparse % 60 + " seconds. \t (number of documents: " + (j) + ",\t number of files: " + f + ")\t\t\tSize of Dictionary before merging: " + term_count);
                }
                filesList.clear();
//                if (f == 9) break;
            }
            indexer.mergePostingTempFiles(targetDirPath);
            double s = System.nanoTime();
            indexer.writeToDictionary(new TreeMap<>(DocDic), "3. Documents Dictionary");
//            indexer.writeToDictionary(termDictionary,"1. Term Dictionary");
//            indexer.writeToDictionary(cache, "2. Cache Dictionary");
            System.out.println(System.nanoTime() - s);

            int total = (int) ((System.currentTimeMillis() - mainStartTime) / 1000);
            System.out.println("\nTime took to run main: " + total / 60 + ":" + (total % 60 < 10 ? "0" : "") + total % 60 + " seconds");
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ++ii);
            e.printStackTrace();
        }
//        LanguagesInfo l = LanguagesInfo.getInstance();
//        CityInfo c = CityInfo.getInstance();
//        System.out.println();
//        l.printLanguages();
//        System.out.println();
//        c.printCities();
//        HashSet cities = new HashSet(CityInfo.getInstance().getCitiesNotAPI());
//        cities.forEach(System.out::println);
    }

    private static void handleFile(HashMap<String, String> parsedDic) {
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            HashMap<String, String> stemmed = stemmer.stem(parsedDic);
            mergeDicts(stemmed);
        } else {
            parsedDic.replaceAll((k, v) -> v = substring(v, 2));
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

            stringBuilder.append(currPath).append(fileDelimiter).append(term.getValue());
            tmpTermDic.put(isUpperCase ? term.getKey() : termKey, stringBuilder.toString());
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currPath, "" + maxTf + "," + length+ "," + filesList.get(docNum++).getFileName());
        map.clear();
    }

    public static int getTermCount() {
        return termDictionary.size();
    }

    public static void addToFinalTermDictionary(String minTerm, String s) {
        termDictionary.put(minTerm, s);

    }

    public static void addToCacheDictionary(String minTerm, String s) {
        cache.put(minTerm, s);
    }

    public static int getDocCount() {
        return DocDic.size();
    }

    public static void removeFromDictionary(String minTerm) {
        termDictionary.remove(minTerm);
    }

    public static void writeToFreeSpace(Indexer indexer) {
        indexer.writeToDictionary(termDictionary, "1. Term Dictionary");
        indexer.writeToDictionary(cache, "2. Cache Dictionary");
        cache.clear();
        termDictionary.clear();
    }
}
