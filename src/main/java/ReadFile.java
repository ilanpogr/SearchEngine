import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadFile {

    private static ArrayList<String> DocNumList = new ArrayList<>();
    private static List<String> docList = new ArrayList<>();
    private static List<String> textList = new ArrayList<>();
    private static List<String> docNumList = new ArrayList<>();





//        private String getFromPropertiesBaseDir() throws IOException {
//        Properties props = new Properties();
//        props.load(this.getClass().getResourceAsStream("project.properties"));
//        return (String)props.get("project.basedir");
//    }


    /**
     * Apply Regular Expressions on the contents of a file
     *
     * FUNCTION WAS TAKEN FROM:
     * https://www.java-tips.org/java-se-tips-100019/37-java-util-regex/1716-how-to-apply-regular-expressions-on-the-contents-of-a-file.html
     *
     * @param filename: the file we want to read from
     * @return contents of a file in a CharSequence object
     * @throws IOException: yes! of course I'll have soup before dessert..
     */
    // Converts the contents of a file into a CharSequence
    private static CharSequence fromFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }

    //TODO: learn how to use maven: properties and on and on and on and on..

    private static ArrayList<String> readByRegEx(String filesPath, String regEx){
        ArrayList<String> textDic = new ArrayList<>();
        try {
            // Create matcher on file
            Pattern pattern = Pattern.compile(regEx,Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(fromFile(filesPath));
            // Find all matches
            while (matcher.find()) {
                if(matcher.group().length() != 0){
//                    System.out.println(matcher.group().trim());
                    textDic.add(matcher.group().trim());
                }
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        return textDic;
    }

    public static void readFiles(Path path){
        if (path==null){
            System.out.println("No path given");
        }
        try{
            Stream<Path> subPaths = Files.walk(path);
            List<String> fileList = subPaths.filter(Files::isRegularFile).map(Objects::toString).collect(Collectors.toList());
//            fileList.forEach(System.out::println);
//            File dir = new File(path.toString());
//            File [] files = dir.listFiles();
            for (String file : fileList) {
                readFromFile(file);
            }


        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private static void readFromFile(String path){

        try {

            double start = System.currentTimeMillis();


            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1+" "));
            System.out.println(stringBuilder);
            String string = stringBuilder.toString();
            docList.addAll(Arrays.asList(string.split("</TEXT>")));
            docList.remove(docList.size()-1);
            extractDocNums();
            extractText();
            HashMap<String,String> parsed=Parse.parse(new String[]{textList.remove(0)});
            double end = System.currentTimeMillis();

            System.out.println("Time took to read files: " + (end - start)/1000 +" sec.");
            System.out.println();



            int x=0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

//        String baseDir = (String)System.getProperties().get("user.dir");
//        String filesPath =  baseDir + "/src/main/java/FB396001";
//        readFromFile(filesPath);
        String filesPath =  "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\corpus";
        Path path = Paths.get(filesPath);
        readFiles(path);
//
//
//        extractDocNums();
    }

    private static void extractDocNums() {
        for (int i=textList.size() ;i<docList.size();i++) {
            String s="";
            String [] s1= docList.get(i).split("</DOCNO>");
            s=s1[0];
            docList.set(i, s1[1]);
            s=s.split("<DOCNO>")[1].trim();
            docNumList.add(s);
        }
    }

    private static void extractText(){
        for (int i = 0; i < docList.size(); i++) {
            String text = docList.get(i).split("<TEXT>")[1].trim();
            text = text.replaceAll("( )+", " ");
            text = text.replace("CELLRULE", " ");
            text = text.replace("TABLECELL", " ");
            text = text.replace("CVJ=\"C\"", " ");
            text = text.replace("CHJ=\"C\"", " ");
            text = text.replace("CHJ=\"R\"", " ");
            textList.add(docNumList.get(i)+" "+text);
        }

    }
}
