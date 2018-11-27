import Parser.Parse;
import ReadFile.ReadFile;
import Stemmer.Stemmer;
import TextContainers.Doc;
import Stemmer.Stemmer;
import java.util.*;

public class Controller {

    private static boolean isStemMode;
    private static ArrayList<Doc> filesList;

    private static int maxTTF = -1;
    private static String maxTTFS = "";
    private static String currPath;

    public static void main(String[] args) {
        double mainStartTime = System.currentTimeMillis();





        int j = 0, f = 0, ii=0;
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
                    currPath = filesList.get(i).docNum;
//                    if (String.valueOf(filesList.get(i).text).isEmpty())
//                        System.out.println("Current File: " + currPath + " (number " + f + ") in Doc number: " + ii);
                    HashMap<String, String> map = p.parse(new String[]{String.valueOf(filesList.get(i).text)});
                    Stemmer stemmer =new Stemmer();
                    HashMap<String, String> stemmed = stemmer.stem(map);
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
                    ii=i;
                }
            if (f % 15 == 0)
                System.out.println("Time took to read and parse file: " + currPath + ": " + singleparse + " seconds. \t Total read and parse time: " + (int)fileparse + " seconds. \t (number of documents: " + (j) + ",\t number of files: " + f + ")");
                filesList.clear();
            }
            System.out.println("\nTime took to run main: " + (System.currentTimeMillis() - mainStartTime) / 1000 + " seconds");
        }catch (Exception e){
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
            maxTTFS = maxKey + " -> " + doc.docNum;
        }
        doc.setMax_tf(max);
        doc.addAttributes(new String[]{"MAX-TF", maxKey + ":" + max + ""});
    }

}
