package Controller;

import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils.*;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Controller {

    private static boolean isStemMode = true;
    private static ArrayList<Doc> filesList;
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static TreeMap<String, String> sortedTermsDic = new TreeMap<>();
    private static String tmpFilesPath;
    private static int maxTTF = -1;
    private static String maxTTFS = "";
    private static String currPath;
    private static StringBuilder stringBuilder = new StringBuilder();

    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> termDic = new LinkedHashMap<>();
    private static String corpusPath;
    private static String targetDirPath;
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");

    public static String getTargetDirPath() {
        return targetDirPath;
    }

    public static void main(String[] args) {
        double mainStartTime = System.currentTimeMillis();
        int j = 0, f = 1495, ii = 0;
        try {
            targetDirPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\tmp-run\\writerDir";
            corpusPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
            filesList = new ArrayList<>();
            ReadFile readFile = new ReadFile(corpusPath);
            Parse p = new Parse();
            double fileparse = 0;
            double singleparse = 0;
            while (readFile.hasNextFile()) {
                f++;
                double read = System.currentTimeMillis();
                filesList = readFile.getFileList();
                for (int i = 12; i < filesList.size(); i++) {
                    double parsestart = System.currentTimeMillis();
                    currPath = filesList.get(i).docNum();
//                    if (String.valueOf(filesList.get(i).text).isEmpty())
//                        System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ii);
//                    handleFile(filesList.get(i).text())
                    HashMap<String, String> map = p.parse(filesList.get(i).text());
                    handleFile(map);
//                    Stemmer stemmer = new Stemmer();
//                    termDic.putAll(map);
//                    HashMap<String, String> stemmed = stemmer.stem(map);

//                    termDic.forEach((s, s2) -> sortedTermsDic.putIfAbsent(s,currPath));
//                    ArrayList<String> sm = new ArrayList<>(map.keySet());
//                    Collections.sort(sm);
//                    System.out.println(sm.toString());
//                    ArrayList<String> sst = new ArrayList<>(stemmed.keySet());
//                    Collections.sort(sst);
//                    System.out.println(sst.toString());
//                    updateDocsMaxTf(filesList.get(i), map);
                    double parseend = System.currentTimeMillis();
                    singleparse = (parseend - read) / 1000;
                    fileparse += (parseend - parsestart) / 1000;
                    ii = i;
                    j++;

                }
                if (f % 50 == 0)
                    termDic.forEach((k, v) -> termDic.replace(k,v,""));
                System.out.println("Time took to read and parse file: " + currPath + ": " + singleparse + " seconds. \t Total read and parse time: " + (int) fileparse / 60 + ":" + ((fileparse % 60 < 10) ? "0" : "") + (int) fileparse % 60 + " seconds. \t (number of documents: " + (j) + ",\t number of files: " + f + ")\t\t\tSize of Dictionary: "+termDic.size() );
                filesList.clear();
            }
            int total = (int) ((System.currentTimeMillis() - mainStartTime) / 1000);
            System.out.println("\nTime took to run main: " + total / 60 + ":" + (total % 60 < 10 ? "0" : "") + total % 60 + " seconds");
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ii);
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
            int termFrequency = countMatches(term.getValue(), Stemmer.getStemDelimiter().charAt(0));
            String termKey = lowerCase(term.getKey());
            boolean isUpperCase = false;
            if (termFrequency == 0)
                termFrequency++;
            termFrequency += countMatches(term.getValue(), Parse.getGapDelimiter().charAt(0));
            if (Character.isUpperCase(term.getKey().charAt(0))) {
                isUpperCase = true;
            }
            if (isUpperCase) {
                if (termDic.containsKey(termKey)) {
                    stringBuilder.append(termDic.get(termKey)).append(fileDelimiter);
                    isUpperCase = false;
                }
            }
            else if (!isUpperCase && termDic.containsKey(upperCase(term.getKey()))) {
                stringBuilder.append(termDic.remove(upperCase(term.getKey()))).append(fileDelimiter);
                stringBuilder.trimToSize();
                termDic.put(termKey, stringBuilder.toString());
            }
            else if (termDic.containsKey(termKey)) {
                stringBuilder.append(termDic.get(termKey)).append(fileDelimiter);
                isUpperCase = false;
            }
            stringBuilder.append(currPath).append(fileDelimiter).append(term.getValue());
            termDic.put(term.getKey(), stringBuilder.toString()); //TODO- put matching case
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }
        DocDic.put(currPath, "" + maxTf + "," + length);
        map.clear();
    }

   /* private static void updateDocsMaxTf(Doc doc, HashMap<String, String> map) {
        int max = -1;
        String maxKey = "";
        Set set = map.entrySet();
        for (Object term : set
        ) {
            int curr = Integer.parseInt(((Map.Entry<String, String>) term).getValue().split(",")[0]);
            if (max != Integer.max(max, curr)) {
                max = curr;
                maxKey = ((Map.Entry<String, String>) term).getKey();
            }
        }
        if (maxTTF < max) {
            maxTTF = max;
            maxTTFS = maxKey + " -> " + doc.docNum();
        }
        doc.setMax_tf(max);
        doc.addAttributes("MAX-TF", maxKey + ":" + max + "");
    }*/
}
