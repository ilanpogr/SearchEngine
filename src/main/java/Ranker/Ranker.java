package Ranker;

import Controller.PropertiesFile;
import Master.Master;
import ReadFile.ReadFile;
import Searcher.QuerySol;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ranker Class Ranks Documents by a given Query
 */
public class Ranker {

    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private double averageDocLength = Master.getAverageDocLength();
    private double BM25__b;
    private double BM25__k;
    private double BM25__idf;
    private double BM25__delta;
    private double BM25__weight;
    private static double dbWeights;
    private static ArrayList<ImmutablePair<RelatednessCalculator,Double>> calculators = initCalculators();
    private TreeMap<String, ImmutablePair<ArrayList<String>, ArrayList<String>>> orderedPosting;
    private TreeMap<String, ArrayList<ImmutablePair<String, String>>> relationDic;
    private TreeMap<String, Double> docsRank;
    private static TreeMap<String, ArrayList<String>> solDict = new TreeMap<>(String::compareToIgnoreCase);


    /**
     * similarity functions weights
     * @return double - weights     Range:[0,1]
     */
    public static double getWeights() {
        return dbWeights;
    }

    /**
     * Initialize the Similarity functions ArrayList
     * @return the calculators list
     */
    private static ArrayList<ImmutablePair<RelatednessCalculator, Double>> initCalculators() {
        ILexicalDatabase db = new NictWordNet();
        ArrayList<ImmutablePair<RelatednessCalculator,Double>> calcList = new ArrayList<>();
        double tmp = PropertiesFile.getPropertyAsDouble("wuPalmer.weight");
        calcList.add(new ImmutablePair<>(new WuPalmer(db),tmp));
        dbWeights+=tmp;
        tmp = PropertiesFile.getPropertyAsDouble("resnik.weight");
        calcList.add(new ImmutablePair<>(new ResnikMod(db),tmp));
        dbWeights+=tmp;
        tmp = PropertiesFile.getPropertyAsDouble("jiang.weight");
        calcList.add(new ImmutablePair<>(new JiangConrath(db),tmp));
        dbWeights+=tmp;
        tmp = PropertiesFile.getPropertyAsDouble("lin.weight");
        calcList.add(new ImmutablePair<>(new Lin(db),tmp));
        dbWeights+=tmp;
        return calcList;
    }

    /**
     * Main Function.
     * Give Ranks to each Document with a given query
     *
     * @param termDic - Term Dictionary
     * @param cache   - cache Dictionary
     * @param docDic  - document Dictionary
     * @param query   - query Dictionary
     * @return a Map with all the docs to return by a given query
     * key - DocNum (name)
     * value - the Rank of the Document (double)
     */
    public TreeMap<Double, String> rank(TreeMap<String, String> termDic, TreeMap<String, String> cache, TreeMap<String, String> docDic, QuerySol query, int solSize) {
        docsRank = new TreeMap<>();
        relationDic = new TreeMap<>();
        orderedPosting = new TreeMap<>();
        reArrangePostingForQuery(termDic, cache, docDic, Master.makeQueryDic(query,this));
        arrangeDictionaryForCalculations();
        //todo - Wiq and CosSim
        calculateBM25(termDic, docDic);
        return getBestDocs(query, solSize);
    }


