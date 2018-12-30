package Tests;

import Controller.PropertiesFile;
import Model.ModelMenu;
import Searcher.QuerySol;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.substring;

public class SupremeParameterTests {

    ModelMenu modelMenu = new ModelMenu();

    private void crazyBionicAstroFantasticWeightsTest(String directory) {
        try {
            ArrayList<String> queries = new ArrayList<>();
            ArrayList<String> queryNums = new ArrayList<>();
            File file = new File(directory + "\\queries.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                if (startsWith(line, "<num> Number: ")) {
                    queryNums.add(trim(substring(line, 13)));
                }
                if (startsWith(line, "<title> ")) {
                    queries.add(trim(substring(line, 7)));
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            double bm25 = PropertiesFile.getPropertyAsDouble("bm25.weight"),
                    e = PropertiesFile.getPropertyAsDouble("e"),
                    maxBM25 = PropertiesFile.getPropertyAsDouble("maxBM25");
            Treceval_cmd tester = new Treceval_cmd();
            double[] maxVals = new double[7];
//            double[] maxVals = tester.getResultRanked();
//            Files.delete(Paths.get(directory + "\\results.txt"));
            CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\weights.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "Retrieved", "Relevant", "Relevant Returned", "BM25", "Wu Palmer", "resnik", "jiang", "lin"));
            printer.flush();
            double startTests = System.currentTimeMillis();
            int iter = 0;
            for (double bm25i = bm25; bm25i <= maxBM25; bm25i += e) {
                for (double wupi = 0; wupi + bm25i <= 1; wupi += e) {
                    for (double resniki = 0; resniki + wupi + bm25i <= 1; resniki += e) {
                        for (double jiangi = 0; jiangi + resniki + wupi + bm25i <= 1; jiangi += e) {
                            for (double lini = 1 - (jiangi + resniki + wupi + bm25i); lini + jiangi + resniki + wupi + bm25i == 1; lini += e) {
                                bm25i = Math.round(bm25i * 1e2) / 1e2;
                                resniki = Math.round(resniki * 1e2) / 1e2;
                                jiangi = Math.round(jiangi * 1e2) / 1e2;
                                wupi = Math.round(wupi * 1e2) / 1e2;
                                lini = Math.round(lini * 1e2) / 1e2;
                                tester.simulateSearch2Treceval(queries, queryNums, bm25i, wupi, resniki, jiangi, lini);
                                double[] newVals = tester.getResultRanked();
                                if (newVals[3] > maxVals[3]) {
                                    maxVals = newVals.clone();
                                    printer.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], newVals[4], newVals[5], newVals[6], bm25i, wupi, resniki, jiangi, lini);
                                    printer.flush();
                                }
                                Files.delete(Paths.get(directory + "\\results.txt"));
                                double currentTime = System.currentTimeMillis();
                                iter++;
                                System.out.println((currentTime - startTests) / 1000 + "\tbm25 = " + bm25i + "\twup = " + wupi + "\tresnik = " + resniki + "\tjiang = " + jiangi + "\tlin = " + lini + "\tsum = " + (lini + jiangi + resniki + wupi + bm25i) + "\t\t" + Arrays.toString(newVals));
                            }
                        }
                    }
                }
            }
            System.out.println();
            System.out.println(iter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void testBM25(String directory) {
        try {
            ArrayList<String> queries = new ArrayList<>();
            ArrayList<String> queryNums = new ArrayList<>();
            File file = new File(directory + "\\queries.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                if (startsWith(line, "<num> Number: ")) {
                    queryNums.add(trim(substring(line, 13)));
                }
                if (startsWith(line, "<title> ")) {
                    queries.add(trim(substring(line, 7)));
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            double k = PropertiesFile.getPropertyAsDouble("k"), b = PropertiesFile.getPropertyAsDouble("b"), d = PropertiesFile.getPropertyAsDouble("d"), f = PropertiesFile.getPropertyAsDouble("f"), e = PropertiesFile.getPropertyAsDouble("e"), maxk = PropertiesFile.getPropertyAsDouble("maxk");
            Treceval_cmd tester = new Treceval_cmd();
            double[] maxVals1 = tester.getResultRanked();
            double[] maxVals2 = tester.getResultRanked();
            double[] maxVals3 = tester.getResultRanked();
            double[] maxVals4 = tester.getResultRanked();
            Files.delete(Paths.get(directory + "\\results.txt"));
            CSVPrinter printer1 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\R-Percision.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer2 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Percision.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer3 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Recall.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            CSVPrinter printer4 = new CSVPrinter(Files.newBufferedWriter(Paths.get(directory + "\\Rank.csv")), CSVFormat.DEFAULT.withHeader("R-Percision", "Percision", "Recall", "Rank", "k", "b", "delta", "idf"));
            printer1.flush();
            printer2.flush();
            printer3.flush();
            printer4.flush();
            double startTests = System.currentTimeMillis();
            for (double kk = k; kk <= maxk; kk += e) {
                for (double bb = b; bb <= 1; bb += e) {
                    for (double dd = d; dd <= 1; dd += 2 * e) {
                        for (double ff = f; ff <= 0.6; ff += e) {
                            tester.simulateSearch2TrecevalBM25(queries, queryNums, kk, bb, dd, ff);
                            double[] newVals = tester.getResultRanked();
                            if (newVals[0] > maxVals1[0]) {
                                maxVals1 = newVals.clone();
                                printer1.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer1.flush();
                            }
                            if (newVals[1] > maxVals2[1]) {
                                maxVals2 = newVals.clone();
                                printer2.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer2.flush();
                            }
                            if (newVals[2] > maxVals3[2]) {
                                maxVals3 = newVals.clone();
                                printer3.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer3.flush();
                            }
                            if (newVals[3] > maxVals4[3]) {
                                maxVals4 = newVals.clone();
                                printer4.printRecord(newVals[0], newVals[1], newVals[2], newVals[3], kk, bb, dd, ff);
                                printer4.flush();
                            }
                            Files.delete(Paths.get(directory + "\\results.txt"));
                            double currentTime = System.currentTimeMillis();
                            System.out.println((currentTime - startTests) / 1000 + "\nk = " + kk + "\tb = " + bb + "\tdelta = " + dd + "\tidf = " + ff + "\n" +
                                    Arrays.toString(maxVals1) + "\n" +
                                    Arrays.toString(maxVals2) + "\n" +
                                    Arrays.toString(maxVals3) + "\n" +
                                    Arrays.toString(maxVals4) + "\n"
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchQueries() {
        ArrayList<String> cities = new ArrayList<>();
//        loadQueriesFile();
        PropertiesFile.putProperty("queries.file.path", "C:\\Users\\User\\Documents\\SearchEngineTests\\queries.txt");
        ArrayList<QuerySol> querySols = modelMenu.multiSearch(cities);
//        for (QuerySol querySol : querySols) {
//            System.out.print("\n" + querySol.getqNum() + ": " + querySol.getTitle() + ": ");
//            for (String s : querySol.getSols()) {
//                System.out.print(s + ", ");
//            }
//        }
    }

    public void testSearch() {
        searchQueries();
    }
}
