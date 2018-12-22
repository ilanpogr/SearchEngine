package Ranker;

import Master.Master;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
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

    public TreeMap<String,Double> rank(String postingPath, TreeMap<String,String> termDic, TreeMap<String,String> cache, TreeMap<String,String> docDic, TreeMap<String,String> query){

        TreeMap<String,Double> ranked = getBestDocs(50);
        return ranked;
    }

    private TreeMap<String, Double> getBestDocs(int i) {

        return docsRank;
    }

    public double calculateBM25(){

        return 0;
    }
}
