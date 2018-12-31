package Tests;

import Controller.ControllerMenu;
import Ranker.Ranker;
import Searcher.QuerySol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Treceval_cmd {

    static String directory = "C:\\Users\\User\\Documents\\SearchEngineTests";
    static String command = "treceval.exe qrels.txt ";

    private static TreeMap<String, String> dict = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> cache = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> docs = new TreeMap<>();

    /**
     * opens Windows Command Prompt, and enters the "treceval" command to check our answers.
     * @return - returns the output of using "treceval".
     */
    private static StringBuilder runCmd(String resultFileName) {
        StringBuilder s = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"" + directory + "\" && " + command + resultFileName);
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

    public void simulateSearch2Treceval(ArrayList<String> queries, ArrayList<String> queryNums, double bm25, double wup, double resnik, double jiang, double lin) {
        Ranker ranker = new Ranker();
        ranker.setWeights(bm25, wup, resnik, jiang,lin);
        new SupremeParameterTests().testSearch();
    }

    public void simulateSearch2TrecevalBM25(ArrayList<String> queries, ArrayList<String> queryNums, double k, double b, double delta, double idf) {
            Ranker ranker = new Ranker();
            ranker.setBM25Values(k, b, delta, idf);
            for (int i = 0; i < queries.size(); i++) {
                QuerySol query = new QuerySol("000|"+queries.get(i)+"|s|s|");
                TreeMap<Double, String> res = ranker.rank( dict, cache, docs, query, 50);
                makeResultsFile(new ArrayList<>(res.values()), queryNums.get(i),"results.txt");
            }
    }

    private void makeResultsFile(ArrayList<String> res, String s,String fileName) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(directory + "\\"+fileName), true));
            for (String docnum : res) {
                String doc = docnum;
                bufferedWriter.write(s + " 0 " + doc + " 1 0 si\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
        }
    }

    /**
     * using the function above for receiving the output of "treceval".
     * manipulating cutting the output for wished parameters.
     * this function used for testing optimal parameters for example: b, m, k, Delta, idf in BM25.
     * @return - Double array containing the wished result from the out put
     *          {r_precision, precision, recall, final_rank}
     */
    public double[] getResultRanked(StringBuilder stringBuilder) {
        String values = "";
        if (stringBuilder != null)
            values = stringBuilder.toString();

        double a_retrieved = Double.parseDouble(trim(substringBetween(values, "Retrieved:", "Relevant:")));
        double b_relevant = Double.parseDouble(trim(substringBetween(values, "Relevant:", "Rel_ret:")));
        double c_rel_ret = Double.parseDouble(trim(substringBetween(values, "Rel_ret:", "Interpolated Recall")));

        double r_precision = Double.parseDouble(trim(substringAfter(values, "Exact:")));

        double precision = c_rel_ret / a_retrieved;
        double recall = c_rel_ret / b_relevant;
        double final_rank = 2 * c_rel_ret / (a_retrieved + b_relevant);

//        System.out.println("R-Percision: " + r_precision + ", " + "Percision: " + precision + ", " + "Recall: " + recall + ", " + "Rank: " + final_rank);

        return new double[]{r_precision, precision, recall, final_rank,a_retrieved,b_relevant,c_rel_ret};
    }

    public double[] getTrecEvalGrades(String resPath, ArrayList<String> res, String qNum) {
        String dirKeeper = directory;
        directory = resPath;
        makeResultsFile(res,qNum,"tmpResults.txt");
        StringBuilder stringBuilder = runCmd("tmpResults.txt");
        double [] ranks = getResultRanked(stringBuilder);
        try{
            Files.deleteIfExists(Paths.get(directory+"\\tmpResults.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        directory = dirKeeper;
        return ranks;
    }

    public void setDics(TreeMap<String, String> termDictionary, TreeMap<String, String> cache, TreeMap<String, String> docDic) {
            dict = termDictionary;
            Treceval_cmd.cache = cache;
            docs = docDic;
    }

    public double[] getResultRanked() {
        return getResultRanked(runCmd("results.txt"));
    }
}
