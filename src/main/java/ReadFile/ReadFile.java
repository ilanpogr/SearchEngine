package ReadFile;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Master.Master;
import TextContainers.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * the class that reads the corpus
 */
public class ReadFile {

    //TODO- return all Tags as Atributes from a Doc

    private ArrayList<String> unInstancedDocList;   //a list of documents which haven't been instanced yet
    private ArrayList<Doc> docList; //a list of Docs, from a single File
    private static ArrayList<String> rootPath; //paths list of all files in the corpus
    private static AtomicInteger fileCounter = new AtomicInteger(0);

    /**
     * dCtor
     */
    public ReadFile() {
        this.unInstancedDocList = new ArrayList<>();
        this.docList = new ArrayList<>();
    }


    /**
     * Ctor
     *
     * @param path - to corpus dir
     */
    public ReadFile(String path) {
        this();
        if (rootPath == null)
            rootPath = new ArrayList<>(createPathsList(/*Paths.get(*/path));
    }

    /**
     * create a list with all paths within corpus dir
     *
     * @param path - to corpus dir
     * @return List of paths
     */
    private static List<String> createPathsList(String path) {
        List<String> fileList = new ArrayList<>();
        try {
            File root = new File(path);
            File[] dirs = root.listFiles();
            StringBuilder filePath = new StringBuilder();
            for (int i = 0; i < dirs.length; i++) {
                filePath.append(dirs[i]);
                filePath.append(substringAfter(filePath.toString(), "corpus"));
                fileList.add(filePath.toString());
                filePath.setLength(0);
            }
            PropertiesFile.putProperty("number.of.files", "" + fileList.size());
        } catch (Exception e) {
            System.out.println("wrong path, look at the instructions!");
        }
        return fileList;
    }

    /**
     * clears static values
     */
    public static void clear() {
        rootPath = null;
        fileCounter = new AtomicInteger(0);
    }

