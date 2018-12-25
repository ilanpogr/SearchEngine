package Searcher;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import org.ibex.nestedvm.util.Seekable;

import java.io.*;
import java.util.Properties;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class QueryDic {
    private static TreeMap<Integer, QuerySol> qmap = readQueryMap();
    private static int pointer = 0;


    private static TreeMap<Integer,QuerySol> readQueryMap() {
        return null;
    }

    private QueryDic(){
        try{
            String dicName = "tested.queries";
            String postings = "queries.postings";
            File dic = new File(getClass().getClassLoader().getResource(dicName).getFile());
            File post = new File(getClass().getClassLoader().getResource(postings).getFile());

//            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName));
        }catch (Exception e){

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

    public static void saveNewSQueries(String queriesPath, String targetPath){
        File file = new File(queriesPath);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(queriesPath));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetPath, true));
            String line = bufferedReader.readLine().trim();
            StringBuilder stringBuilder = new StringBuilder();
            String query = "";
            while (line != null) {
                if (isEmpty(line)){
                    line = bufferedReader.readLine();
                    continue;
                }
                if (startsWithIgnoreCase(line, "<num>")) {
                    stringBuilder.append(substringAfterLast(trim(line), " ")).append("|");
                } else if (startsWithIgnoreCase(line, "<title>")) {
                    line = substringAfter(line, ">");
                    if (contains(line, ":"))
                        line = substringAfter(line, ":");
                    stringBuilder.append(trim(line)).append("|");
                } else if (startsWithIgnoreCase(trim(line), "<desc>")) {
                    line = bufferedReader.readLine();
                    while (line != null && !startsWithIgnoreCase(line, "<narr>")) {
                        line  = checkAndAddline(bufferedReader, stringBuilder,line);
                    }
                }
                if (startsWithIgnoreCase(line, "<narr>")) {
                    line = bufferedReader.readLine();
                    stringBuilder.append("|");
                    while (line != null && !startsWithIgnoreCase(line, "</top>")) {
                        if (isEmpty(line)){
                            line = bufferedReader.readLine();
                            continue;
                        }
                        stringBuilder.append(trim(line)).append(";");
                        line = bufferedReader.readLine();
                    }
                    stringBuilder.append("|\n");
                    query = stringBuilder.toString();
                    QuerySol sol = new QuerySol(query, pointer);
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

    private static String checkAndAddline(BufferedReader bufferedReader, StringBuilder stringBuilder, String line) throws IOException{
        if (!isEmpty(line)){
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
