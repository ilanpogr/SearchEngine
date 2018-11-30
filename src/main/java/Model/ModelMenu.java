package Model;

import Controller.PropertiesFile;
import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModelMenu {

    private static boolean isStemMode = true;
    private static ArrayList<Doc> filesList = new ArrayList<>();
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> termDic = new LinkedHashMap<>();
    private static String targetDirPath;
    private static String currPath;
    private String corpusPath;
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");


    private StringBuilder stringBuilder = new StringBuilder();


    public ModelMenu(String corpusPath, String targetDirPath) {
        this.corpusPath = corpusPath;
        this.targetDirPath = targetDirPath;
    }

    public ModelMenu(String corpusPath, String targetDirPath, boolean stemMode) {
        this.corpusPath = corpusPath;
        this.targetDirPath = targetDirPath;
        isStemMode = stemMode;
    }

    /**
     *
     */
    public void start() {
        double startTime = System.currentTimeMillis();
        int docCounter = 0, fileCounter = 0;
        try {
            ReadFile readFile = new ReadFile(corpusPath);
            Parse parser = new Parse();
            while (readFile.hasNextFile()) {
                fileCounter++;
                filesList = readFile.getFileList();
                for (int i = 0; i < filesList.size(); i++) {
                    docCounter++;
                    currPath = filesList.get(i).docNum();
                    HashMap<String, String> parsedDic = parser.parse(filesList.get(i).text());
                    handleFile(parsedDic);
                }
            }
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + fileCounter + ") in Doc number: " + docCounter);
            e.printStackTrace();
        }
        System.out.println("\nTime took to run main: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    private void handleFile(HashMap<String, String> parsedDic) {
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            HashMap<String, String> stemmed = stemmer.stem(parsedDic);
            mergeDicts(stemmed);
        } else {
            parsedDic.forEach((key, value) -> value = StringUtils.substring(value, 2));
            mergeDicts(parsedDic);
        }
    }

    private void mergeDicts(HashMap<String, String> map) {
        int maxTf = 0, length = 0;
        for (Map.Entry<String, String> term : map.entrySet()
        ) {
            stringBuilder.setLength(0);
            int termFrequency = StringUtils.countMatches(term.getValue(), Stemmer.getStemDelimiter().charAt(0));
            if (termFrequency == 0)
                termFrequency++;
            termFrequency += StringUtils.countMatches(term.getValue(), Parse.getGapDelimiter().charAt(0));
            if (termDic.containsKey(term.getKey()))
                stringBuilder.append(termDic.get(term.getKey())).append(fileDelimiter);
            stringBuilder.append(currPath).append(fileDelimiter).append(term.getValue());
            termDic.put(term.getKey(), stringBuilder.toString());
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currPath, "" + maxTf + "," + length);
    }
}
