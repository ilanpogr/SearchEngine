package ReadFile;

import TextContainers.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 */
public class ReadFile {

    //TODO- return all Tags as Atributes from a Doc

    private ArrayList<String> unInstancedDocList;   //a list of documents which haven't been instanced yet
    private ArrayList<Doc> docList; //a list of Docs, from a single File
    private static ArrayList<String> rootPath; //paths list of all files in the corpus
    private static int fileCounter = 0;
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
            System.out.println(fileCounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
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
            String[] docArr = split(document.toString(), '\n');
            StringBuilder line = new StringBuilder(), tag = new StringBuilder();
            for (int i = 0; i < docArr.length; i++) {
                docArr[i] = trim(docArr[i]);
                if (startsWith(docArr[i],"<")) {
                    String[] lineArr = splitPreserveAllTokens(docArr[i],">", 2);
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
                createAndUpdateCity(curr,line,tag.append(document));
            }
            docList.add(curr);

        }
    }

    private void createAndUpdateCity(Doc doc, StringBuilder document, StringBuilder stringBuilder) {
        stringBuilder.setLength(0);
        extractTag(stringBuilder,document,"<F P=104>");
        doc.addAttributes("City",stringBuilder.toString());
    }


    /**
     * extracts a given tag from a document string.
     * the element cut from the document will be kept in 'element' for further use
     * @param element - String holder (mutable)
     * @param document - holding the document String (also mutable) and cuts the given key (optional)
     * @param delimiter - tag's name to extract
     */
    private void extractTag(StringBuilder element, StringBuilder document, String delimiter) {
//        String[] tmp = document[0].split(delimiter + ">", 3);
        String[] tmp = splitByWholeSeparator(document.toString(), appendIfMissing(delimiter, ">"));
        if (tmp.length < 3) {
            element.delete(0, element.length());
            return;
        }
//        if (!delimiter.equalsIgnoreCase("text")) {
//            document[0] = trim(substring(tmp[0],0, tmp[0].length() - 1))+ "\n " + trim(tmp[2]);
//        }
        element.append(trim(substring(tmp[1], 0, tmp[1].length() - 2)));
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
            return readFromFile(rootPath.get(fileCounter++));
        }
        return null;
    }

    /**
     * check if there are more files in the corpus
     * @return true if there are unread files, else false
     */
    public boolean hasNextFile() {
//        return fileCounter < 1000;
        return fileCounter < rootPath.size();
    }

}
