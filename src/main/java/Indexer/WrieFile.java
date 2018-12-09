package Indexer;

import Controller.PropertiesFile;
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


    public static void setTargetPath(String targetPath) {
        WrieFile.targetPath = targetPath;
    }

    public static int getFileNum() {
        return fileNum.get();
    }

    /**
     * adds a single line to a file
     * @param writersMap - a map of Buffered Writers
     * @param fileName - the name of the file that will be written
     * @param content - the text
     */
    public static void addLineToFile(LinkedHashMap<String, BufferedWriter> writersMap, String fileName, String content) {
        try {
            writersMap.get(fileName).append(content);
        } catch (Exception e) {
            System.out.println("Couldn't write the line: " + content + " to the file Named: " + fileName);
            e.printStackTrace();
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
            e.printStackTrace();
        }

    }

    /**
     * writes a string to a specified file
     * @param stringBuilder - the content
     * @param fileName - the name of the file
     */
    public static void writeToDictionary(StringBuilder stringBuilder, String fileName) {
        try {
            writeStringToFile(new File(targetPath+fileName),stringBuilder.toString(),true);
        } catch (IOException e) {
            System.out.println("create Dictionary file filed in: " +fileName);
            e.printStackTrace();
        }
    }

    /**
     * clears static fields from memory
     */
    public static void clear() {
        targetPath = PropertiesFile.getProperty("save.files.path");
        fileNum = new AtomicInteger(0);
    }
}
