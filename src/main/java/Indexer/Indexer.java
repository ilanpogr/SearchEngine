package Indexer;

import Controller.PropertiesFile;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Indexer {

    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static int tmpFilesCounter = 0;
    private static int mergedFilesCounter = 0;
    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();


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
        int currFileNum = WrieFile.getFileNum();
        StringBuilder stringBuilder = new StringBuilder();
        LinkedHashMap<Integer,String> termKeys = new LinkedHashMap<>();
        LinkedHashMap<Integer,String> termValues = new LinkedHashMap<>();
        LinkedHashMap<Integer,BufferedReader> tmpFiles = new LinkedHashMap<>();
        for (int i = 0; i < currFileNum; i++) {
            stringBuilder.append(targetDirPath).append((i + 1)).append(".post").trimToSize();
            checkOrMakeDir(targetDirPath);
            addFileToList(tmpFiles, stringBuilder,i);
            getFirstTerms(tmpFiles, termKeys, termValues, i);
        }


    }

    private void addFileToList(LinkedHashMap<Integer, BufferedReader> tmpFiles, StringBuilder stringBuilder, int i) {
        try {
            tmpFiles.put(i,new BufferedReader(new FileReader(new File(stringBuilder.toString()))));
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
                termKeys.put(i,term[0]);
                termValues.put(i,term[1]);
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