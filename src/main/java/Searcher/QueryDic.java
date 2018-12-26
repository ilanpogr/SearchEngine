package Searcher;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import org.ibex.nestedvm.util.Seekable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class QueryDic {
    private static QueryDic yoda=null;
    private static TreeMap<Integer, QuerySol> qmap = null;
    private static HashMap<String,Integer> inv_qmap=null;
    private static int pointer = 0;




    public boolean containsNum(String num){
        if (isNumeric(num)) return qmap.containsKey(Integer.parseInt(num));
        return false;
    }

    public boolean containsQuery(String query){
        if (inv_qmap.containsKey(query)){
            return true;
        }
        return false;
    }

    /**
     * by semantics and similarities, this function will retrieve the rank
     * that the query is like a solved query in the dictionary
     * @param query - the query we want to look for
     * @return number in [0,1]
     *          0 - not in dictionary
     *          1 - the query is in the dictionary
     */
    public double queryEvaluator(String query){
        //todo- imlement
        return 0;
    }

    public ArrayList<String> getSolutions (String query){
        if (isNumeric(query)){
            return qmap.get(Integer.parseInt(query)).getSols();
        }else if (inv_qmap.containsKey(query)){
            return qmap.get(inv_qmap.get(query)).getSols();
        }
        return null;
    }

    public static QueryDic getInstance() {
        if (yoda==null){
            yoda=new QueryDic();
        }
        return yoda;
    }

    private QueryDic() {
        try {
            String dicName = "tested.queries";
            String postings = "queries.postings";
            File dic = new File(getClass().getClassLoader().getResource(dicName).getFile());
            File post = new File(getClass().getClassLoader().getResource(postings).getFile());
            dic.setWritable(false);
            post.setWritable(false);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dic));
            String q = bufferedReader.readLine();
            while (q!=null){
                QuerySol querySol = new QuerySol(q);
                RandomAccessFile posting = new RandomAccessFile(post,"r");
                posting.skipBytes(querySol.getPostingPointer());
                querySol.addPosting(posting.readLine());
                qmap.put(querySol.getPostingPointer(),querySol);
                inv_qmap.put(querySol.getTitle(),querySol.getqNumAsInt());
                if (containsAny(querySol.getTitle()," ,.-")){

                }
            }
        } catch (Exception e) {

        }
