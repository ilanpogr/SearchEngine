package ReadFile;

import TextContainers.*;
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

    private ArrayList<String> docList;
    private ArrayList<String> textList;
    private ArrayList<String> docNumList;
    //    private Map<String,String> docMap = new M
    private static ArrayList<String> rootPath;
    private static int docCounter = 0;
    private static int fileCounter = 0;
    private static String currPath;

    public ReadFile() {
        this.docList = new ArrayList<>();
        this.textList = new ArrayList<>();
        this.docNumList = new ArrayList<>();
    }

    public ReadFile(String path) {
        this();
        if (rootPath==null)
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
        docList = new ArrayList<>();
        textList = new ArrayList<>();
        docNumList = new ArrayList<>();
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
            docList.addAll(Arrays.asList(string.split("</TEXT>")));
            docList.remove(docList.size() - 1);
            extractTags();
//            extractDocNums();
            extractText();
//            double end = System.currentTimeMillis();
//            System.out.println("Time took to read files: " + (end - start) / 1000 + " sec.");
//            System.out.println();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void extractTags() {

    }



    private void extractText() {
        for (int i = 0; i < docList.size(); i++) {
            String text = docList.get(i);
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

    }

    public ArrayList<Doc> getFileList() {
        if (fileCounter<rootPath.size()){
            return readFromFile(rootPath.get(fileCounter++));
        }
        return null;
    }

    public void readFiles(String path) {
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


    }
}
