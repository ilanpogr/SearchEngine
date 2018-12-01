package Indexer;

import Controller.PropertiesFile;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Indexer {

    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static int tmpFilesCounter = 0;
    private static int mergedFilesCounter = 0;
    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();
    private static String targetPath = null;


    public void indexTempFile(TreeMap<String, String> sortedTermsDic) {
        try {
            StringBuilder tmpPostFile = new StringBuilder();
            sortedTermsDic.forEach((k, v) ->
                    tmpPostFile.append(k).append(termSeperator).append(v).append("\n"));
            tmpPostFile.trimToSize();
            tmpDicQueue.addLast(tmpPostFile);
            WrieFile.createTempPostingFile(tmpPostFile);
            tmpFilesCounter++;
        } catch (OutOfMemoryError om) {     //if Map is too big
            try {
                indexTempFile(new TreeMap<>(sortedTermsDic.tailMap("M")));
                indexTempFile(new TreeMap<>(sortedTermsDic.headMap("M")));
            } catch (OutOfMemoryError om2) {    //if "M" wasn't good enough
                indexTempFile(new TreeMap<>(sortedTermsDic.tailMap("a")));
                indexTempFile(new TreeMap<>(sortedTermsDic.headMap("a")));
            }
        }

    }

    public void mergePostingTempFiles(String targetDirPath, LinkedHashMap<String, String> termDictionary, LinkedHashMap<String, String> cache) {
        targetPath = targetDirPath;
        int currFileNum = WrieFile.getFileNum();
        StringBuilder stringBuilder = new StringBuilder();
        LinkedHashMap<Integer, String> termKeys = new LinkedHashMap<>();
        LinkedHashMap<Integer, String> termValues = new LinkedHashMap<>();
        LinkedHashMap<Integer, BufferedReader> tmpFiles = new LinkedHashMap<>();
        for (int i = 1; i <= currFileNum; i++) {
            stringBuilder.append(targetDirPath).append((i + 1)).append(".post").trimToSize();
            checkOrMakeDir(targetDirPath);
            addFileToList(tmpFiles, stringBuilder, i);
            getFirstTerms(tmpFiles, termKeys, termValues, i);
        }
        LinkedHashMap<String, Integer> mergedFilesCounterDic = new LinkedHashMap<>();
        LinkedHashMap<String, BufferedReader> mergedFilesDic = new LinkedHashMap<>();
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic);
        TreeMap<String, ArrayList<Integer>> termsSorter = new TreeMap<>();
        while (mergedFilesCounter < tmpFilesCounter) {
            ArrayList <Integer> minTerms = new ArrayList<>();
            for (int i = 1; i <= tmpFiles.size(); i++) {
                if (!tmpFiles.containsKey(i)) continue;
                termsSorter.put(termKeys.get(i),minTerms);
            }
            String minTerm= termsSorter.firstKey();
            for (Map.Entry<Integer, String> term: termKeys.entrySet()
                 ) {
                if (equalsIgnoreCase(minTerm,term.getValue())){
                    if (!minTerm.equals(term.getValue())){

                    }

                }
            }
        }

    }

    private void initMergedDictionaries(LinkedHashMap<String, Integer> mergedFilesCounterDic, LinkedHashMap<String, BufferedReader> mergedFilesDic) {
        StringBuilder stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        String fileName = "others";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "abc";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "defgh";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "ijklm";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "mnop";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "qrst";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "uvwxyz";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
    }

    private void addFileToList(LinkedHashMap<Integer, BufferedReader> tmpFiles, StringBuilder stringBuilder, int i) {
        try {
            tmpFiles.put(i, new BufferedReader(new FileReader(new File(stringBuilder.toString()))));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            e.printStackTrace();
        }
    }

    private void addFileToList(LinkedHashMap<String, BufferedReader> tmpFiles, StringBuilder stringBuilder, String name) {
        try {
            tmpFiles.put(name, new BufferedReader(new FileReader(new File(stringBuilder.toString()))));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            e.printStackTrace();
        }
    }

    private void getFirstTerms(LinkedHashMap<Integer, BufferedReader> tmpFiles, LinkedHashMap<Integer, String> termKeys, LinkedHashMap<Integer, String> termValues, int i) {
        try {
            BufferedReader file = tmpFiles.get(i);
            String line = tmpFiles.get(i).readLine();
            if (!isEmpty(line)) {
                String[] term = split(line, termSeperator, 2);
                termKeys.put(i, term[0]);
                termValues.put(i, term[1]);
            } else {
                termKeys.remove(i);
                termValues.remove(i);
                tmpFiles.remove(i).close();
                mergedFilesCounter++;
            }
        } catch (IOException e) {
            System.out.println("couldn't find or open: ");
            e.printStackTrace();
        }

    }

    private void checkOrMakeDir(String targetDirPath) {
        try {
            Path path = Paths.get(targetDirPath);
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        } catch (Exception e) {
            System.out.println("couldn't find or open: " + targetDirPath);
            e.printStackTrace();
        }
    }
}