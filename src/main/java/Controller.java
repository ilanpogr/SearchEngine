import ReadFile.ReadFile;
import TextContainers.Doc;


import java.util.*;

public class Controller {

    private static boolean isStemMode;
    private static ArrayList<Doc> filesList;

    public static void main(String[] args) {
        double mainStartTime=System.currentTimeMillis();

        String path = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
        filesList = new ArrayList<>();
        ReadFile readFile = new ReadFile(path);
        filesList = readFile.getFileList();
        for (int i = 0; i < filesList.size(); i++) {
            HashMap<String,String> map =new Parse().parse(new String []{filesList.get(i).text});
            System.out.println(map.toString());

        }

        System.out.println("\nTime took to run main: " + (System.currentTimeMillis()-mainStartTime)/1000+ " seconds");
    }

}
