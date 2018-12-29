package Searcher;

import Indexer.WrieFile;
import Ranker.Ranker;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class QueryDic {
    private static QueryDic yoda = null;
    private static TreeMap<Integer, QuerySol> qmap = null;
    private static HashMap<String, Integer> inv_qmap = null;
    private static HashMap<String, ArrayList<Integer>> wordsToQueries = null;
    private static int pointer = 0;


    public boolean containsNum(String num) {
        if (isNumeric(num)) return qmap.containsKey(Integer.parseInt(num));
        return false;
    }

    public boolean containsQuery(String query) {
        if (inv_qmap.containsKey(query)) {
            return true;
        }
        return false;
    }

    /**
     * by semantics and similarities, this function will retrieve the rank
     * that the query is like a solved query in the dictionary
     *
     * @param query - the query we want to look for
     * @return number in [0,1]
     * 0 - not in dictionary
     * 1 - the query is in the dictionary
     */
    public double queryEvaluator(QuerySol query) {
        if (query.getEvaluationRank()==-1) setMostEvaluatedQuery(query);
        return query.getEvaluationRank();
    }

    /**
     * get the QuerySol which is the highest ranked (to the given query)
     * @param query - the given query
     * @return a copy of the original QuerySol
     */
    public QuerySol getEvaluatedQuerySol(QuerySol query){
        int qNum = query.getEvaluationOtherQueryNum();
        if (qNum==-1) setMostEvaluatedQuery(query);
        return new QuerySol(qmap.get(query.getEvaluationOtherQueryNum()));
    }

    /**
     * set the evaluation to a given query
     * @param query - the query that will be evaluated
     */
    private void setMostEvaluatedQuery(QuerySol query) {
        if (inv_qmap.containsKey(query.getTitle())) {
            QuerySol querySol = qmap.get(query.getqNumAsInt());
            if (querySol.equals(query))
                query.setEvaluation(querySol.getqNumAsInt(),1);
        }
        String[] words = split(query.getTitle(), " ,.-");
        HashMap<Integer, Integer> queryIndex = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            ArrayList<Integer> queries = wordsToQueries.get(words[i]);
            if (queries == null || queries.size() == 0) {
                continue;
            }
            for (int j = 0; j < queries.size(); j++) {
                if (!queryIndex.containsKey(queries.get(j)))
                    queryIndex.put(queries.get(j), 1);
                else {
                    queryIndex.put(queries.get(j), queryIndex.get(queries.get(j)) + 1);
                }
            }
        }
        int maxCount = 0;
        for (Integer counter : queryIndex.values()) {
            if (counter > maxCount) maxCount = counter;
        }
        ArrayList<QuerySol> potentialQueries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : queryIndex.entrySet()) {
            Integer queryNum = entry.getKey();
            Integer counter = entry.getValue();
            if (counter == maxCount) {
                potentialQueries.add(qmap.get(queryNum));
            }
        }

        double maxEvaluated = 0;
        double[] rates = new double[potentialQueries.size()];
        for (int i = 0; i < rates.length; i++) {
            String[] title = potentialQueries.get(i).getTitleArray();
            for (int j = 0; j < words.length; j++) {
                for (int k = 0; k < title.length; k++) {
                    rates[i] = Double.max(rates[i], Ranker.getWeigthedSimilarity(title[k], words[j]));
                }
            }
            if (maxEvaluated < rates[i]) {
                maxEvaluated = rates[i];
                maxCount = i;
            }
        }
        query.setEvaluation(maxCount,maxEvaluated);
    }

    public ArrayList<QuerySol> readQueries(String path) {
        try {
            String[] queries = split(saveNewQueries(path, ""), "\n"); //no target path - will not write to disk
            ArrayList<QuerySol> querySols = new ArrayList<>();
            for (int i = 0; i < queries.length; i++) {
                QuerySol querySol = new QuerySol(queries[i], -1);
                ArrayList<String> sols = null;
                try {
                    sols = qmap.get(inv_qmap.get(querySol.getTitle())).getSols();
                } catch (Exception e) {

                }
                if (sols != null) {
                    querySol.addPosting(querySol.getqNum() + "," + join(sols, "|"));

                }
                querySols.add(querySol);
            }
            return querySols;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getSolutions(String query) {
        if (isNumeric(query)) {
            return qmap.get(Integer.parseInt(query)).getSols();
        } else if (inv_qmap.containsKey(query)) {
            return qmap.get(inv_qmap.get(query)).getSols();
        }
        return null;
    }

    public static QueryDic getInstance() {
        if (yoda == null) {
            yoda = new QueryDic();
        }
        return yoda;
    }

    /**
     * Ctor - private (singleton)
     *///todo- add substrings to wordsToQueries
    private QueryDic() {
        try {
            String dicName = "tested.queries";
            String postings = "queries.postings";
//            ClassLoader classLoader = getClass().getClassLoader();
//            File dic = new File(classLoader.getResource(dicName).getFile());
            File dic = new File("src\\main\\resources", dicName);
            File post = new File("src\\main\\resources", "sols.post");
            dic.setWritable(false);
            post.setWritable(false);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dic.getCanonicalFile()));
            String q = bufferedReader.readLine();
            qmap = new TreeMap<>();
            inv_qmap = new HashMap<>();
            wordsToQueries = new HashMap<>();
            while (q != null) {
                QuerySol querySol = new QuerySol(q);
                RandomAccessFile posting = new RandomAccessFile(post, "r");
                posting.skipBytes(querySol.getPostingPointer());
                querySol.addPosting(posting.readLine());
                qmap.put(querySol.getqNumAsInt(), querySol);
                inv_qmap.put(querySol.getTitle(), querySol.getqNumAsInt());
                if (containsAny(querySol.getTitle(), " ,.-")) {
                    String[] words = split(querySol.getTitle(), " ,.-");
                    for (int i = 0; i < words.length; i++) {
                        if (!wordsToQueries.containsKey(words[i])) {
                            wordsToQueries.put(words[i], new ArrayList<>());
                        }
                        wordsToQueries.get(words[i]).add(querySol.getqNumAsInt());
                    }
                }
                q = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveNewQueries(String queriesPath, String targetPath) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(queriesPath));
            BufferedWriter bufferedWriter = null;
            if (!isEmpty(targetPath))
                bufferedWriter = new BufferedWriter(new FileWriter(WrieFile.getTmpFile(targetPath, ""), true));
            String line = bufferedReader.readLine().trim();
            StringBuilder stringBuilder = new StringBuilder();
//            String query = "";
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
                    else if (isEmpty(line)) {
                        line = bufferedReader.readLine();
                    }
                    if (line != null && !isEmpty(line))
                        stringBuilder.append(trim(line)).append("|");
                } else if (startsWithIgnoreCase(trim(line), "<desc>")) {
                    line = bufferedReader.readLine();
                    while (line != null && !startsWithIgnoreCase(line, "<narr>")) {
                        line = checkAndAddline(bufferedReader, stringBuilder, line);
                    }
                    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                }
                if (startsWithIgnoreCase(line, "<narr>")) {
                    line = bufferedReader.readLine();
                    stringBuilder.append("|");
                    while (line != null && !startsWithIgnoreCase(line, "</top>")) {
                        line = checkAndAddline(bufferedReader, stringBuilder, line);
                    }
                    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                    stringBuilder.append("|\n");
                }
                line = bufferedReader.readLine();
            }
            if (!isEmpty(targetPath)) {
                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static int getPointer() {
        return pointer;
    }

    public static void saveSolutions(String solutionsPath, String targetPath) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(solutionsPath));
            File file = WrieFile.getTmpFile(targetPath, "");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            String line = bufferedReader.readLine().trim();
            String num = substringBefore(line, " ");
            StringBuilder stringBuilder = new StringBuilder(num);
            stringBuilder.append(",");
            while (line != null) {
                String[] splitted = split(line, " ");
                if (!splitted[splitted.length - 1].equalsIgnoreCase("0")) {
                    if (num.equalsIgnoreCase(splitted[0])) {
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

    /**
     * updates the pointers from query dict to posting (can add from scratch)
     *
     * @param queriesPath   - the path of the dictionary
     * @param solutionsPath - the path of the posting
     * @param hasPointers   - if queriesPath already ha pointers
     */
    public static void updateQdictPointers(String queriesPath, String solutionsPath, boolean hasPointers) {
        try {
            pointer = 0;
            BufferedReader qr = new BufferedReader(new FileReader(queriesPath));
            BufferedReader sr = new BufferedReader(new FileReader(solutionsPath));
            File file = WrieFile.getTmpFile(queriesPath, "");
            BufferedWriter qw = new BufferedWriter(new FileWriter(file, true));
            String lineQ = qr.readLine().trim();
            String lineS = sr.readLine().trim();
            StringBuilder stringBuilder = new StringBuilder();
            while (lineQ != null && lineS != null) {
                if (equalsIgnoreCase(substringBefore(lineQ, "|"), substringBefore(lineS, ","))) {
                    stringBuilder.append(lineQ);
                    if (hasPointers) {
//                        lineQ = substringBeforeLast(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString(), "|");
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        stringBuilder.delete(stringBuilder.lastIndexOf("|") + 1, stringBuilder.length() - 1);
                    }
                    stringBuilder.append(Integer.toString(pointer, 36)).append("|\n");
                    qw.write(stringBuilder.toString());
                    stringBuilder.setLength(0);
                    pointer += lineS.getBytes().length + 1;
                } else if (compareIgnoreCase(lineQ, lineS) < 1) {
                    lineS = sr.readLine();
                }
                lineS = sr.readLine();
                lineQ = qr.readLine();
            }
            qr.close();
            sr.close();
            qw.close();
        } catch (Exception e) {

        }
    }

    private static String checkAndAddline(BufferedReader bufferedReader, StringBuilder stringBuilder, String line) throws IOException {
        line = trim(line);
        stringBuilder.trimToSize();
        if (!endsWith(stringBuilder, ";") && !isEmpty(line))
            stringBuilder.append(line).append("; ");
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
