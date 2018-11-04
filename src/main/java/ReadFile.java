import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {

    private static String regEx_Text = "^(<TEXT>[^<]*</TEXT>)$";



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
    public static CharSequence fromFile(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }

    //TODO: learn how to use maven: properties and on and on and on and on..

    private static List<String> readByRegEx(String filesPath, String regEx){
        List<String> textDic = new LinkedList<>();
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


    public static void main(String[] args) {
        String baseDir = (String)System.getProperties().get("user.dir");
        String filesPath =  baseDir + "/src/main/java/FB396001";
        List<String> textDic = readByRegEx(filesPath, regEx_Text);
        while(!textDic.isEmpty() && textDic.iterator().hasNext()){
            System.out.println(textDic.iterator().next());
            textDic.iterator().next();

        }
    }
}
