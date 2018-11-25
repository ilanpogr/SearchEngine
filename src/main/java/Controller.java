import ReadFile.ReadFile;
import TextContainers.Doc;


import java.util.*;

public class Controller {

    private static boolean isStemMode;
    private static ArrayList<Doc> filesList;

    private static int maxTTF=-1;
    private static String maxTTFS="";

    public static void main(String[] args) {
        double mainStartTime = System.currentTimeMillis();

        String path = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
        filesList = new ArrayList<>();
        ReadFile readFile = new ReadFile(path);
        filesList = readFile.getFileList();
        for (int i = 0; i < filesList.size(); i++) {
            HashMap<String, String> map = new Parse().parse(new String[]{filesList.get(i).text});
            updateDocsMaxTf(filesList.get(i), map);
            System.out.println(map.toString());

        }

        System.out.println("\nTime took to run main: " + (System.currentTimeMillis() - mainStartTime) / 1000 + " seconds");
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
        if (maxTTF<max){
            maxTTF=max;
            maxTTFS = maxKey + " -> " + doc.docNum;
        }
        doc.setMax_tf(max);
        doc.addAttributes(new String[]{"MAX-TF" ,maxKey + ":" + max + ""});
    }

}