    /**
     * reads dictionaries and makes a Map of Maps out of it to keep in the memory
     *
     * @param dicPath   - the path to the dictionary
     * @param delimiter - the delimiter of whats a key and whats a value
     * @return a Map or null
     */
    public static TreeMap<Character, TreeMap<String, String>> readDictionaries(String dicPath, String delimiter) {
        TreeMap<Character, TreeMap<String, String>> dicSet = new TreeMap<>();
        TreeMap<String, String> dic = null;
        BufferedReader bufferedReader = null;
        try {
            File dicDir = new File(dicPath);
            if (dicDir.isDirectory()) {
                File[] dics = dicDir.listFiles();
                for (int i = 0; i < dics.length; i++) {
                    if (!isNumeric(dics[i].getName().substring(0, 1))) continue;
                    //todo - implement
                    bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dics[i].getPath()), StandardCharsets.UTF_8));
                    String s = bufferedReader.readLine();
                    dic = new TreeMap<>(String::compareToIgnoreCase);
                    while (s != null) {
                        String[] term = split(s, delimiter, 2);
                        dic.put(term[0], term[1]);
                        s = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    dicSet.put(dics[i].getName().charAt(0), dic);
                }
            }
        } catch (Exception e) {
            System.out.println("wrong path, look at the instructions!");
        }

        return dicSet;
    }

    /**
     * Getter for the posting of a given term and path to posting file
     *
     * @param postingPath - the path to the posting directory
     * @param term        - the term we seek
     * @param skip        - the number of bytes before this line
     * @return posting line (String)
     */
    public static String getTermLine(StringBuilder postingPath, String term, String skip) {
        if (isEmpty(skip)) return "";
        postingPath.append("\\Dictionaries without stemming\\").append(Indexer.getFileName(lowerCase(term).charAt(0))).append(".post");
        try {
            RandomAccessFile file = new RandomAccessFile(postingPath.toString(), "r");
            file.skipBytes(Integer.parseInt(skip, 36));
            return file.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String saveSolution(String path) {
        File file = new File(path);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(substringBeforeLast(path, "\\") + "\\tmpsols.txt", true));
            String line = bufferedReader.readLine().trim();
            while (line != null) {
                if (!endsWith(line, "0")) {
                    bufferedWriter.write(line + "\n");
                    stringBuilder.append(line).append("\n");
                }
                line = bufferedReader.readLine();
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {
            return "";
        }
        return stringBuilder.toString();
    }


    /**
     * clears the documents lists
     */
    private void clearLists() {
        unInstancedDocList.clear();
        docList.clear();
    }

    /**
     * reads a File and splits it into a document list
     *
     * @param path - to the file
     * @return a list of Doc (documents)
     */
    private ArrayList<Doc> readFromFile(String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1 + " "));
            String string = stringBuilder.toString();
            unInstancedDocList.addAll(Arrays.asList(splitByWholeSeparator(string, "</DOC>")));
            unInstancedDocList.remove(unInstancedDocList.size() - 1);
            createDocList();
            stringBuilder.setLength(0);
            unInstancedDocList.clear();
            return docList;
        } catch (FileNotFoundException e) {
            //skip this file.
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
            curr.setFileName(substringAfterLast(rootPath.get(fileCounter.get() - 1), "\\"));
            StringBuilder docCity = new StringBuilder();
            String docLang = "";
            boolean isLanguage = false;
            String[] docArr = split(document.toString(), '\n');
            StringBuilder line = new StringBuilder(), tag = new StringBuilder();
            for (int i = 0; i < docArr.length; i++) {
                docArr[i] = trim(docArr[i]);
                if (startsWith(docArr[i], "<")) {
                    if (containsIgnoreCase(docArr[i], "f p=\"104\"")) {
                        docCity = new StringBuilder(docArr[i] + docArr[i + 1] + docArr[i + 2]);
                    }
                    if (containsIgnoreCase(docArr[i], "f p=\"105\"")) {
                        docLang = trim(docArr[i + 1]);
                        isLanguage = true;
                    }
                    String[] lineArr = splitPreserveAllTokens(docArr[i], ">", 2);
                    docArr[i] = trim(lineArr[1]);
                    if (line.length() > 0 && docArr[i].isEmpty()) {
                        if (!lineArr[0].contains("/")) continue;
                        tag.append(upperCase(splitPreserveAllTokens(lineArr[0], "/")[1]));
                        curr.addAttributes(trim(tag.toString()), trim(line.toString()));
                        tag.setLength(0);
                        line.setLength(0);
                    }
                    if (docArr[i].endsWith(">")) {
                        docArr[i] = trim(splitPreserveAllTokens(docArr[i], "<", 2)[0]);
                    }
                }
                if (docArr[i].isEmpty()) continue;
                line.append(" ").append(docArr[i]);
            }
            if (curr.hasCity()) {
                createAndUpdateCity(curr, docCity);
            }
            if (isLanguage) {
                createAndUpdateLanguage(curr, docLang);
            }
            docList.add(curr);

        }
    }

    /**
     * creates a City and puts it in the CityInfo
     *
     * @param doc  - the document that the city was stated
     * @param lang - the actual language
     */
    private void createAndUpdateLanguage(Doc doc, String lang) {
        if (!lang.equals("")) {
            LanguagesInfo languagesInfo = LanguagesInfo.getInstance();
            languagesInfo.addLanguageToList(lang);
            doc.setLanguage(lang);
        }
    }

    /**
     * creates a City and puts it in the CityInfo
     *
     * @param doc           - the document that the city was stated
     * @param stringBuilder - the actual string
     */
    private void createAndUpdateCity(Doc doc, StringBuilder stringBuilder) {
        String tag = trim(substringBetween(stringBuilder.toString(), ">", "<"));
        if (tag != null && !tag.equals("")) {
            CityInfo cityInfo = CityInfo.getInstance();
            cityInfo.setInfo(tag, doc);
        }
    }


    /**
     * get a list of 'Doc' from a single File.
     * the function works as a queue, each time it's called it will return the next file
     * in a form of Doc's list.
     *
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
     *
     * @return true if there are unread files, else false
     */
    public static boolean hasNextFile() {
        return fileCounter.get() < rootPath.size();
    }


    /**
     * private function to make corpus as asked.
     * in case its a dir with only files
     *
     * @param path - path to the corpus
     */
    private void corpusCreate(String path) {
        File root = new File(path);
        File[] files = root.listFiles();
        File[] dirs = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                dirs[i] = new File(files[i].getAbsolutePath() + "dir\\");
                try {
                    FileUtils.forceMkdir(dirs[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                files[i].renameTo(new File(dirs[i].getAbsolutePath() + "\\" + files[i].getName()));
                if (dirs[i].isDirectory()) {
                    dirs[i].renameTo(new File(dirs[i].getAbsolutePath().substring(0, dirs[i].getAbsolutePath().length() - 3)));
                }
            }
        }
    }

}
