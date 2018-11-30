package Model;

import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ModelMenu {

    private static boolean isStemMode = true;
    private static ArrayList<Doc> filesList = new ArrayList<>();
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> fileDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> docDic = new LinkedHashMap<>();
    private static String currPath;
    private String corpusPath;
    private String targetDirPath;
//    private static int maxTTF = -1;
//    private static String maxTTFS = "";


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
                    Stemmer stemmer = new Stemmer();
                    HashMap<String, Pair<Integer,String>> stemmed = stemmer.stem(parsedDic);
                }
            }
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + fileCounter + ") in Doc number: " + docCounter);
            e.printStackTrace();
        }
        System.out.println("\nTime took to run main: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    private void handleFile(HashMap<String, String> parsedDic) {
        int maxTf = 0, length = 0;
        if (isStemMode){
            Stemmer stemmer = new Stemmer();
            HashMap<String, Pair<Integer, String>> stemmed = stemmer.stem(parsedDic);

        } else {

        }
    }


//# The function that merges the final dictionaries. Called from handle files.  It iterates through the dictionary and
//# # adds new terms or updates new ones that weren't seen before.
//# def __update_and_merge_dictionaries(doc_id, term_dictionary_ref, documents_dictionary_ref_ref, check_this_dictionary):
//#     max_tf = 0
//#     length = 0
//#     for key, value in check_this_dictionary.items():
//#         if key in term_dictionary_ref:
//#             term_dictionary_ref[key] += __docs_delimiter + doc_id + __docs_delimiter + str(value)
//#         else:
//#             term_dictionary_ref[key] = doc_id + __docs_delimiter + str(value)
//#
//#         if value > max_tf:
//#             max_tf = value
//#         length += value
//#     documents_dictionary_ref_ref[doc_id] = (max_tf, length)
}