    /**
     * Makes a Posting list in a different order to be able to make some relations
     * and work faster with the postings of each term.
     * Read the report for further information.
     *
     * @param termDic - Term Dictionary
     * @param cache   - cache Dictionary
     * @param docDic  - document Dictionary
     * @param query   - query Dictionary
     */
    private void reArrangePostingForQuery(TreeMap<String, String> termDic, TreeMap<String, String> cache, TreeMap<String, String> docDic, HashMap<String, Integer> query) {
        for (Map.Entry<String, Integer> entry : query.entrySet()) {
            StringBuilder fromPosting = new StringBuilder(256);
            String word = entry.getKey();
            int qf = entry.getValue(); //query Frequency
            if (termDic.containsKey(upperCase(word)))
                word = upperCase(word);
            if (!termDic.containsKey(word)) continue;
            String termVal = termDic.get(word);
            if (endsWith(termVal, "*")) { //it's in cache
                termVal = substringBeforeLast(termVal, ",");
                String[] postPoint = split(cache.get(word), ",");
                fromPosting.append(postPoint[0]).append(fileDelimiter).append(ReadFile.getTermLine(new StringBuilder(substringBeforeLast(PropertiesFile.getProperty("save.files.path"), "\\")), word, postPoint[1]));
            } else {
                fromPosting.append(ReadFile.getTermLine(new StringBuilder(substringBeforeLast(PropertiesFile.getProperty("save.files.path"), "\\")), word, substringAfterLast(termVal, ",")));
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
                    orderedValue.right.add(splitted[i]);
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

    /**
     * Sorts and Filters the Documents with their values.
     *  the query is given to give bonus to known pre-calculated answers
     *
     * @param query - the query object to Evaluate the bonus (read Report)
     * @param maxSolSize - max amount of documents in the solution
     * @return TreeMap of the Rank as key and DocNum as value
     */
    public TreeMap<Double, String> getBestDocs(QuerySol query, int maxSolSize) {
        TreeMap<Double, String> res = new TreeMap<>(Double::compareTo);
        for (Map.Entry<String, Double> o : docsRank.entrySet()
        ) {
            String doc = o.getKey();
            Double rank = o.getValue();
            rank+=query.getSolRank(doc)*dbWeights;
            while (res.containsKey(rank))
                rank -= Math.pow(10, -9);
            res.put(rank, doc);
            if (res.size() > maxSolSize)
                res.pollFirstEntry();
        }
        return res;
    }

    /**
     * calculates the rank given by BM25+
     *
     * @param termDic - dictionary
     * @param docDic  - docs map
     */
    public void calculateBM25(TreeMap<String, String> termDic, TreeMap<String, String> docDic) {
        for (Map.Entry<String, ArrayList<ImmutablePair<String, String>>> entry : relationDic.entrySet()) {
            double res = 0;
            String docNum = entry.getKey();
            ArrayList<ImmutablePair<String, String>> termsList = entry.getValue();
            for (int i = 0; i < termsList.size(); i++) {
                ImmutablePair<String, String> pair = termsList.get(i);
                String term = pair.left;
                if (!termDic.containsKey(term)) {
                    if (termDic.containsKey(upperCase(term)))
                        term = upperCase(term);
                }
                String positions = pair.right;
                if (docDic.containsKey(docNum)) {
                    String[] docRecord = split(docDic.get(docNum), ",");
                    int docLength = Integer.parseInt(docRecord[1]);
                    int df = Integer.parseInt(split(termDic.get(term), ",")[1]);
                    double idf = StrictMath.log10((docDic.size() - df + BM25__idf) / (df + BM25__idf));
                    int tf = Master.getFrequencyFromPosting(positions);
                    double tfInDoc = tf * Integer.parseInt(docRecord[0]);
                    double Dlen = docLength / averageDocLength;
                    double punishment = 1 - BM25__b + (BM25__b * Dlen);
                    double mone = tfInDoc * (BM25__k + 1);
                    double mehane = tfInDoc + (BM25__k * punishment);
                    res += idf * ((mone / mehane) + BM25__delta);
                }
            }
            docsRank.put(docNum, res * BM25__weight);
        }
    }

    /**
     * Sets the Values of BM25 Function
     * @param k - parameter k       Range: [1.2,2]
     * @param b - parameter b       Range: [0,1]
     * @param delta - read report   Range: [0,1]
     * @param idf - read report     Range: [0,1]
     */
    public void setBM25Values(double k, double b, double delta, double idf) {
        BM25__k = k;
        BM25__b = b;
        BM25__delta = delta;
        BM25__idf = idf;
        BM25__weight = PropertiesFile.getPropertyAsDouble("bm25.weight");
    }


    /**
     * Calculate and Sum the Similarity between two words.
     * the values given by the calculations is weighted.
     * means that each function is multiplied respectively to the weight of the function
     * @param w1 - first word
     * @param w2 - second word
     * @return double - the Similarity between the two words
     */
    public static double getWeigthedSimilarity(String w1, String w2) {
        double res = 0;
        if (w1.equalsIgnoreCase(w2)) return dbWeights;
        for (int i = 0; i < calculators.size(); i++) {
            ImmutablePair<RelatednessCalculator, Double> rc = calculators.get(i);
            res+= (rc.right*rc.left.calcRelatednessOfWords(w1, w2));
        }
        return res;
    }

    /**
     * sets the Weights of the Similarity functions in the ranker
     * the Weights have to sum to 1.
     * @param bm25 - the weight will be given to the similarity function - BM25             (default 0.2)
     * @param wup - the weight will be given to the similarity function  - Wu Palmer        (default 0.32)
     * @param resnik - the weight will be given to the similarity function - Resnik         (default 0.08)
     * @param jiang - the weight will be given to the similarity function - Jiang Conrath   (default 0.16)
     * @param lin - the weight will be given to the similarity function - Lin               (default 0.24)
     */
    public void setWeights(double bm25, double wup, double resnik, double jiang, double lin) {
        if (bm25>1 || bm25+wup+resnik+jiang+lin>1) setWeights();
        BM25__weight = bm25;
        calculators.set(0,new ImmutablePair<>(calculators.get(0).left,wup));
        calculators.set(1,new ImmutablePair<>(calculators.get(1).left,resnik));
        calculators.set(2,new ImmutablePair<>(calculators.get(2).left,jiang));
        calculators.set(3,new ImmutablePair<>(calculators.get(3).left,lin));
        dbWeights = 1-bm25;
    }

    /**
     * Sets default Weights
     */
    private void setWeights() {
        BM25__weight = 0.2;
        calculators.set(0,new ImmutablePair<>(calculators.get(0).left,0.32));
        calculators.set(1,new ImmutablePair<>(calculators.get(1).left,0.08));
        calculators.set(2,new ImmutablePair<>(calculators.get(2).left,0.16));
        calculators.set(3,new ImmutablePair<>(calculators.get(3).left,0.24));
        dbWeights = 0.8;
    }

    /**
     * Overriding the similarity of Resnik for normalitation
     */
    private static class ResnikMod extends Resnik{
        public ResnikMod(ILexicalDatabase db) {
            super(db);
        }

        @Override
        public double calcRelatednessOfWords(String word1, String word2) {
            return Double.min(super.calcRelatednessOfWords(word1, word2)/10,1);
        }
    }
}
