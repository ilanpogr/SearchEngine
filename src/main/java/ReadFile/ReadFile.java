package ReadFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadFile {


    private ArrayList<String> docList = new ArrayList<>();
    private ArrayList<String> textList = new ArrayList<>();
    private ArrayList<String> docNumList = new ArrayList<>();
    private static int docCounter = 0;
    private static int fileCounter = 0;
    private static String currPath = "";

    public void readFiles(String  path) {
        if (path == null) {
            System.out.println("No path given");
        }
        try {
            Stream<Path> subPaths = Files.walk(Paths.get(path));
            List<String> fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());

//            fileList.forEach(System.out::println);
//            File dir = new File(path.toString());
//            File [] files = dir.listFiles();
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

    private void clearLists() {
        docList = new ArrayList<>();
        textList = new ArrayList<>();
        docNumList = new ArrayList<>();
    }

    private void readFromFile(String path) {

        try {

            double start = System.currentTimeMillis();


            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1 + " "));
//            System.out.println(stringBuilder);
            String string = stringBuilder.toString();
            docList.addAll(Arrays.asList(string.split("</TEXT>")));
            docList.remove(docList.size() - 1);
            extractDocNums();
            extractText();
//            System.out.println();
            while (textList.size() > 0) {
                docCounter++;
//                if(textList.get(0).startsWith("FT924-11569"))
                textList.remove(0);
//                else {textList.remove(0);}
//                    System.out.print(this.docNumList.remove(0));
            }
//            System.out.println();
//            HashMap<String,String> parsed=Parse.parse(new String[]{textList.remove(0)});
            double end = System.currentTimeMillis();

//            System.out.println("Time took to read files: " + (end - start) / 1000 + " sec.");
//            System.out.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*public static void main(String[] args) {

//        String baseDir = (String)System.getProperties().get("user.dir");
//        String filesPath =  baseDir + "/src/main/java/FB396001";
//        readFromFile(filesPath);
        String filesPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
        Path path = Paths.get(filesPath);
        ReadFile readFile = new ReadFile();
        double s = System.currentTimeMillis();
        readFile.readFiles(path);
        double e = System.currentTimeMillis();
        int sec = ((int) ((e - s) / 1000) % 60);
        int min = ((int) ((e - s) / 60000));
        System.out.println("\nTime took to read whole Corpus: " + (e - s) / 1000 + " Seconds");
        System.out.println("\twhich is " + min + ":" + (sec > 9 ? sec : "0" + sec) + " Minuts");
        System.out.println("Doc count: " + docCounter + "\tFile count: " + fileCounter);
//
//        extractDocNums();
    }*/

    private void extractDocNums() {
        for (int i = textList.size(); i < docList.size(); i++) {
            String s = "";
            String[] s1 = docList.get(i).split("</DOCNO>");
            s = s1[0];
            docList.set(i, s1[1]);
            s = s.split("<DOCNO>")[1].trim();
            docNumList.add(s);
        }
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
//                System.out.println("\n\n\n\t\tNo TEXT tag!!!\n\n");
//                System.out.println("\n\n\n\t\tNo TEXT tag!!!\n\n");
//                System.out.println("\n\n\n\t\tNo TEXT tag!!!\n\n");
//                continue;
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
}
