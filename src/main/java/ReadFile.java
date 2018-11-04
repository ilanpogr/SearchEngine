import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {

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

//    private String getFromPropertiesBaseDir() throws IOException {
//        Properties props = new Properties();
//        props.load(this.getClass().getResourceAsStream("project.properties"));
//        return (String)props.get("project.basedir");
//    }

    //TODO: learn how to use maven: properties and on and on and on and on..

    public static void main(String[] args) {
        double reStart = System.currentTimeMillis();
        String baseDir = (String)System.getProperties().get("user.dir");
        String filesPath =  baseDir + "/src/main/java/FB396001";
        try {
            // Create matcher on file
            //"^<TEXT>[\\w\\W]*</TEXT>$"
            String regEx = "<TEXT>[^<]*</TEXT>";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(fromFile(filesPath));
            // Find all matches
            int i=0;
            System.out.println();
            while (matcher.find()) {
                if(matcher.group().length() != 0){
//                    System.out.println(matcher.group().trim());
                    System.out.print((i++) + ",");
                }
//                 Get the matching string
            }
            System.out.println();
            double reEnd = System.currentTimeMillis();
            System.out.println("regex time: "+(reEnd-reStart));
            double start = System.currentTimeMillis();
            baseDir = (String)System.getProperties().get("user.dir");
            filesPath =  baseDir + "/src/main/java/FB396001";
            // Create matcher on file
            //"^<TEXT>[\\w\\W]*</TEXT>$"
            String s = fromFile(filesPath).toString();
            i=0;
            int j=2;
            // Find all matches
            System.out.println();
            while (j<s.length()) {
                if(s.charAt(j-2)=='<' && s.charAt(j-1)=='T' && s.charAt(j)=='E'){
                    System.out.print((i++) + ",");
                }
                j++;
                // Get the matching string
            }
            System.out.println();
            double end = System.currentTimeMillis();
            System.out.println("string time: "+(end-start));

        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
