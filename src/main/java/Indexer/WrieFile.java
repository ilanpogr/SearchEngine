package Indexer;

import Controller.Controller;
import static org.apache.commons.io.FileUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WrieFile {

    private static String targetPath = Controller.getTargetDirPath();
    private static AtomicInteger fileNum=new AtomicInteger(0);

    public static void createTempPostingFile(StringBuilder poString) {
        int fileName = fileNum.getAndIncrement()+1;
        try {
            writeStringToFile(new File(targetPath+fileName+".post"),poString.toString(),false);
        } catch (IOException e) {
            System.out.println("create temp posting file filed in: " +fileName);
            e.printStackTrace();
        }

    }

    public static int getFileNum() {
        return fileNum.get();
    }

    public static void addPostLine(LinkedHashMap<String, BufferedWriter> mergedFilesDic, String mergedFileName, String stringBuilder) {
        try{
            mergedFilesDic.get(mergedFileName).append(stringBuilder);
        } catch (Exception e){
            System.out.println("Couldn't write the line: " + stringBuilder + " to the file Named: " +mergedFileName);
            e.printStackTrace();
        }
    }

    public static void writeFinalDictionary(StringBuilder termDictionary) {
        int fileName = fileNum.getAndIncrement()+1;
        try {
            writeStringToFile(new File(targetPath+"Final Terms Dictionary"),termDictionary.toString(),true);
        } catch (IOException e) {
            System.out.println("create temp posting file filed in: " +fileName);
            e.printStackTrace();
        }

    }

    public static void writeCacheDictionary(StringBuilder cahce) {
        int fileName = fileNum.getAndIncrement()+1;
        try {
            writeStringToFile(new File(targetPath+"Cache Dictionary"),cahce.toString(),true);
        } catch (IOException e) {
            System.out.println("create temp posting file filed in: " +fileName);
            e.printStackTrace();
        }

    }
}
