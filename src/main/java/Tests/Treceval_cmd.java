package Tests;

import Master.Master;
import Ranker.Ranker;
import Ranker.MultiQueriesHandler;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Treceval_cmd {

    static String directory = "C:\\Ilan\\4";
    static String command = "treceval.exe qrels.txt results.txt";

    private static TreeMap<String, String> dict = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> cache = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> docs = new TreeMap<>();

    private static StringBuilder runCmd() {
        StringBuilder s = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"" + directory + "\" && " + command);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                s.append(line);
            }
            return s;
        } catch (IOException e) {
            e.getStackTrace();
            return null;
        }
    }

    public void simulateSearch2Treceval(ArrayList<String> queries, ArrayList<String> queryNums, double k, double b, double delta, double idf) {


            Ranker ranker = new Ranker();
//            ranker.setBM25Values(1.85, 0.6, 0.25, 0.5);
            ranker.setBM25Values(k, b, delta, idf);
//        MultiQueriesHandler multiQueriesHandler =new MultiQueriesHandler();
//        multiQueriesHandler.parseMultiQuery();
//        ArrayList<HashMap<String,Integer>> relevants = multiQueriesHandler.getRelevantList();
//        ArrayList<HashMap<String,Integer>> notRelevants = multiQueriesHandler.getNotRelevantList();
            for (int i = 0; i < queries.size(); i++) {
//                HashMap<String, Integer> query = relevants.get(i);
//                HashMap<String, Integer> anti_query = notRelevants.get(i);
                HashMap<String, Integer> query = Master.makeQueryDic(queries.get(i));
                TreeMap<Double, String> res = ranker.rank(directory, dict, cache, docs, query);
//                TreeMap<Double, String> ares = ranker.rank(directory, dict, cache, docs, anti_query);

                makeResultsFile(res, queryNums.get(i));

            }

    }

    private void makeResultsFile(TreeMap<Double, String> res, String s) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(directory + "\\results.txt"), true));
            for (Map.Entry<Double, String> entry : res.entrySet()) {
                String doc = entry.getValue();
                bufferedWriter.write(s + " 0 " + doc + " 1 0 si\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
        }
    }

    public double[] getResultRanked() {
        StringBuilder s = runCmd();
        String values = "";
        if (s != null)
            values = s.toString();

        double a_retrieved = Double.parseDouble(trim(substringBetween(values, "Retrieved:", "Relevant:")));
        double b_relevant = Double.parseDouble(trim(substringBetween(values, "Relevant:", "Rel_ret:")));
        double c_rel_ret = Double.parseDouble(trim(substringBetween(values, "Rel_ret:", "Interpolated Recall")));

        double r_precision = Double.parseDouble(trim(substringAfter(values, "Exact:")));

        double precision = c_rel_ret / a_retrieved;
        double recall = c_rel_ret / b_relevant;
        double final_rank = 2 * c_rel_ret / (a_retrieved + b_relevant);

//        System.out.println("R-Percision: " + r_precision + ", " + "Percision: " + precision + ", " + "Recall: " + recall + ", " + "Rank: " + final_rank);

        return new double[]{r_precision, precision, recall, final_rank,c_rel_ret};
    }


    public void setDics(TreeMap<String, String> termDictionary, TreeMap<String, String> cache, TreeMap<String, String> docDic) {
            dict = termDictionary;
            Treceval_cmd.cache = cache;
            docs = docDic;
    }
}
