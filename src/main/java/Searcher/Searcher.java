package Searcher;

import Controller.PropertiesFile;
import Ranker.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class Searcher {


    private static Ranker ranker = new Ranker();

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
            if (QueryDic.getInstance().queryEvaluator(query) == 2) {
                query.filterSols(docs.keySet());
                if (!totalRickAll)
                    query.filterSolsNum(50);
                continue;
            }
            //TODO - add nerrative here
            freeLangSearch(query,dict,cache,docs,totalRickAll);
        }

    }

    public void freeLangSearch(QuerySol query, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, boolean totalRickAll) {
        ArrayList<String> res = rankQuery(query, dict, cache, docs, totalRickAll);
        for (int j = 0; j < res.size() && (query.getSolSize() < 50 || totalRickAll); j++) { //todo - check if insertion of new docnums will help
            query.addSingleDoc(res.get(j));
        }
        query.filterSols(docs.keySet());
        if (!totalRickAll) {
            query.filterSolsNum(50);
        }
    }
}
