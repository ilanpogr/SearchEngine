package Searcher;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.*;

public class QuerySol {
    private String qNum;
    private String title;
    private String desc;
    private String narr;
    private int postingPointer;
    private ArrayList<String> sols;
    private MutablePair<Integer, Double> evaluation;

    public QuerySol(QuerySol other) {
        this.qNum = other.qNum;
        this.title = other.title;
        this.desc = other.desc;
        this.narr = other.narr;
        this.sols = other.sols;
        this.postingPointer = -1;
    }

    /**
     * Returns a Mutable pair of the last evaluated query where key is QueryNum and value is rank
     *
     * @return the evaluation pair
     */
    public MutablePair<Integer, Double> getEvaluation() {
        return evaluation;
    }

    public double getEvaluationRank(){
        if (evaluation==null) return -1;
        else return evaluation.right;
    }


    public int getEvaluationOtherQueryNum(){
        if (evaluation==null) return -1;
        else return evaluation.left;
    }

    public void setEvaluation(int otherQNum, double rank) {
        if (evaluation == null) evaluation = new MutablePair<>(otherQNum, rank);
        else {
            evaluation.left = otherQNum;
            evaluation.right = rank;
        }
    }

    /**
     * appends the semantic words to the query title
     * @param semantics
     */
    public void setSemantic(String semantics){

    }

    public QuerySol(String query) {
        String[] q = split(query, "|");
        qNum = q[0];
        title = q[1];
        desc = q[2];
        narr = q[3];
        if (postingPointer == 0)
            try {
                postingPointer = Integer.parseInt(q[4], 36);
            } catch (Exception e) {
                QueryDic.getPointer();
            }
        sols = new ArrayList<>();
    }

    public QuerySol(String query, int pointer) {
        this(query);
        postingPointer = pointer;
    }

    /**
     * do not use the original list
     *
     * @return docnums
     */
    public ArrayList<String> getSols() {
        return new ArrayList<>(sols);
    }

    public String getqNum() {
        return qNum;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getNarr() {
        return narr;
    }

    public int getPostingPointer() {
        return postingPointer;
    }

    public void setPostingPointer(int p) {
        postingPointer += p;
    }

    public Integer getqNumAsInt() {

        return Integer.parseInt(qNum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuerySol) {
            QuerySol o = (QuerySol) obj;
            if (o.title.equals(this.title)) {
                return true;
            }
        }
        return false;
    }

    public void addPosting(String readLine) {
        if (!substringBefore(readLine, ",").equals(qNum))
            return;
        String[] docnums = split(substringAfter(readLine, ","), "|");

        for (int i = 0; i < docnums.length; i++) {
            if (!sols.contains(docnums[i])) {
                sols.add(docnums[i]);
            }
        }
    }

    public void filterSols(Set<String> docs) {
        if (docs == null || docs.size() == 0) return;
        ArrayList<String> newSols = new ArrayList<>();
        for (String s : sols) {
            if (docs.contains(s)) {
                newSols.add(s);
            }
        }
        sols = newSols;
    }

    public void filterSolsNum(int i) {
        while (sols.size() > i) sols.remove(0);
    }

    public void addSingleDocs(String value) {
        if (postingPointer == -1) sols.add(value);
    }

    public int getSolSize() {
        return sols.size();
    }

    public String[] getTitleArray() {
        return split(title, " .,-/");
    }
}
