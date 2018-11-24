import ReadFile.ReadFile;
import TextContainers.Doc;


import java.util.*;

public class Controller {

    private static boolean isStemMode;
    private static ArrayList<Doc> filesList;

    public static void main(String[] args) {
        String path = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
        filesList = new ArrayList<>();
        ReadFile readFile = new ReadFile(path);
        filesList = readFile.getFileList();
        HashMap<String,String> map =new Parse().parse(new String []{filesList.get(0).text});
        System.out.println(map.toString());

    }

}
