package Master;


import Controller.PropertiesFile;
import TextContainers.Doc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Master {

    private static int fileNum = getPropertyAsInt("number.of.files");
    private static int tmpFileNum = getPropertyAsInt("number.of.temp.files");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static StringBuilder stringBuilder = new StringBuilder();
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> DocDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> termDictionary = new LinkedHashMap<>();
    private static boolean isStemMode = true;
    private static ArrayList<Doc> filesList;
    private static String targetDirPath;
    private static String corpusPath;
    private static String currPath;
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public Master(){

    }

    /**
     * get a Property from properties file and convert it to int.
     * if it can't convert to Integer, it will return 5.
     * @param s - the value of the property
     * @return the value of the property
     */
    private static int getPropertyAsInt(String s) {
        try {
            return Integer.parseInt(PropertiesFile.getProperty(s));
        } catch (Exception e){
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 5;
        }
    }

    /**
     * get the Path to the Directory that file will be written to
     * @return String path
     */
    public static String getTargetDirPath() {
        return targetDirPath;
    }

    public int getNumOfTerms() {
        return termDictionary.size();
    }

    public int getNumOfDocs() {
        return DocDic.size();
    }


}
