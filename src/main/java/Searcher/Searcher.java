package Searcher;

import Controller.PropertiesFile;
import Master.Master;
import Ranker.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {


    private static Ranker ranker = new Ranker();

    private ArrayList<String> search(String path, String queryString, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, ArrayList<String> cities, boolean totalRickAll) {
        if (dict == null || cache == null || docs == null) return new ArrayList<>();
        HashMap<String, Integer> query = Master.makeQueryDic(queryString);
        ranker.setBM25Values(PropertiesFile.getPropertyAsDouble("k"), PropertiesFile.getPropertyAsDouble("b"), PropertiesFile.getPropertyAsDouble("d"), PropertiesFile.getPropertyAsDouble("f"));
        TreeMap<Double, String> res = ranker.rank(path, dict, cache, docs, query, totalRickAll?Integer.MAX_VALUE:50);
        return new ArrayList<>(res.values());
    }


    public void multiSearch(String path, ArrayList<QuerySol> queryList, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, ArrayList<String> cities, boolean totalRickAll) {
        if (dict == null || cache == null || docs == null || queryList == null)
            return;
        for (int i = 0; i < queryList.size(); i++) {
            QuerySol query = queryList.get(i);
            if (QueryDic.getInstance().queryEvaluator(query) == 1) {
//                StringBuilder stringBuilder = new StringBuilder(join(QueryDic.getInstance().getSolutions(query.getTitle()), "|"));
//                query.addPosting(query.getqNum() + "," + stringBuilder.toString());
                query.filterSols(docs.keySet());
                if (!totalRickAll)
                    query.filterSolsNum(50);
                continue;
            }
            //TODO - add nerrative here
            query.filterSols(docs.keySet());
            if (!totalRickAll)
                query.filterSolsNum(50);
            if (QueryDic.getInstance().queryEvaluator(query)<0.7 || query.getSolSize()==0) {
                ArrayList<String> res = search(path, query.getTitle(), dict, cache, docs,cities,totalRickAll);
                for (int j = 0; j < res.size() && query.getSolSize()<50; j++) {
                    query.addSingleDocs(res.get(j));
                }
            }
//                StringBuilder stringBuilder = new StringBuilder(query.getqNum()).append(",");
//                while (!res.isEmpty()) {
//                    stringBuilder.append(res.pollLastEntry().getValue()).append("|");
//
//
//                }
        }

    }
}
