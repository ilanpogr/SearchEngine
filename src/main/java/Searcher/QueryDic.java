package Searcher;

import com.sun.corba.se.impl.orbutil.ObjectWriter;

import java.io.*;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class QueryDic {
    private static TreeMap<Integer, QuerySol> qmap = new TreeMap<Integer, QuerySol>(Integer::compareTo);
    private static int pointer = 0;


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
