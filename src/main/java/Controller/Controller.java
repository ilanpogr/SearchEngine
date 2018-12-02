package Controller;

import Indexer.Indexer;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Controller {
    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static boolean isStemMode = true;
    private static ArrayList<Doc> filesList;
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> termDictionary = new LinkedHashMap<>();
    private static String tmpFilesPath;
    private static String currPath;
    private static StringBuilder stringBuilder = new StringBuilder();
    private static int fileNum = getPropertyAsInt("number.of.files");
    private static int tmpFileNum = getPropertyAsInt("number.of.temp.files");



    private static int getPropertyAsInt(String s) {
        try {
            return Integer.parseInt(PropertiesFile.getProperty(s));
        } catch (Exception e){
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 5;
        }
    }

    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static String corpusPath;
    private static String targetDirPath;
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");

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
                    term_count = tmpTermDic.size() + termDictionary.size();

                }
                if (f % (fileNum/tmpFileNum) == 0) {
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    termDictionary.putAll(tmpTermDic);
                    if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory() / 100)
                        termDictionary.forEach((k, v) -> termDictionary.replace(k, v, ""));
                    tmpTermDic.clear();
                }
                System.out.println("Time took to read and parse file: " + currPath + ": " + singleparse + " seconds. \t Total read and parse time: " + (int) fileparse / 60 + ":" + ((fileparse % 60 < 10) ? "0" : "") + (int) fileparse % 60 + " seconds. \t (number of documents: " + (j) + ",\t number of files: " + f + ")\t\t\tSize of Dictionary: " + tmpTermDic.size() + "\t\t\tTotal Num of Terms: " + term_count);
                filesList.clear();
                if (f == tmpFileNum) break;
            }
            indexer.mergePostingTempFiles(targetDirPath, termDictionary, cache);
            int total = (int) ((System.currentTimeMillis() - mainStartTime) / 1000);
            System.out.println("\nTime took to run main: " + total / 60 + ":" + (total % 60 < 10 ? "0" : "") + total % 60 + " seconds");
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ++ii);
            e.printStackTrace();
        }
    }

    private static void handleFile(HashMap<String, String> parsedDic) {
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            HashMap<String, String> stemmed = stemmer.stem(parsedDic);
            mergeDicts(stemmed);
        } else {
            parsedDic.forEach((key, value) -> value = substring(value, 2));
            mergeDicts(parsedDic);
        }
    }

    private static void mergeDicts(HashMap<String, String> map) {
        int maxTf = 0, length = 0;
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
                if (!isUpperCase){
                    tmpTermDic.replace(termKey,"0"+substring(tmpTermDic.get(termKey),1));
                }
                stringBuilder.append(tmpTermDic.get(termKey)).append(fileDelimiter);
            } else {
                stringBuilder.append(isUpperCase?"1":"0").append(termSeperator);
            }
            stringBuilder.append(currPath).append(fileDelimiter).append(term.getValue());
//            if (isUpperCase) {
//                if (tmpTermDic.containsKey(termKey)) {
//                    stringBuilder.append(tmpTermDic.get(termKey)).append(fileDelimiter);
//                    if (tmpTermDic.get(termKey).startsWith("0"))
//                        isUpperCase = false;
//                }
//            } else if (tmpTermDic.containsKey(upperCase(term.getKey()))) {
//                stringBuilder.append(tmpTermDic.remove(upperCase(term.getKey()))).append(fileDelimiter);
//                stringBuilder.trimToSize();
//                tmpTermDic.put(termKey, stringBuilder.toString());
//            } else if (tmpTermDic.containsKey(termKey)) {
//                stringBuilder.append(tmpTermDic.get(termKey)).append(fileDelimiter);
//                isUpperCase = false;
//            }
//            stringBuilder.append(currPath).append(fileDelimiter).append(term.getValue());
            tmpTermDic.put(termKey, stringBuilder.toString());
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currPath, "" + maxTf + "," + length);
        map.clear();
    }

    public static int getTermCount() {
        return termDictionary.size();
    }
}
