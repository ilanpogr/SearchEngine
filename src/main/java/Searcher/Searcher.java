package Searcher;

import Controller.PropertiesFile;
import Ranker.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class Searcher {


    private Ranker ranker = new Ranker();

    private ArrayList<String> rankQuery(QuerySol query, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, boolean totalRickAll) {
        if (dict == null || cache == null || docs == null) return new ArrayList<>();
        ranker.setBM25Values(PropertiesFile.getPropertyAsDouble("k"), PropertiesFile.getPropertyAsDouble("b"), PropertiesFile.getPropertyAsDouble("d"), PropertiesFile.getPropertyAsDouble("f"));
        TreeMap<Double, String> res = ranker.rank(dict, cache, docs, query, totalRickAll ? Integer.MAX_VALUE : 50);
        return new ArrayList<>(res.values());
    }


    public void multiSearch(ArrayList<QuerySol> queryList, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, boolean totalRickAll) {
        if (dict == null || cache == null || docs == null || queryList == null)
            return;
        for (int i = 0; i < queryList.size(); i++) {
            QuerySol query = queryList.get(i);
            if (QueryDic.getInstance().queryEvaluator(query) == 2 && totalRickAll) {
                query.filterSols(docs.keySet());
                continue;
            }
            //TODO - add nerrative here
            freeLangSearch(query, dict, cache, docs);
        }

    }

    private void freeLangSearch(QuerySol query, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs) {
        ArrayList<String> res = rankQuery(query, dict, cache, docs, false);
        query.removeSuggestedSols();
        for (int j = 0; j < res.size() && query.getSolSize() < 50; j++) {
            query.addSingleDoc(res.get(j));
        }
        query.filterSols(docs.keySet());
        query.filterSolsNum(50);
    }
}
