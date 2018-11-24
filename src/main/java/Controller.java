import ReadFile.ReadFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {

    private static boolean isStemMode;
    private static Map<String,String> filesMap;

    public static void main(String[] args) {
        List<String> paths = createPathsList(Paths.get("C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש"));
        filesMap=new HashMap<>();

        ReadFile readFile = new ReadFile();

    }

    private static List<String> createPathsList(Path path) {
        List<String> fileList = null;
        try {
            Stream<Path> subPaths = Files.walk(path);
            fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());

        } catch (Exception e){
            e.printStackTrace();
        }
        return fileList;
    }

}
