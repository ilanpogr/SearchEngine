package Searcher;

import Master.Master;
import Ranker.Ranker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private static Ranker ranker = new Ranker();

    public ArrayList<String> search(String path, String queryString,  TreeMap<String, String> dict, TreeMap<String, String> cache, TreeMap<String, String> docs, ArrayList<String> cities) {
        if (dict == null || cache == null || docs == null ) return new ArrayList<>();
        HashMap<String, Integer> query = Master.makeQueryDic(queryString);
        TreeMap<Double,String> res = ranker.rank(path,dict,cache,docs,query);
        return new ArrayList<>(res.values());
    }




}
