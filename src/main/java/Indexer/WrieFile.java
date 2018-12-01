package Indexer;

import Controller.Controller;
import static org.apache.commons.io.FileUtils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class WrieFile {

    private static String targetPath = Controller.getTargetDirPath();
    private static AtomicInteger fileNum=new AtomicInteger(1);

    public static void createTempPostingFile(StringBuilder poString) {
        int fileName = fileNum.getAndIncrement();
        try {

            writeStringToFile(new File(targetPath+fileName+".post"),poString.toString());
        } catch (IOException e) {
            System.out.println("create temp posting file filed in: " +fileName);
            e.printStackTrace();
        }

    }

    public static int getFileNum() {
        return fileNum.get();
    }
}
