package Searcher;

import Controller.PropertiesFile;
import Master.Master;
import Ranker.Ranker;
import Tests.Treceval_cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {


    private static Ranker ranker = new Ranker();

    public ArrayList<String> search(String path, String queryString, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, ArrayList<String> cities) {
        if (dict == null || cache == null || docs == null) return new ArrayList<>();
        HashMap<String, Integer> query = Master.makeQueryDic(queryString);
        TreeMap<Double, String> res = ranker.rank(path, dict, cache, docs, query);
        return new ArrayList<>(res.values());
    }


    public void multiSearch(String path, ArrayList<QuerySol> queryList, TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, ArrayList<String> cities) {
        if (!(dict == null || cache == null || docs == null)) {
            for (int i = 0; i < queryList.size(); i++) {
                QuerySol query = queryList.get(i);
                if (QueryDic.getInstance().queryEvaluator(query) == 1) {
                    queryList.get(i).addPosting(join(QueryDic.getInstance().getSolutions(query.getTitle()), "|"));
                    double[] results = new Treceval_cmd().getTrecEvalGrades(PropertiesFile.getProperty("save.files.path"), query.getSols(), query.getqNum());
                    if (results[1] == 1 || results[3] > 0.8) continue;
                }
                //TODO - add nerrative here
                HashMap<String, Integer> prepQuery = Master.makeQueryDic(query.getTitle());
                TreeMap<Double, String> res = ranker.rank(path, dict, cache, docs, prepQuery);
                StringBuilder stringBuilder = new StringBuilder(query.getqNum()).append(",");
                while (!res.isEmpty()) {
                    stringBuilder.append(res.pollLastEntry().getValue()).append("|");
                }

            }
        }
    }
}
