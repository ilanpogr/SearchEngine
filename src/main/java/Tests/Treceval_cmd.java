package Tests;

import Master.Master;
import Ranker.Ranker;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Treceval_cmd {

    static String directory = "C:\\Users\\User\\Documents\\SearchEngineTests";
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
            for (int i = 0; i < queries.size(); i++) {
                HashMap<String, Integer> query = Master.makeQueryDic(queries.get(i));
                TreeMap<Double, String> res = ranker.rank(directory, dict, cache, docs, query);
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

        return new double[]{r_precision, precision, recall, final_rank};
    }


    public void setDics(TreeMap<String, String> termDictionary, TreeMap<String, String> cache, TreeMap<String, String> docDic) {
            dict = termDictionary;
            Treceval_cmd.cache = cache;
            docs = docDic;
    }


    /*
    351 0 FBIS4-66185 1 0 run-id
351 0 FT941-9999 1 0 run-id
351 0 FT934-4848 1 0 run-id
351 0 FT922-15099 1 0 run-id
351 0 FT931-932 1 0 run-id
351 0 FT921-2097 1 0 run-id
351 0 FT931-16104 1 0 run-id
351 0 FBIS3-59016 1 0 run-id
351 0 FT921-6603 1 0 run-id
351 0 FT932-6577 1 0 run-id
351 0 FT932-16710 1 0 run-id
351 0 FT942-12805 1 0 run-id
351 0 FT941-13429 1 0 run-id
351 0 FT922-3165 1 0 run-id
351 0 FT934-4856 1 0 run-id
351 0 FT931-10913 1 0 run-id
351 0 FT922-8324 1 0 run-id
351 0 FT944-15849 1 0 run-id
351 0 FT922-14936 1 0 run-id
351 0 FBIS4-20472 1 0 run-id
351 0 LA050190-0192 1 0 run-id
351 0 LA081390-0025 1 0 run-id
351 0 FBIS3-59836 1 0 run-id
351 0 LA111389-0074 1 0 run-id
351 0 FT924-9877 1 0 run-id
351 0 FT933-8945 1 0 run-id
351 0 FT922-10155 1 0 run-id
351 0 FT933-12469 1 0 run-id
351 0 FT942-1252 1 0 run-id
351 0 FT922-6599 1 0 run-id
351 0 FT922-3549 1 0 run-id
351 0 FBIS4-33167 1 0 run-id
351 0 FT932-15115 1 0 run-id
351 0 FT923-11397 1 0 run-id
351 0 FBIS4-44591 1 0 run-id
351 0 FBIS4-44559 1 0 run-id
351 0 FT932-17180 1 0 run-id
351 0 FT923-10607 1 0 run-id
351 0 FBIS3-60336 1 0 run-id
351 0 FT924-9876 1 0 run-id
351 0 FT934-13192 1 0 run-id
351 0 FT931-13121 1 0 run-id
351 0 FBIS3-20632 1 0 run-id
351 0 LA102490-0042 1 0 run-id
351 0 FBIS4-49573 1 0 run-id
351 0 FT931-3057 1 0 run-id
351 0 FT941-2146 1 0 run-id
351 0 FT911-932 1 0 run-id
351 0 FBIS3-11935 1 0 run-id
351 0 FT944-6105 1 0 run-id

    */
}
