package ReadFile;

import TextContainers.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadFile {

    //TODO- return all Tags as Atributes from a Doc

    private ArrayList<String> unInstancedDocList;
    private ArrayList<Doc> docList;
    private static ArrayList<String> rootPath;
    private static int fileCounter = 0;
    //    private ArrayList<String> textList;
//    private ArrayList<String> docNumList;
//    private Map<String,String> docMap = new M
    private static String currPath;
    private static int docCounter = 0;

    public ReadFile() {
        this.unInstancedDocList = new ArrayList<>();
        this.docList = new ArrayList<>();
//        this.textList = new ArrayList<>();
//        this.docNumList = new ArrayList<>();
    }

    public ReadFile(String path) {
        this();
        if (rootPath == null)
            rootPath = new ArrayList<>(createPathsList(Paths.get(path)));
    }

    private static List<String> createPathsList(Path path) {
        List<String> fileList = null;
        try {
            Stream<Path> subPaths = Files.walk(path);
            fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private void clearLists() {
        unInstancedDocList.clear();
        docList.clear();
//        textList = new ArrayList<>();
//        docNumList = new ArrayList<>();
    }

    private ArrayList<Doc> readFromFile(String path) {
        try {
//            double start = System.currentTimeMillis();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1 + " "));
//            System.out.println(stringBuilder);
            String string = stringBuilder.toString();
            unInstancedDocList.addAll(Arrays.asList(string.split("</DOC>")));
//            unInstancedDocList.remove(unInstancedDocList.size() - 1);
            createDocList();
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

    private void createDocList() {
        for (String doc : unInstancedDocList) {
            String[] document = {Jsoup.parse(doc).toString()};
            String[] docNum = new String[1];
            String[] docText = new String[1];
            extractTag(docNum, document, "docno");
//            docText[0]="";
            extractTag(docText, document, "text");
            Doc curr = new Doc(docNum, docText);
            String[] docArr = document[0].split("\n");
            String line = "", tag = "";
            for (int i = 0; i < docArr.length; i++) {
                docArr[i] = docArr[i].trim();
                if (docArr[i].startsWith("<")) {
                    String[] lineArr = docArr[i].split(">", 2);
                    docArr[i] = lineArr[1].trim();
                    if (!line.isEmpty() && docArr[i].isEmpty()) {
                        if (!lineArr[0].contains("/"))continue;
                        tag = lineArr[0].split("/")[1].toUpperCase();
                        curr.addAttributes(new String[]{tag.trim(), line.trim()});
                        tag = "";
                        line = "";
                    }
                    if (docArr[i].endsWith(">")) {
                        docArr[i] = docArr[i].split("<", 2)[0].trim();
                    }
                }
                if (docArr[i].isEmpty()) continue;
                line += " " + docArr[i];
            }
            docList.add(curr);
        }
    }

    private void extractTag(String[] element, String[] document, String delimiter) {
        String[] tmp = document[0].split(delimiter + ">", 3);
        if (tmp.length<3) {
            element[0]="";
            return;
        }
        if (!delimiter.equalsIgnoreCase("text")) {
            document[0] = tmp[0].substring(0, tmp[0].length() - 1).trim()+ "\n " + tmp[2].trim();
        }
            element[0] = tmp[1].substring(0, tmp[1].length() - 2).trim();
    }

    public ArrayList<Doc> getFileList() {
        if (fileCounter < rootPath.size()) {
            return readFromFile(rootPath.get(fileCounter++));
        }
        return null;
    }

    /*private void extractText() {
        for (int i = 0; i < unInstancedDocList.size(); i++) {
            String text = unInstancedDocList.get(i);
            String[] s = text.split("<TEXT>");
            if (s.length > 1)
                text = s[1].trim();
            else {
                String textTmp = text;
                text = s[0];
                s = text.split("<P>");
                if (s.length > 1)
                    for (int j = 1; j < s.length; j += 2) {
                        if (j == 1)
                            text = "";
                        text += s[j].split("</P>")[0];
                    }
                else
                    docCounter--;
            }
            text = text.replaceAll("( )+", " ");
            text = text.replace("CELLRULE", " ");
            text = text.replace("TABLECELL", " ");
            text = text.replace("CVJ=\"C\"", " ");
            text = text.replace("CHJ=\"C\"", " ");
            text = text.replace("CHJ=\"R\"", " ");
            textList.add(docNumList.get(i) + " " + text);
        }

    }*/

    /*public void readFiles(String path) {
        if (path == null) {
            System.out.println("No path given");
        }
        try {
            Stream<Path> subPaths = Files.walk(Paths.get(path));
            List<String> fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());
            for (String file : fileList) {
                currPath = fileList.get(fileCounter);
                fileCounter++;
                this.readFromFile(file);
                this.clearLists();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }*/
}
