package Ranker;

import Master.Master;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.tuple.MutablePair;
import static org.apache.commons.lang3.StringUtils.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Ranker {

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
    private static TreeMap<String,Integer> relationDic = new TreeMap<>();
    private static TreeMap<String,Double> docsRank = new TreeMap<>();
    private static TreeMap<String, MutablePair<ArrayList<String>,ArrayList<Double>>> orderedPosting = new TreeMap<>();

    /**
     * Main Function.
     * Give Ranks to each Document with a given query
     * @param postingPath - the path to the Posting files Dir
     * @param termDic - Term Dictionary
     * @param cache - cache Dictionary
     * @param docDic - document Dictionary
     * @param query - query Dictionary
     * @return a Map with all the docs to return by a given query
     *          key - DocNum (name)
     *          value - the Rank of the Document (double)
     */
    public TreeMap<String,Double> rank(String postingPath, TreeMap<String,String> termDic, TreeMap<String,String> cache, TreeMap<String,String> docDic, HashMap<String, Integer> query){
        reArrangePostingForQuery(postingPath,termDic,cache,docDic,query);
        TreeMap<String,Double> ranked = getBestDocs(50);
        return ranked;
    }

    /**
     * Makes a Posting list in a different order to be able to make some relations
     * and work faster with the postings of each term.
     * Read the report for further information.
     * @param postingPath - the path to the Posting files Dir
     * @param termDic - Term Dictionary
     * @param cache - cache Dictionary
     * @param docDic - document Dictionary
     * @param query - query Dictionary
     */
    private void reArrangePostingForQuery(String postingPath, TreeMap<String, String> termDic, TreeMap<String, String> cache, TreeMap<String, String> docDic, HashMap<String, Integer> query) {
        //TODO - Add Semantics!
        for (Map.Entry<String, Integer> entry : query.entrySet()) {
            String fromPosting="";
            String word = entry.getKey();
            int qf = entry.getValue(); //query Frequency
            if (!termDic.containsKey(word))continue;
            String linePointer = termDic.get(word);
            if (endsWith(linePointer,"*")){
            }


        }
    }

    private TreeMap<String, Double> getBestDocs(int i) {

        return docsRank;
    }

    public double calculateBM25(){

        return 0;
    }
}
