package Tests;

import java.io.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Treceval_cmd {

    static String directory = "D:\\Documents\\school\\semester e 3\\Ihzur\\Project\\PartB\\Files\\";
    static String command = "treceval qrels.txt results.txt";


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

    public static double[] getResultRanked() {
        StringBuilder s = runCmd();
        String values = "";
        if (s != null)
            values = s.toString();

        double a_retrieved = Double.parseDouble(trim(substringBetween(values, "Retrieved:", "Relevant:")));
        double b_relevant = Double.parseDouble(trim(substringBetween(values, "Relevant:", "Rel_ret:")));
        double c_rel_ret = Double.parseDouble(trim(substringBetween(values, "Rel_ret:", "Interpolated Recall")));

        double r_precision = Double.parseDouble(trim(substringAfter(values, "Exact:")));

        double precision = c_rel_ret/a_retrieved;
        double recall = c_rel_ret/b_relevant;
        double final_rank = 2*c_rel_ret/(a_retrieved+b_relevant);

        System.out.println("R-Percision: " + r_precision + ", " + "Percision: " + precision + ", " + "Recall: " + recall + ", " + "Rank: " + final_rank);

        return new double[] {r_precision,precision,recall,final_rank};
    }
}
