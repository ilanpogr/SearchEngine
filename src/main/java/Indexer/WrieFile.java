package Indexer;

import Controller.PropertiesFile;
import org.apache.commons.io.FileUtils;

import static org.apache.commons.io.FileUtils.*;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the class responsible for writing to the disk. addressed from Indexer
 */
public class WrieFile {

    private static String targetPath = PropertiesFile.getProperty("save.files.path");
    private static AtomicInteger fileNum = new AtomicInteger(0);
    private static LinkedHashMap<String, Integer> pointers = new LinkedHashMap<>();


    public static void setTargetPath(String targetPath) {
        WrieFile.targetPath = targetPath;
    }

    public static int getFileNum() {
        return fileNum.get();
    }

    /**
     * adds a single line to a file
     *
     * @param writersMap - a map of Buffered Writers
     * @param fileName   - the name of the file that will be written
     * @param content    - the text
     */
    public static void addLineToFile(LinkedHashMap<String, BufferedWriter> writersMap, String fileName, String content) {
        try {
            writersMap.get(fileName).append(content);
        } catch (Exception e) {
            System.out.println("Couldn't write the line: " + content + " to the file Named: " + fileName);
            //do nothing
        }
    }

    /**
     * writes a string to a new temporary file
     * @param poString - post-string. get it?
     */
    public static void createTempPostingFile(StringBuilder poString) {
        int fileName = fileNum.getAndIncrement() + 1;
        try {
            writeStringToFile(new File(targetPath + fileName + ".post"), poString.toString(), false);
        } catch (IOException e) {
            System.out.println("create temp posting file filed in: " + fileName);
            //do nothing
        }

    }

    /**
     * writes to a file in the current path
     * @param stringBuilder - the content that will be written to the file
     * @param fileName - file name
     * @return the number of bytes to skip to get to this content
     */
    public static int writeToDictionary(StringBuilder stringBuilder, String fileName) {
        targetPath = PropertiesFile.getProperty("save.files.path");
        int pointer=0;
        if (pointers.putIfAbsent(fileName,0)!=null)
            pointer = pointers.get(fileName);
        try {
            File file = new File(targetPath + fileName);
            String toPrint = stringBuilder.toString();
            writeStringToFile(file, toPrint, true);
            pointer += toPrint.getBytes().length;
        } catch (IOException e) {
            System.out.println("create Dictionary file filed in: " + fileName);
            //do nothing
        }
        return pointers.put(fileName, pointer);
    }

    /**
     * clears static fields from memory
     */
    public static void clear() {
        targetPath = PropertiesFile.getProperty("save.files.path");
        fileNum = new AtomicInteger(0);
    }

    public void writeQueryResults(String path) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path, true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
