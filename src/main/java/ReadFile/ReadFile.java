package ReadFile;

import Controller.PropertiesFile;
import TextContainers.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.jsoup.Jsoup;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 *
 */
public class ReadFile {

    //TODO- return all Tags as Atributes from a Doc

    private ArrayList<String> unInstancedDocList;   //a list of documents which haven't been instanced yet
    private ArrayList<Doc> docList; //a list of Docs, from a single File
    private static ArrayList<String> rootPath; //paths list of all files in the corpus
    private static AtomicInteger fileCounter = new AtomicInteger(0);

    //    private ArrayList<String> textList;
//    private ArrayList<String> docNumList;
//    private Map<String,String> docMap = new M
//    private static int docCounter = 0;


    /**
     * dCtor
     */
    public ReadFile() {
        this.unInstancedDocList = new ArrayList<>();
        this.docList = new ArrayList<>();
//        this.textList = new ArrayList<>();
//        this.docNumList = new ArrayList<>();
    }

    /**
     * Ctor
     * @param path - to corpus dir
     */
    public ReadFile(String path) {
        this();
        if (rootPath == null)
            rootPath = new ArrayList<>(createPathsList(/*Paths.get(*/path));
    }

    /**
     * create a list with all paths within corpus dir
     * @param path - to corpus dir
     * @return List of paths
     */
    private static List<String> createPathsList(String path) {
        List<String> fileList = new ArrayList<>();
        try {
//            Stream<Path> subPaths = Files.walk(Paths.get(path));
//            fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());
            File root = new File(path);
            File[] dirs = root.listFiles();
            StringBuilder filePath = new StringBuilder();
            for (int i = 0; i < dirs.length; i++) {
                filePath.append(dirs[i]);
                filePath.append(substringAfter(filePath.toString(), "corpus"));
                fileList.add(filePath.toString());
                filePath.setLength(0);
            }
//            System.out.println(fileCounter);
            PropertiesFile.putProperty("number.of.files",""+fileList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public static void clear() {
        rootPath=null;
        fileCounter = new AtomicInteger(0);
    }

    /**
     * clears the documents lists
     */
    private void clearLists() {
        unInstancedDocList.clear();
        docList.clear();
//        textList = new ArrayList<>();
//        docNumList = new ArrayList<>();
    }

    /**
     * reads a File and splits it into a document list
     * @param path - to the file
     * @return a list of Doc (documents)
     */
    private ArrayList<Doc> readFromFile(String path) {
        try {
//            double start = System.currentTimeMillis();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1 + " "));
//            System.out.println(stringBuilder);
            String string = stringBuilder.toString();
//            unInstancedDocList.addAll(Arrays.asList(string.split("</DOC>")));
            unInstancedDocList.addAll(Arrays.asList(splitByWholeSeparator(string, "</DOC>")));
            unInstancedDocList.remove(unInstancedDocList.size() - 1);
            createDocList();
            stringBuilder.setLength(0);
            unInstancedDocList.clear();
            return docList;
//            extractDocNums();
//            extractText();
//            double end = System.currentTimeMillis();
//            System.out.println("Time took to read files: " + (end - start) / 1000 + " sec.");
//            System.out.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * creates a list of Doc from the read File
     */
    private void createDocList() {
        for (String doc : unInstancedDocList) {
            StringBuilder document = new StringBuilder(Jsoup.parse(doc).toString());
            Doc curr = new Doc();
            curr.setFileName(substringAfterLast(rootPath.get(fileCounter.get()-1),"\\"));
            StringBuilder docCity = new StringBuilder();
            String docLang = "";
            boolean isLanguage = false;
            String[] docArr = split(document.toString(), '\n');
            StringBuilder line = new StringBuilder(), tag = new StringBuilder();
            for (int i = 0; i < docArr.length; i++) {
                docArr[i] = trim(docArr[i]);
                if (startsWith(docArr[i],"<")) {
                    if (containsIgnoreCase(docArr[i],"f p=\"104\"")) {
                        docCity = new StringBuilder(docArr[i] + docArr[i+1] + docArr[i+2]);
                    }
                    if (containsIgnoreCase(docArr[i],"f p=\"105\"")){
                        docLang = trim(docArr[i+1]);
                        isLanguage = true;
                    }
                        String[] lineArr = splitPreserveAllTokens(docArr[i], ">", 2);
                        docArr[i] = trim(lineArr[1]);
                    if (line.length() > 0 && docArr[i].isEmpty()) {
                        if (!lineArr[0].contains("/")) continue;
                        tag.append(upperCase(splitPreserveAllTokens(lineArr[0],"/")[1]));
                        curr.addAttributes(trim(tag.toString()), trim(line.toString()));
                        tag.setLength(0);
                        line.setLength(0);
                    }
                    if (docArr[i].endsWith(">")) {
                        docArr[i] = trim(splitPreserveAllTokens(docArr[i],"<", 2)[0]);
                    }
                }
                if (docArr[i].isEmpty()) continue;
                line.append(" ").append(docArr[i]);
            }
            if (curr.hasCity()){
                createAndUpdateCity(curr,docCity);
            }
            if (isLanguage){
                createAndUpdateLanguage(curr,docLang);
            }
            docList.add(curr);

        }
    }

    private void createAndUpdateLanguage(Doc curr, String docLang) {
        if (!docLang.equals("")){
            LanguagesInfo languagesInfo = LanguagesInfo.getInstance();
            languagesInfo.addLanguageToList(docLang);
            curr.setLanguage(docLang);
        }
    }

    private void createAndUpdateCity(Doc doc, StringBuilder document) {
//        stringBuilder.setLength(0);
//        extractTag(stringBuilder,document,"<F P=104>");
//        String tag = stringBuilder.toString();
        String tag = trim(substringBetween(document.toString(),">","<"));
        if (tag != null && !tag.equals("")) {
            CityInfo cityInfo = CityInfo.getInstance();
            cityInfo.setInfo(tag, doc);
//            doc.addAttributes("City", stringBuilder.toString());
        }
    }


    /**
     * get a list of 'Doc' from a single File.
     * the function works as a queue, each time it's called it will return the next file
     * in a form of Doc's list.
     * @return ArrayList of Doc
     */
    public ArrayList<Doc> getFileList() {
        if (hasNextFile()) {
            clearLists();
            return readFromFile(rootPath.get(fileCounter.getAndIncrement()));
        }
        return null;
    }

    /**
     * check if there are more files in the corpus
     * @return true if there are unread files, else false
     */
    public static boolean hasNextFile() {
//        return fileCounter < 1000;
        return fileCounter.get() < rootPath.size();
    }

}
