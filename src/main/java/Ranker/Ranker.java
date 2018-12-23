package Ranker;

import Controller.PropertiesFile;
import Master.Master;
import ReadFile.ReadFile;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Ranker {

    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static double averageDocLength = Master.getAvrageDocLength();
    private static double BM25__b;
    private static double BM25__k;
    private static double BM25__idf;
    private static double BM25__delta;
    private static double BM25__weight;
    private static double Cosine__weight;
    private static double WuPalmer__weight;
    private static double Lin__weight;
    private static double JiangConrath__weight;
    private static double Lesk__weight;
    private static double LeacockChodrow__weight;
    private static double Resnik__weight;
    private static double Path__weight;
    private static TreeMap<String, ArrayList<ImmutablePair<String, String>>> relationDic = new TreeMap<>();
    private static TreeMap<String, Double> docsRank = new TreeMap<>();
    private TreeMap<String, ImmutablePair<ArrayList<String>, ArrayList<String>>> orderedPosting = new TreeMap<>();

    /**
     * Main Function.
     * Give Ranks to each Document with a given query
     *
     * @param postingPath - the path to the Posting files Dir
     * @param termDic     - Term Dictionary
     * @param cache       - cache Dictionary
     * @param docDic      - document Dictionary
     * @param query       - query Dictionary
     * @return a Map with all the docs to return by a given query
     * key - DocNum (name)
     * value - the Rank of the Document (double)
     */
    public TreeMap<String, Double> rank(String postingPath, TreeMap<String, String> termDic, TreeMap<String, String> cache, TreeMap<String, String> docDic, HashMap<String, Integer> query) {
        reArrangePostingForQuery(postingPath, termDic, cache, docDic, query);
        arrangeDictionaryForCalculations();
        calculateBM25(termDic,docDic);
        return getBestDocs(50);
    }

    /**
     * Makes a Posting list in a different order to be able to make some relations
     * and work faster with the postings of each term.
     * Read the report for further information.
     *
     * @param postingPath - the path to the Posting files Dir
     * @param termDic     - Term Dictionary
     * @param cache       - cache Dictionary
     * @param docDic      - document Dictionary
     * @param query       - query Dictionary
     */
    private void reArrangePostingForQuery(String postingPath, TreeMap<String, String> termDic, TreeMap<String, String> cache, TreeMap<String, String> docDic, HashMap<String, Integer> query) {
        //TODO - Add Semantics!
        for (Map.Entry<String, Integer> entry : query.entrySet()) {
            StringBuilder fromPosting = new StringBuilder(256);
            String word = entry.getKey();
            int qf = entry.getValue(); //query Frequency
            if (!termDic.containsKey(word)) continue;
            String termVal = termDic.get(word);
            if (endsWith(termVal, "*")) { //it's in cache
                termVal = substringBeforeLast(termVal, ",");
                String[] postPoint = split(cache.get(word), ",");
                fromPosting.append(postPoint[0]).append(ReadFile.getTermLine(new StringBuilder(postingPath), word, postPoint[1]));
            } else {
                fromPosting.append(ReadFile.getTermLine(new StringBuilder(postingPath), word, substringAfterLast(termVal, ",")));
            }
            fromPosting.trimToSize();
            if (fromPosting.length() != 0) {
                String[] splitted = split(fromPosting.toString(), fileDelimiter);
                ArrayList<String> docNums = new ArrayList<>(splitted.length / 2);
                ArrayList<String> positions = new ArrayList<>(splitted.length / 2);
                ImmutablePair<ArrayList<String>, ArrayList<String>> orderedValue = new ImmutablePair<>(docNums, positions);
                orderedPosting.put(word, orderedValue);
                for (int i = 0; i < splitted.length; i++) {
                    orderedValue.left.add(splitted[i++]);
                    //todo - choose if positions are useful here
                    orderedValue.left.add(splitted[i]);
                }
            }

        }
    }

    /**
     * makes a dictionary with document number as a key to make
     * calculations easier
     * Read the report for further information.
     */
    private void arrangeDictionaryForCalculations() {
        for (Map.Entry<String, ImmutablePair<ArrayList<String>, ArrayList<String>>> entry : orderedPosting.entrySet()) {
            String term = entry.getKey();
            ImmutablePair<ArrayList<String>, ArrayList<String>> value = entry.getValue();
            ArrayList<String> docs = value.left;
            ArrayList<String> frqs = value.right;
            for (int i = 0; i < docs.size(); i++) {
                String docNum = docs.get(i);
                ImmutablePair<String, String> pair = new ImmutablePair<>(term, frqs.get(i));
                if (!relationDic.containsKey(docNum))
                    relationDic.put(docNum, new ArrayList<>());
                relationDic.get(docNum).add(pair);
            }
        }
    }

    private TreeMap<String, Double> getBestDocs(int i) {
        return docsRank;
    }

    public void calculateBM25(TreeMap<String, String> termDic, TreeMap<String, String> docDic) {
        int N = docDic.size();
        double log2 = StrictMath.log10(2);
        double res = 0;
        for (Map.Entry<String, ArrayList<ImmutablePair<String, String>>> entry : relationDic.entrySet()) {
            String docNum = entry.getKey();
            ArrayList<ImmutablePair<String, String>> termsList = entry.getValue();
            for (int i = 0; i < termsList.size(); i++) {
                ImmutablePair<String, String> pair = termsList.get(i);
                String term = pair.left;
                String positions = pair.right;
                if (docDic.containsKey(docNum)) {
                    String[] docRecord = split(docDic.get(docNum), ",");
                    int docLength = Integer.parseInt(docRecord[1]);
                    int df = Integer.parseInt(split(termDic.get(docNum), ",")[1]);
                    double logN = StrictMath.log10(N - df + BM25__idf) / log2;
                    double idf = logN - (StrictMath.log10(df + BM25__idf) / log2);
                    int tf = Master.getFrequencyFromPosting(positions);
                    double tfInDoc = tf * Integer.parseInt(docRecord[0]);
                    double Dlen = Math.abs(docLength) / averageDocLength;
                    double punishment = 1 - BM25__b + (BM25__b * Dlen);
                    double mone = tfInDoc * (BM25__k + 1);
                    double mehane = tfInDoc + (BM25__k * punishment);
                    res += idf * ((mone / mehane) + BM25__delta);
                }
            }
            docsRank.put(docNum,res*BM25__weight);
        }
    }

//    public static void main(String[] args) {
//        double start,end;
//        String s = "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123";
//        start = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            s = substringBeforeLast(s,"1");
//        }
//        end = System.currentTimeMillis();
//        System.out.println(s);
//        System.out.println((end-start)/1000);
//
//        s = "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123";
//        start = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            s = stripEnd(s,"23");
//            s = stripEnd(s,"1");
//        }
//        end = System.currentTimeMillis();
//        System.out.println(s);
//        System.out.println((end-start)/1000);
////        for (int i = 0; i < 1000; i++) {
////            System.out.print("123");
////        }
//    }
}
