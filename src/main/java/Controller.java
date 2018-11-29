import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import Stemmer.Stemmer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class Controller {

    private static boolean isStemMode=true;
    private static ArrayList<Doc> filesList;
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> fileDic = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> docDic = new LinkedHashMap<>();
    private static int maxTTF = -1;
    private static String maxTTFS = "";
    private static String currPath;

    public static void main(String[] args) {
        double mainStartTime = System.currentTimeMillis();


        int j = 0, f = 0, ii = 0;
        try {
            String path = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
            filesList = new ArrayList<>();
            ReadFile readFile = new ReadFile(path);
            Parse p = new Parse();
            double fileparse = 0;
            double singleparse = 0;
            while (readFile.hasNextFile()) {
                f++;
                double read = System.currentTimeMillis();
                filesList = readFile.getFileList();
                for (int i = 0; i < filesList.size(); i++) {
                    double parsestart = System.currentTimeMillis();
                    currPath = filesList.get(i).docNum();
//                    if (String.valueOf(filesList.get(i).text).isEmpty())
//                        System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ii);
//                    handleFile(filesList.get(i).text())
                    HashMap<String, String> map = p.parse(filesList.get(i).text());
                    Stemmer stemmer = new Stemmer();
                    HashMap<String, Pair<Integer, String>> stemmed = stemmer.stem(map);
//                    ArrayList<String> sm = new ArrayList<>(map.keySet());
//                    Collections.sort(sm);
//                    System.out.println(sm.toString());
//                    ArrayList<String> sst = new ArrayList<>(stemmed.keySet());
//                    Collections.sort(sst);
//                    System.out.println(sst.toString());
//
                    updateDocsMaxTf(filesList.get(i), map);
                    double parseend = System.currentTimeMillis();
                    singleparse = (parseend - read) / 1000;
                    fileparse += (parseend - parsestart) / 1000;
                    j++;
                    ii = i;
                }
                if (f % 18 == 0)
                    System.out.println("Time took to read and parse file: " + currPath + ": " + singleparse + " seconds. \t Total read and parse time: " + (int) fileparse + " seconds. \t (number of documents: " + (j) + ",\t number of files: " + f + ")");
                filesList.clear();
            }
            System.out.println("\nTime took to run main: " + (System.currentTimeMillis() - mainStartTime) / 1000 + " seconds");
        } catch (Exception e) {
            System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ii);
            e.printStackTrace();
        }
    }

    private static void updateDocsMaxTf(Doc doc, HashMap<String, String> map) {
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
        doc.addAttributes(new String[]{"MAX-TF", maxKey + ":" + max + ""});
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