//        properties = new Properties();
//
//        // Load the properties file into the Properties object
//        String propertiesFileName = "project.properties";
//        try {
//            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName));
//        } catch (IOException e) {
//            String message = "Exception while reading properties file '" + propertiesFileName + "':" + e.getLocalizedMessage();
//            throw new RuntimeException(message, e);
//        }
//        return properties;
    }

    public static void saveNewSQueries(String queriesPath, String targetPath) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(queriesPath));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetPath, true));
            String line = bufferedReader.readLine().trim();
            StringBuilder stringBuilder = new StringBuilder();
            String query = "";
            while (line != null) {
                if (isEmpty(line)) {
                    line = bufferedReader.readLine();
                    continue;
                }
                if (startsWithIgnoreCase(line, "<num>")) {
                    stringBuilder.append(substringAfterLast(trim(line), " ")).append("|");
                } else if (startsWithIgnoreCase(line, "<title>")) {
                    line = substringAfter(line, ">");
                    if (contains(line, ":"))
                        line = substringAfter(line, ":");
                    else if (isEmpty(line)){
                        line = bufferedReader.readLine();
                    }
                    if (line!=null && !isEmpty(line))
                        stringBuilder.append(trim(line)).append("|");
                } else if (startsWithIgnoreCase(trim(line), "<desc>")) {
                    line = bufferedReader.readLine();
                    while (line != null && !startsWithIgnoreCase(line, "<narr>")) {
                        line = checkAndAddline(bufferedReader, stringBuilder, line);
                    }
                }
                if (startsWithIgnoreCase(line, "<narr>")) {
                    line = bufferedReader.readLine();
                    stringBuilder.append("|");
                    while (line != null && !startsWithIgnoreCase(line, "</top>")) {
                        if (isEmpty(line)) {
                            line = bufferedReader.readLine();
                            continue;
                        }
                        stringBuilder.append(trim(line)).append(";");
                        line = bufferedReader.readLine();
                    }
                    stringBuilder.append("|\n");
//                    stringBuilder.append("|");
                    query = stringBuilder.toString();
                    QuerySol sol = new QuerySol(query, pointer);
//                    stringBuilder.append(pointer).append("|\n");
//                    qmap.put(sol.getqNumAsInt(),sol);
                    pointer += query.getBytes().length + 1;
                }
                line = bufferedReader.readLine();
            }
            bufferedWriter.write(query);
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {

        }
    }

    public static int getPointer() {
        return pointer;
    }

    public static void main(String[] args) {
        saveNewSQueries("C:\\Users\\User\\Documents\\SearchEngineTests\\solutions\\totrim","C:\\Users\\User\\Documents\\SearchEngineTests\\solutions\\trimmed.post");
    }
    public static void saveSolutions(String solutionsPath, String targetPath) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(solutionsPath));
            File file = new File(targetPath);
            int i=0;
            while (file.exists() && file.isFile()){
                file = new File(substringBeforeLast(targetPath,".")+"("+(i++)+")."+substringAfterLast(targetPath,"."));
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            String line = bufferedReader.readLine().trim();
            String num = substringBefore(line, " ");
            StringBuilder stringBuilder = new StringBuilder(num);
            stringBuilder.append(",");
            while (line != null) {
                String [] splitted = split(line, " ");
                if (!splitted[splitted.length-1].equalsIgnoreCase("0")){
                    if (num.equalsIgnoreCase(splitted[0])){
                        stringBuilder.append(splitted[2]).append("|");
                    } else {
                        num = splitted[0];
                        stringBuilder.append("\n").append(num).append(",");
                        stringBuilder.append(splitted[2]).append("|");
                    }
                }
                line = bufferedReader.readLine();
            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {

        }
    }

    private static String checkAndAddline(BufferedReader bufferedReader, StringBuilder stringBuilder, String line) throws IOException {
        if (!isEmpty(line)) {
            stringBuilder.append(trim(line)).append(";");
        }
        return bufferedReader.readLine();
    }



//    public static void main(String[] args) {
//        File file = new File("C:\\Users\\User\\Documents\\SearchEngineTests\\solutions\\sols.post");
//        File file2 = new File("C:\\Users\\User\\Documents\\SearchEngineTests\\solutions\\qs.post");
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(file2));
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\User\\Documents\\SearchEngineTests\\solutions\\trimmed.post", true));
//            String line = bufferedReader.readLine().trim();
//            StringBuilder stringBuilder = new StringBuilder();
//            String query = "";
//            while (line != null) {
//                if (isEmpty(line)){
//                    line = bufferedReader.readLine();
//                    continue;
//                }
//                if (startsWithIgnoreCase(line, "<num>")) {
//                    stringBuilder.append(substringAfterLast(trim(line), " ")).append("|");
//                } else if (startsWithIgnoreCase(line, "<title>")) {
//                    line = substringAfter(line, ">");
//                    if (contains(line, ":"))
//                        line = substringAfter(line, ":");
//                    stringBuilder.append(trim(line)).append("|");
//                } else if (startsWithIgnoreCase(trim(line), "<desc>")) {
//                    line = bufferedReader.readLine();
//                    while (line != null && !startsWithIgnoreCase(line, "<narr>")) {
//                        if (isEmpty(line)){
//                            line = bufferedReader.readLine();
//                            continue;
//                        }
//                        stringBuilder.append(trim(line)).append(";");
//                        line = bufferedReader.readLine();
//                    }
//                }
//                if (startsWithIgnoreCase(line, "<narr>")) {
//                    line = bufferedReader.readLine();
//                    stringBuilder.append("|");
//                    while (line != null && !startsWithIgnoreCase(line, "</top>")) {
//                        if (isEmpty(line)){
//                            line = bufferedReader.readLine();
//                            continue;
//                        }
//                        stringBuilder.append(trim(line)).append(";");
//                        line = bufferedReader.readLine();
//                    }
//                    stringBuilder.append("|\n");
//                    query = stringBuilder.toString();
//                    QuerySol sol = new QuerySol(query, pointer);
//                    qmap.put(sol.getqNumAsInt(),sol);
//                    pointer += query.getBytes().length + 1;
//                }
//                line = bufferedReader.readLine();
//            }
//            bufferedWriter.write(query);
//            bufferedWriter.flush();
//            bufferedReader.close();
//            bufferedWriter.close();
//        } catch (Exception e) {
//
//        }
//    }
        }
